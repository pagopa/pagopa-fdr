package it.gov.pagopa.fdr.repository;

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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.Session;
import org.jboss.logging.Logger;

@ApplicationScoped
public class PaymentRepository extends Repository implements PanacheRepository<PaymentEntity> {

  private EntityManager entityManager;

  private Logger log;

  public static final String QUERY_GET_BY_FLOW_ID = "flowId = ?1";

  public static final String QUERY_DELETE_BY_ID = "id in ?1";

  public static final String QUERY_GET_BY_FLOW_ID_AND_INDEXES = "flowId = ?1" + " and index in ?2";

  public static final String INSERT_IN_BULK =
      "INSERT INTO payment (flow_id, iuv, iur, index, amount, pay_date, pay_status, transfer_id,"
          + " created, updated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
      int pageNumber,
      int pageSize) {

    StringBuilder query =
        new StringBuilder(
            "SELECT p FROM PaymentEntity p LEFT JOIN FETCH p.flow WHERE p.flow.senderPspId = :psp");
    Parameters params = new Parameters().and("psp", pspId);
    if (iuv != null) {
      query.append(" and iuv = :iuv");
      params.and("iuv", iuv);
    }
    if (iur != null) {
      query.append(" and iur = :iur");
      params.and("iur", iur);
    }
    if (createdFrom != null) {
      query.append(" and created >= :createdFrom");
      params.and("createdFrom", createdFrom);
    }
    if (createdTo != null) {
      query.append(" and created <= :createdTo");
      params.and("createdTo", createdTo);
    }

    Page page = Page.of(pageNumber - 1, pageSize);
    Sort sort = getSort(SortField.of("index", Direction.Ascending));

    PanacheQuery<PaymentEntity> resultPage =
        PaymentEntity.findPageByQuery(query.toString(), sort, params).page(page);
    return getPagedResult(resultPage);
  }

  public long countByFlowIdAndIndexes(Long flowId, Set<Long> indexes) {

    return count(QUERY_GET_BY_FLOW_ID_AND_INDEXES, flowId, indexes);
  }

  public void createEntityInBulk(List<PaymentEntity> entityBatch) throws SQLException {

    Session session = entityManager.unwrap(Session.class);

    try (PreparedStatement preparedStatement =
        session.doReturningWork(connection -> connection.prepareStatement(INSERT_IN_BULK))) {

      for (PaymentEntity payment : entityBatch) {
        payment.exportInPreparedStatement(preparedStatement);
        preparedStatement.addBatch();
      }
      preparedStatement.executeBatch();

    } catch (SQLException e) {

      log.error("An error occurred while executing payments bulk insert", e);
      throw e;
    }
  }

  public RepositoryPagedResult<PaymentEntity> findByFlowId(
      Long flowId, int pageNumber, int pageSize) {

    Page page = Page.of(pageNumber - 1, pageSize);
    Sort sort = getSort(SortField.of("index", Direction.Ascending));

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
