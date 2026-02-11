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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.Session;
import org.jboss.logging.Logger;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;

@ApplicationScoped
public class PaymentRepository extends Repository implements PanacheRepository<PaymentEntity> {

  @ConfigProperty(name = "payments.batch.size")
  Integer batchSize;

  public static final String INDEX = "index";
  private final EntityManager entityManager;

  private final Logger log;

  public static final String QUERY_GET_BY_FLOW_ID = "flowId = ?1";

  public static final String QUERY_DELETE_BY_ID = "id in ?1";

  public static final String QUERY_GET_BY_FLOW_ID_AND_INDEXES = "flowId = ?1" + " and index in ?2";

  public static final String INSERT_IN_BULK =
      "INSERT INTO payment (flow_id, iuv, iur, index, amount, pay_date, pay_status, transfer_id,"
          + " created, updated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  private static final String COPY_PAYMENT_SQL =
      "COPY payment (flow_id, iuv, iur, \"index\", amount, pay_date, pay_status, transfer_id, created, updated) FROM STDIN WITH (FORMAT CSV)";

  private static final DateTimeFormatter COPY_TIMESTAMP_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

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
      query.append(" and iuv = :iuv");
      params.and("iuv", iuv);
    }
    if (iur != null) {
      query.append(" and iur = :iur");
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

    Page page = Page.of(pageNumber - 1, pageSize);
    Sort sort = getSort(SortField.of(INDEX, Direction.Ascending));

    PanacheQuery<PaymentEntity> resultPage =
        PaymentEntity.findPageByQuery(query.toString(), sort, params).page(page);
    return getPagedResult(resultPage);
  }

  public long countByFlowIdAndIndexes(Long flowId, Set<Long> indexes) {

    return count(QUERY_GET_BY_FLOW_ID_AND_INDEXES, flowId, indexes);
  }

  @Timed(
      value = "paymentRepository.createEntityInBulk.task",
      description = "Time taken to perform createEntityInBulk",
      percentiles = 0.95,
      histogram = true)
  public void createEntityInBulk(List<PaymentEntity> entityBatch) throws SQLException {

    Session session = entityManager.unwrap(Session.class);

    try (PreparedStatement preparedStatement =
        session.doReturningWork(connection -> connection.prepareStatement(INSERT_IN_BULK))) {

      int count = 0;
      for (PaymentEntity payment : entityBatch) {
        payment.exportInPreparedStatement(preparedStatement);
        preparedStatement.addBatch();
        count++;

        if (count % batchSize == 0) {
          preparedStatement.executeBatch();
        }
      }
      preparedStatement.executeBatch();
    } catch (SQLException e) {
      log.error("An error occurred while executing payments bulk insert", e);
      throw e;
    }
  }

  @Timed(
      value = "paymentRepository.createEntityInBulkCopy.task",
      description = "Time taken to perform createEntityInBulkCopy",
      percentiles = 0.95,
      histogram = true)
  public void createEntityInBulkCopy(List<PaymentEntity> entityBatch) throws SQLException {
    Session session = entityManager.unwrap(Session.class);
    try {
      session.doWork(
          connection -> {
            PGConnection pgConnection = connection.unwrap(PGConnection.class);
            CopyManager copyManager = pgConnection.getCopyAPI();
            byte[] csvPayload = buildCsvPayload(entityBatch);
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(csvPayload)) {
              copyManager.copyIn(COPY_PAYMENT_SQL, inputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
          });
    } catch (RuntimeException e) {
      log.error("An error occurred while executing payments bulk insert via COPY", e);
      throw e;
    }
  }

  private byte[] buildCsvPayload(List<PaymentEntity> entityBatch) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    for (PaymentEntity payment : entityBatch) {
      String[] fields = {
        toCsvField(payment.getFlowId()),
        toCsvField(payment.getIuv()),
        toCsvField(payment.getIur()),
        toCsvField(payment.getIndex()),
        toCsvField(payment.getAmount()),
        toCsvField(formatInstant(payment.getPayDate())),
        toCsvField(payment.getPayStatus()),
        toCsvField(payment.getTransferId()),
        toCsvField(formatInstant(payment.getCreated())),
        toCsvField(formatInstant(payment.getUpdated()))
      };
      String row = String.join(",", fields) + "\n";
      out.writeBytes(row.getBytes(StandardCharsets.UTF_8));
    }
    return out.toByteArray();
  }

  private String toCsvField(Object value) {
    if (value == null) {
      return "";
    }
    if (value instanceof String) {
      return escapeCsv((String) value);
    }
    return value.toString();
  }

  private String formatInstant(Instant instant) {
    if (instant == null) {
      return "";
    }
    return COPY_TIMESTAMP_FORMATTER.format(instant.atZone(ZoneId.systemDefault()));
  }

  private String escapeCsv(String value) {
    if (value == null || value.isEmpty()) {
      return value == null ? "" : value;
    }
    boolean needsQuoting =
        value.contains(",")
            || value.contains("\"")
            || value.contains("\n")
            || value.contains("\r")
            || value.contains("\\");
    String escaped = value.replace("\\", "\\\\").replace("\"", "\"\"");
    if (needsQuoting) {
      return "\"" + escaped + "\"";
    }
    return escaped;
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

    Set<Long> ids = entityBatch.stream().map(PaymentEntity::getId).collect(Collectors.toSet());
    delete(QUERY_DELETE_BY_ID, ids);
  }
}
