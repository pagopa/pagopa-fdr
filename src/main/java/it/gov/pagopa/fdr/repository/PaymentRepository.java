package it.gov.pagopa.fdr.repository;

import io.micrometer.core.annotation.Timed;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import it.gov.pagopa.fdr.repository.common.Repository;
import it.gov.pagopa.fdr.repository.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.common.SortField;
import it.gov.pagopa.fdr.repository.entity.PaymentEntity;
import it.gov.pagopa.fdr.util.common.BigEndianWriter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.Session;
import org.jboss.logging.Logger;
import org.postgresql.copy.PGCopyOutputStream;
import org.postgresql.core.BaseConnection;

@ApplicationScoped
public class PaymentRepository extends Repository implements PanacheRepository<PaymentEntity> {

  @ConfigProperty(name = "payments.batch-insert.buffer-size")
  Integer pgCopyBufferSize;

  public static final String INDEX = "id.index";
  private final EntityManager entityManager;

  private final Logger log;

  public static final String QUERY_GET_BY_FLOW_ID = "id.flowId = ?1";

  public static final String QUERY_GET_BY_FLOW_ID_AND_INDEXES = "id.flowId = ?1" + " and id.index in ?2";

    private static final String INSERT_IN_BULK =
            "COPY payment (flow_id, iuv, iur, \"index\", amount, pay_date, pay_status, transfer_id, created, updated) " +
                    "FROM STDIN WITH (FORMAT BINARY)";


  public PaymentRepository(Logger log, EntityManager em) {
    this.log = log;
    this.entityManager = em;
  }

  public RepositoryPagedResult<PaymentEntity> findByPspAndIuvAndIur(
      String pspId,
      String iuv,
      String iur,
      Instant createdFrom,
      Instant createdTo,
      String orgDomainId,
      int pageNumber,
      int pageSize) {

    StringBuilder query =
        new StringBuilder(
            "SELECT p FROM PaymentEntity p LEFT JOIN FETCH p.flow WHERE p.flow.pspDomainId = :psp");
    Parameters params = new Parameters().and("psp", pspId);

    if (orgDomainId != null) {
      query.append("  and p.flow.orgDomainId = :orgDomainId");
      params.and("orgDomainId", orgDomainId);
    }
    if (iuv != null) {
      query.append(" and p.iuv = :iuv");
      params.and("iuv", iuv);
    }
    if (iur != null) {
      query.append(" and p.iur = :iur");
      params.and("iur", iur);
    }
    if (createdFrom != null) {
      query.append(" and p.created >= :createdFrom");
      params.and("createdFrom", createdFrom);
    }
    if (createdTo != null) {
      query.append(" and p.created <= :createdTo");
      params.and("createdTo", createdTo);
    }

    // add ORDER BY as qualified alias to avoid ambiguity with the JOIN on flow
    query.append(" ORDER BY p.id.index ASC");

    Page page = Page.of(pageNumber - 1, pageSize);

    PanacheQuery<PaymentEntity> resultPage =
        PaymentEntity.findPageByQuery(query.toString(), params).page(page);
    return getPagedResult(resultPage);
  }

  public long countByFlowIdAndIndexes(Long flowId, Set<Long> indexes) {
    return count(QUERY_GET_BY_FLOW_ID_AND_INDEXES, flowId, indexes);
  }

  @Timed(value = "paymentRepository.createEntityInBulk.task", description = "Time taken to perform createEntityInBulk", percentiles = 0.95, histogram = true)
  public void createEntityInBulk(List<PaymentEntity> entityBatch) {

    Session session = entityManager.unwrap(Session.class);
    session.doWork(connection -> {
      BaseConnection pgConnection = connection.unwrap(BaseConnection.class);

      try (
        PGCopyOutputStream outputStream = new PGCopyOutputStream(pgConnection, INSERT_IN_BULK);
        BufferedOutputStream out = new BufferedOutputStream(outputStream, pgCopyBufferSize)
      ) {

        // Header signature
        out.write("PGCOPY\n\377\r\n\0".getBytes(StandardCharsets.ISO_8859_1));

        // Flags field (32-bit, 0)
        BigEndianWriter.writeInt32(out, 0);

        // Header extension (32-bit, 0)
        BigEndianWriter.writeInt32(out, 0);

        //
        for (PaymentEntity entity : entityBatch) {

          // Define a row as 10-columns stream
          BigEndianWriter.writeInt16(out, 10);

          BigEndianWriter.writeBigInt(out, entity.getId().getFlowId());
          BigEndianWriter.writeText(out, entity.getIuv());
          BigEndianWriter.writeText(out, entity.getIur());
          BigEndianWriter.writeBigInt(out, entity.getId().getIndex());
          BigEndianWriter.writeNumeric(out, entity.getAmount());
          BigEndianWriter.writeTimestamp(out, entity.getPayDate());
          BigEndianWriter.writeText(out, entity.getPayStatus());
          BigEndianWriter.writeBigInt(out, entity.getTransferId());
          BigEndianWriter.writeTimestamp(out, entity.getCreated());
          BigEndianWriter.writeTimestamp(out, entity.getUpdated());
        }

        // File trailer, -1 as int16
        BigEndianWriter.writeInt16(out, -1);
        out.flush();

      } catch (SQLException e) {
          log.error("An error occurred while executing payments bulk insert", e);
          throw e;
      } catch (IOException e) {
        log.error("An error occurred while executing payments bulk insert", e);
        throw new RuntimeException(e);
      }
    });
  }

  public PanacheQuery<PaymentEntity> findPageByFlowId(Long flowId, int pageNumber, int pageSize) {

    Page page = Page.of(pageNumber, pageSize);
    Sort sort = getSort(SortField.of(INDEX, Direction.Ascending));

    return find(QUERY_GET_BY_FLOW_ID, sort, flowId).page(page);
  }

  public RepositoryPagedResult<PaymentEntity> findByFlowId(
      Long flowId, int pageNumber, int pageSize) {

    Page page = Page.of(pageNumber - 1, pageSize);
    Sort sort = getSort(SortField.of(INDEX, Direction.Ascending));

    PanacheQuery<PaymentEntity> resultPage = find(QUERY_GET_BY_FLOW_ID, sort, flowId).page(page);
    return getPagedResult(resultPage);
  }

  public List<PaymentEntity> findByFlowIdAndIndexes(Long flowId, Set<Long> indexes) {
    return find(QUERY_GET_BY_FLOW_ID_AND_INDEXES, flowId, indexes).list();
  }

  public void deleteEntityInBulk(List<PaymentEntity> entityBatch) {

    for (PaymentEntity entity : entityBatch) {
      delete("id.flowId = ?1 and id.index = ?2", entity.getId().getFlowId(), entity.getId().getIndex());
    }
  }
}
