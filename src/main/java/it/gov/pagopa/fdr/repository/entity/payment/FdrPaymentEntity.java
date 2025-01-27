package it.gov.pagopa.fdr.repository.entity.payment;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.gov.pagopa.fdr.repository.enums.PaymentStatusEnum;
import it.gov.pagopa.fdr.util.error.exception.persistence.PersistenceFailureException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

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

  public static long deleteByFilter(String filterKey, Object filterValue)
      throws PersistenceFailureException {

    long deletedEntities = 0;

    try {
      MongoCollection<FdrPaymentEntity> collection = mongoCollection();

      boolean areThereMoreEntities = true;
      while (areThereMoreEntities) {
        List<ObjectId> idsToDelete =
            collection
                .find(Filters.eq(filterKey, filterValue))
                .limit(500)
                .map(document -> document.id)
                .into(new ArrayList<>());

        if (idsToDelete.isEmpty()) {
          areThereMoreEntities = false;
        } else {
          collection.deleteMany(Filters.in("_id", idsToDelete));
          deletedEntities += idsToDelete.size();
        }
      }
    } catch (Exception e) {
      throw new PersistenceFailureException(e);
    }

    return deletedEntities;
  }

  public static MongoCollection<FdrPaymentEntity> getCollection() {
    return mongoCollection();
  }
}
