package it.gov.pagopa.fdr.repository;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import it.gov.pagopa.fdr.repository.entity.common.Repository;
import it.gov.pagopa.fdr.repository.entity.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.payment.FdrPaymentEntity;
import it.gov.pagopa.fdr.repository.exception.TransactionRollbackException;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;

@ApplicationScoped
public class FdrPaymentRepository extends Repository {

  public static final String QUERY_GET_BY_FLOW_OBJID = "ref_fdr.id = :flowObjId";

  public static final String QUERY_GET_BY_FLOW_OBJID_AND_INDEXES = "ref_fdr.id = :flowObjId";

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
    Sort sort = getSort(Pair.of("index", Direction.Ascending));

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
    Sort sort = getSort(Pair.of("index", Direction.Ascending));

    PanacheQuery<FdrPaymentEntity> resultPage =
        FdrPaymentEntity.findPageByQuery(
                FdrPaymentRepository.QUERY_GET_BY_FLOW_OBJID, sort, parameters)
            .page(page);
    return getPagedResult(resultPage);
  }

  public Long countByFlowAndIndexes(ObjectId flowId, Set<Long> indexes) {

    // defining query with mandatory fields
    Parameters parameters = new Parameters();
    parameters.and("flowObjId", flowId);
    parameters.and("indexes", indexes);

    return FdrPaymentEntity.countByQuery(
        FdrPaymentRepository.QUERY_GET_BY_FLOW_OBJID_AND_INDEXES, parameters);
  }

  public void createEntityInTransaction(List<FdrPaymentEntity> entityBatch)
      throws TransactionRollbackException {

    try (ClientSession session = this.mongoClient.startSession()) {
      FdrPaymentEntity.persistBulkInTransaction(session, entityBatch);
    }
  }
}
