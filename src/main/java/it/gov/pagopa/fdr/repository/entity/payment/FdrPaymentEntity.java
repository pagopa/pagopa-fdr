package it.gov.pagopa.fdr.repository.entity.payment;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.gov.pagopa.fdr.repository.enums.PaymentStatusEnum;
import it.gov.pagopa.fdr.repository.exception.TransactionRollbackException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.faulttolerance.Retry;

@Data
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "fdr_payment")
public class FdrPaymentEntity extends PanacheMongoEntity {

  private String iuv;

  private String iur;

  private Long index;

  private Double amount;

  @BsonProperty("pay_status")
  private PaymentStatusEnum payStatus;

  @BsonProperty("pay_date")
  private Instant payDate;

  @BsonProperty("transfer_id")
  private Long transferId;

  private Instant created;

  private Instant updated;

  @BsonProperty("ref_fdr")
  private ReferencedFdrEntity refFdr;

  @BsonProperty("_ts")
  public Instant timestamp;

  public static PanacheQuery<PanacheMongoEntityBase> findPageByQuery(
      String query, Sort sort, Parameters parameters) {
    return find(query, sort, parameters.map());
  }

  public static long countByQuery(String query, Parameters parameters) {
    return count(query, parameters.map());
  }

  public static void persistBulkInTransaction(
      ClientSession session, Iterable<FdrPaymentEntity> entityBatch)
      throws TransactionRollbackException {

    try {
      Instant now = Instant.now();
      session.startTransaction();
      MongoCollection<FdrPaymentEntity> collection = mongoCollection();

      List<WriteModel<FdrPaymentEntity>> bulkOperations = new ArrayList<>();
      for (FdrPaymentEntity entity : entityBatch) {
        entity.setTimestamp(now);
        bulkOperations.add(new InsertOneModel<>(entity));
      }
      collection.bulkWrite(session, bulkOperations);

      session.commitTransaction();

    } catch (Exception e) {

      if (session.hasActiveTransaction()) {
        session.abortTransaction();
      }
      throw new TransactionRollbackException(e);
    }
  }

  public static void persistFdrPaymentHistoryEntities(
      List<FdrPaymentEntity> fdrPaymentHistoryEntities) {
    persist(fdrPaymentHistoryEntities);
  }

  public static PanacheQuery<PanacheMongoEntityBase> findByFdrAndIndexes(
      ObjectId fdrId, List<Long> indexList) {
    return find(
        "ref_fdr.id = :fdrId and index in :indexes",
        Parameters.with("fdrId", fdrId).and("indexes", indexList).map());
  }

  public static PanacheQuery<PanacheMongoEntityBase> findByFdrAndPspId(String fdr, String pspId) {
    return find(
        "ref_fdr = :fdr and ref_fdr_sender_psp_id = :pspId",
        Parameters.with("fdr", fdr).and("pspId", pspId).map());
  }

  // https://quarkus.io/guides/smallrye-fault-tolerance
  @Retry(delay = 500, delayUnit = ChronoUnit.MILLIS)
  public static long deleteByFdrAndIndexes(String fdr, List<Long> indexList) {
    return delete(
        "ref_fdr = :fdr and index in :indexes",
        Parameters.with("fdr", fdr).and("indexes", indexList).map());
  }

  public static long deleteByFdrAndPspId(String fdr, String pspId) {
    return delete(
        "ref_fdr = :fdr and ref_fdr_sender_psp_id = :pspId",
        Parameters.with("fdr", fdr).and("pspId", pspId).map());
  }

  public static long deleteByFdr(String fdr) {
    return delete("ref_fdr = :fdr", Parameters.with("fdr", fdr).map());
  }

  public static void persistFdrPaymentsInsert(
      List<FdrPaymentInsertEntity> fdrPaymentInsertEntityList) {
    persist(fdrPaymentInsertEntityList);
  }

  public static PanacheQuery<PanacheMongoEntityBase> findByFdrAndPspIAndOrganizationIdSort(
      String fdr, String pspId, String organizationId, Sort sort) {
    return find(
        "ref_fdr = :fdr and ref_fdr_sender_psp_id = :pspId and ref_fdr_receiver_organization_id ="
            + " :organizationId",
        sort,
        Parameters.with("fdr", fdr)
            .and("pspId", pspId)
            .and("organizationId", organizationId)
            .map());
  }

  public static PanacheQuery<PanacheMongoEntityBase> findByFdrAndRevAndPspIdAndOrganizationId(
      String fdr, Long rev, String pspId, String organizationId, Sort sort) {
    return find(
        "ref_fdr = :fdr and ref_fdr_revision = :rev and ref_fdr_sender_psp_id = :pspId and"
            + " ref_fdr_receiver_organization_id = :organizationId",
        sort,
        Parameters.with("fdr", fdr)
            .and("rev", rev)
            .and("pspId", pspId)
            .and("organizationId", organizationId)
            .map());
  }

  public static PanacheQuery<PanacheMongoEntityBase> executeQueryByPspIuvAndIur(
      String psp, String iuv, String iur, Instant createdFrom, Instant createdTo, Sort sort) {
    String query = "ref_fdr_sender_psp_id = :psp";
    Parameters params = new Parameters().and("psp", psp);
    if (iuv != null && iur != null) {
      query += " and iuv = :iuv and iur = :iur";
      params.and("iuv", iuv).and("iur", iur);
    } else if (iuv != null) {
      query += " and iuv = :iuv";
      params.and("iuv", iuv);
    } else if (iur != null) {
      query += " and iur = :iur";
      params.and("iur", iur);
    }
    if (createdFrom != null && createdTo != null) {
      query += " and created >= :createdFrom and created <= :createdTo";
      params.and("createdFrom", createdFrom).and("createdTo", createdTo);
    } else if (createdFrom != null) {
      query += " and created >= :createdFrom";
      params.and("createdFrom", createdFrom);
    } else if (createdTo != null) {
      query += " and created <= :createdTo";
      params.and("createdTo", createdTo);
    }
    return find(query, sort, params);
  }

  public static void persistFdrPaymentPublishEntities(
      List<FdrPaymentEntity> fdrPaymentPublishEntities) {
    persist(fdrPaymentPublishEntities);
  }
}
