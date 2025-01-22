package it.gov.pagopa.fdr.repository.entity.payment;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.DeleteManyModel;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.Filters;
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
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonProperty;

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

  public static long deleteByQuery(String query, Parameters parameters) {
    return delete(query, parameters.map());
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

  public static void deleteBulkInTransaction(
      ClientSession session, Iterable<FdrPaymentEntity> entityBatch)
      throws TransactionRollbackException {

    try {
      session.startTransaction();
      MongoCollection<FdrPaymentEntity> collection = mongoCollection();

      List<WriteModel<FdrPaymentEntity>> bulkOperations = new ArrayList<>();
      for (FdrPaymentEntity entity : entityBatch) {
        bulkOperations.add(new DeleteOneModel<>(Filters.eq("_id", entity.id)));
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

  public static void deleteBulkInTransaction(
      ClientSession session, String filterKey, Object filterValue)
      throws TransactionRollbackException {

    try {
      session.startTransaction();
      MongoCollection<FdrPaymentEntity> collection = mongoCollection();

      List<WriteModel<FdrPaymentEntity>> bulkOperations = new ArrayList<>();
      bulkOperations.add(new DeleteManyModel<>(Filters.eq(filterKey, filterValue)));
      collection.bulkWrite(session, bulkOperations);

      session.commitTransaction();

    } catch (Exception e) {

      if (session.hasActiveTransaction()) {
        session.abortTransaction();
      }
      throw new TransactionRollbackException(e);
    }
  }

  public static PanacheQuery<PanacheMongoEntityBase> executeQueryByPspIuvAndIur(
      String psp, String iuv, String iur, Instant createdFrom, Instant createdTo, Sort sort) {
    String query = "ref_fdr_sender_psp_id = :psp";
    Parameters params = new Parameters().and("psp", psp);
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
    return find(query, sort, params);
  }
}
