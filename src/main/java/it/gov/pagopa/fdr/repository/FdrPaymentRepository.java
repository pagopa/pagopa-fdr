package it.gov.pagopa.fdr.repository;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import it.gov.pagopa.fdr.repository.common.Repository;
import it.gov.pagopa.fdr.repository.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.common.SortField;
import it.gov.pagopa.fdr.repository.entity.payment.FdrPaymentEntity;
import it.gov.pagopa.fdr.util.error.exception.persistence.PersistenceFailureException;
import it.gov.pagopa.fdr.util.error.exception.persistence.TransactionRollbackException;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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

    String query = "ref_fdr_sender_psp_id = :psp";
    Parameters params = new Parameters().and("psp", pspId);
    if (iuv != null) {
      query += " and iuv = :iuv";
      params.and("iuv", iuv);
    }
    if (iur != null) {
      query += " and iur = :iur";
      params.and("iur", iur);
    }
    if (createdFrom != null) {
      query += " and created >= :createdFrom";
      params.and("createdFrom", createdFrom);
    }
    if (createdTo != null) {
      query += " and created <= :createdTo";
      params.and("createdTo", createdTo);
    }

    Page page = Page.of(pageNumber - 1, pageSize);
    Sort sort = getSort(SortField.of("index", Direction.Ascending));

    PanacheQuery<FdrPaymentEntity> resultPage =
        FdrPaymentEntity.findPageByQuery(query, sort, params).page(page);
    return getPagedResult(resultPage);
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

      try {

        // first, start a new transaction
        session.startTransaction();
        MongoCollection<FdrPaymentEntity> collection = FdrPaymentEntity.getCollection();

        // add each entity in a bulk write, in order to save it as a single batch in transaction
        Instant now = Instant.now();
        List<WriteModel<FdrPaymentEntity>> bulkOperations = new ArrayList<>();
        for (FdrPaymentEntity entity : entityBatch) {
          entity.setTimestamp(now);
          bulkOperations.add(new InsertOneModel<>(entity));
        }
        collection.bulkWrite(session, bulkOperations);

        // finally, close and commit the transaction
        session.commitTransaction();

      } catch (Exception e) {

        // if the transaction is active, abort it
        if (session.hasActiveTransaction()) {
          session.abortTransaction();
        }
        throw new TransactionRollbackException(e);
      }
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

    return FdrPaymentEntity.deleteByFilter("ref_fdr.id", flowId);
  }

  public void deleteEntityInTransaction(List<FdrPaymentEntity> entityBatch)
      throws TransactionRollbackException {

    try (ClientSession session = this.mongoClient.startSession()) {

      try {

        // first, start a new transaction
        session.startTransaction();
        MongoCollection<FdrPaymentEntity> collection = FdrPaymentEntity.getCollection();

        // add each entity in a bulk write, in order to delete it as a single batch in transaction
        List<WriteModel<FdrPaymentEntity>> bulkOperations = new ArrayList<>();
        for (FdrPaymentEntity entity : entityBatch) {
          bulkOperations.add(new DeleteOneModel<>(Filters.eq("_id", entity.id)));
        }
        collection.bulkWrite(session, bulkOperations);

        // finally, close and commit the transaction
        session.commitTransaction();

      } catch (Exception e) {

        // if the transaction is active, abort it
        if (session.hasActiveTransaction()) {
          session.abortTransaction();
        }
        throw new TransactionRollbackException(e);
      }
    }
  }
}
