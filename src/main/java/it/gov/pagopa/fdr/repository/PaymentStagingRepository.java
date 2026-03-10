package it.gov.pagopa.fdr.repository;

import io.micrometer.core.annotation.Timed;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import it.gov.pagopa.fdr.repository.common.Repository;
import it.gov.pagopa.fdr.repository.entity.PaymentStagingEntity;
import it.gov.pagopa.fdr.util.common.BigEndianWriter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.Session;
import org.jboss.logging.Logger;
import org.postgresql.copy.PGCopyOutputStream;
import org.postgresql.core.BaseConnection;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class PaymentStagingRepository extends Repository implements PanacheRepository<PaymentStagingEntity> {

  @ConfigProperty(name = "payments.batch-insert.buffer-size")
  Integer pgCopyBufferSize;

  public static final String QUERY_GET_BY_FLOW_ID_INDEXES_AND_ORG_ID = "id.flowId = ?1" + " and id.index in ?2 and id.orgId = ?3";

  private static final String INSERT_IN_BULK =
          "COPY payment_staging (flow_id, \"index\", org_id, iuv, iur, amount, pay_date, pay_status, transfer_id, created, updated) " +
                  "FROM STDIN WITH (FORMAT BINARY)";

  private final EntityManager entityManager;

  private final Logger log;

  public PaymentStagingRepository(Logger log, EntityManager em) {
    this.log = log;
    this.entityManager = em;
  }

  public List<PaymentStagingEntity> findByFlowIdIndexesAndOrgId(Long flowId, Set<Long> indexes, String orgDomainId) {
    return find(QUERY_GET_BY_FLOW_ID_INDEXES_AND_ORG_ID, flowId, indexes,  orgDomainId).list();
  }

  public void deleteEntityInBulk(List<PaymentStagingEntity> entityBatch) {
    for (PaymentStagingEntity entity : entityBatch) {
      delete("id.flowId = ?1 and id.index = ?2", entity.getId().getFlowId(), entity.getId().getIndex());
    }
  }

  @Timed(value = "paymentRepository.createEntityInBulk.task", description = "Time taken to perform createEntityInBulk", percentiles = 0.95, histogram = true)
  public void createEntityInBulk(List<PaymentStagingEntity> entityBatch, String orgDomainId) {
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

        for (PaymentStagingEntity entity : entityBatch) {

          // Define a row as 11-columns stream
          BigEndianWriter.writeInt16(out, 11);

          BigEndianWriter.writeBigInt(out, entity.getId().getFlowId());
          BigEndianWriter.writeBigInt(out, entity.getId().getIndex());
          BigEndianWriter.writeText(out, orgDomainId);
          BigEndianWriter.writeText(out, entity.getIuv());
          BigEndianWriter.writeText(out, entity.getIur());
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
}
