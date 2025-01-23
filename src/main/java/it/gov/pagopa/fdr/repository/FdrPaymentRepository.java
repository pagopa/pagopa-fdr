package it.gov.pagopa.fdr.repository;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import it.gov.pagopa.fdr.repository.common.Repository;
import it.gov.pagopa.fdr.repository.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.common.SortField;
import it.gov.pagopa.fdr.repository.entity.payment.FdrPaymentEntity;
import it.gov.pagopa.fdr.repository.exception.PersistenceFailureException;
import it.gov.pagopa.fdr.repository.exception.TransactionRollbackException;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.faulttolerance.Retry;

@ApplicationScoped
public class FdrPaymentRepository extends Repository {

  public static final String QUERY_GET_BY_FLOW_OBJID = "ref_fdr.id = :flowObjId";

  public static final String QUERY_GET_BY_FLOW_OBJID_AND_INDEXES =
      "ref_fdr.id = :flowObjId" + " and index in :indexes";

  private final MongoClient mongoClient;

  public FdrPaymentRepository(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }

  public RepositoryPagedResult<FdrPaymentEntity> executeQueryByPspAndIuvAndIur(
      String pspId,
      String iuv,
      String iur,
      Instant createdFrom,
      Instant createdTo,
      int pageNumber,
      int pageSize) {

    Page page = Page.of(pageNumber - 1, pageSize);
    Sort sort = getSort(SortField.of("index", Direction.Ascending));

    PanacheQuery<FdrPaymentEntity> query =
        FdrPaymentEntity.executeQueryByPspIuvAndIur(pspId, iuv, iur, createdFrom, createdTo, sort)
            .page(page);
    return getPagedResult(query);
  }

  public RepositoryPagedResult<FdrPaymentEntity> findByFlowObjectId(
      ObjectId flowId, int pageNumber, int pageSize) {

    // defining query with mandatory fields
    Parameters parameters = new Parameters();
    parameters.and("flowObjId", flowId);

    Page page = Page.of(pageNumber - 1, pageSize);
    Sort sort = getSort(SortField.of("index", Direction.Ascending));

    PanacheQuery<FdrPaymentEntity> resultPage =
        FdrPaymentEntity.findPageByQuery(
                FdrPaymentRepository.QUERY_GET_BY_FLOW_OBJID, sort, parameters)
            .page(page);
    return getPagedResult(resultPage);
  }

  public Long countByFlowObjectIdAndIndexes(ObjectId flowId, Set<Long> indexes) {

    // defining query with mandatory fields
    Parameters parameters = new Parameters();
    parameters.and("flowObjId", flowId);
    parameters.and("indexes", indexes);

    return FdrPaymentEntity.countByQuery(
        FdrPaymentRepository.QUERY_GET_BY_FLOW_OBJID_AND_INDEXES, parameters);
  }

  public List<FdrPaymentEntity> findByFlowObjectIdAndIndexes(ObjectId flowId, Set<Long> indexes) {

    // defining query with mandatory fields
    Parameters parameters = new Parameters();
    parameters.and("flowObjId", flowId);
    parameters.and("indexes", indexes);

    Page page = Page.of(0, indexes.size());
    Sort sort = getSort(SortField.of("index", Direction.Ascending));

    PanacheQuery<FdrPaymentEntity> resultPage =
        FdrPaymentEntity.findPageByQuery(
                FdrPaymentRepository.QUERY_GET_BY_FLOW_OBJID_AND_INDEXES, sort, parameters)
            .page(page);
    return resultPage.list();
  }

  public void createEntityInTransaction(List<FdrPaymentEntity> entityBatch)
      throws TransactionRollbackException {

    try (ClientSession session = this.mongoClient.startSession()) {
      FdrPaymentEntity.persistBulkInTransaction(session, entityBatch);
    }
  }

  // https://quarkus.io/guides/smallrye-fault-tolerance
  @Retry(
      delay = 1000,
      maxRetries = -1,
      maxDuration = 1,
      durationUnit = ChronoUnit.MINUTES,
      retryOn = PersistenceFailureException.class)
  public long deleteByFlowObjectId(ObjectId flowId) throws PersistenceFailureException {

    System.out.print("[AD] executing deleteByFilter try... ");
    return FdrPaymentEntity.deleteByFilter("ref_fdr.id", flowId);
  }

  public void deleteEntityInTransaction(List<FdrPaymentEntity> entityBatch)
      throws TransactionRollbackException {

    try (ClientSession session = this.mongoClient.startSession()) {
      FdrPaymentEntity.deleteBulkInTransaction(session, entityBatch);
    }
  }
}
