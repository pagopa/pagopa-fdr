package it.gov.pagopa.fdr.repository.fdr;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.panache.common.Parameters;
import it.gov.pagopa.fdr.repository.fdr.model.FdrStatusEnumEntity;
import it.gov.pagopa.fdr.repository.fdr.model.ReceiverEntity;
import it.gov.pagopa.fdr.repository.fdr.model.SenderEntity;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "fdr_insert")
public class FdrInsertEntity extends PanacheMongoEntity {

  private Long revision;

  private Instant created;

  private Instant updated;

  private String fdr;

  @BsonProperty("fdr_date")
  private Instant fdrDate;

  private SenderEntity sender;

  private ReceiverEntity receiver;

  private String regulation;

  @BsonProperty("regulation_date")
  private Instant regulationDate;

  @BsonProperty("bic_code_pouring_bank")
  private String bicCodePouringBank;

  private FdrStatusEnumEntity status;

  @BsonProperty("computed_tot_payments")
  private Long computedTotPayments;

  @BsonProperty("computed_sum_payments")
  private Double computedSumPayments;

  @BsonProperty("tot_payments")
  private Long totPayments;

  @BsonProperty("sum_payments")
  private Double sumPayments;

  public static PanacheQuery<PanacheMongoEntityBase> findByFdrAndPspId(String fdr, String pspId) {
    return find(
        "fdr = :fdr and sender.psp_id = :pspId",
        Parameters.with("fdr", fdr).and("pspId", pspId).map());
  }

  public static PanacheQuery<PanacheMongoEntityBase> findByFdrAndRevAndPspId(
      String fdr, String pspId) {
    return find(
        "fdr = :fdr and sender.psp_id = :pspId",
        Parameters.with("fdr", fdr).and("pspId", pspId).map());
  }
}
