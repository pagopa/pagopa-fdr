package it.gov.pagopa.fdr.repository.fdr;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.gov.pagopa.fdr.repository.fdr.model.PaymentStatusEnumEntity;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

@Data
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "fdr_payment_history")
public class FdrPaymentHistoryEntity extends PanacheMongoEntity {

  private Long revision;

  private Instant created;

  private Instant updated;

  private String iuv;
  private String iur;

  private Long index;
  private Double pay;

  @BsonProperty("pay_status")
  private PaymentStatusEnumEntity payStatus;

  @BsonProperty("pay_date")
  private Instant payDate;

  @BsonProperty("ref_fdr_id")
  private ObjectId refFdrId;

  @BsonProperty("ref_fdr_reporting_flow_name")
  private String refFdrReportingFlowName;

  @BsonProperty("ref_fdr_reporting_sender_psp_id")
  private String refFdrReportingSenderPspId;

  @BsonProperty("ref_fdr_revision")
  private Long refFdrRevision;

  public static void persistFdrPaymentHistoryEntities(
      List<FdrPaymentHistoryEntity> fdrPaymentHistoryEntities) {
    persist(fdrPaymentHistoryEntities);
  }

  public static PanacheQuery<PanacheMongoEntityBase> findByFlowNameAndRevAndPspId(
      String reportingFlowName, Long rev, String pspId, Sort sort) {
    return find(
        "ref_fdr_reporting_flow_name = :flowName and ref_fdr_revision = :rev and"
            + " ref_fdr_reporting_sender_psp_id = :pspId",
        sort,
        Parameters.with("flowName", reportingFlowName).and("rev", rev).and("pspId", pspId).map());
  }
}
