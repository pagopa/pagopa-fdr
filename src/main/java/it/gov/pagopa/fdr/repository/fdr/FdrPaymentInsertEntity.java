package it.gov.pagopa.fdr.repository.fdr;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.panache.common.Parameters;
import it.gov.pagopa.fdr.repository.fdr.model.PaymentStatusEnumEntity;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

@Data
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "fdr_payment_insert")
public class FdrPaymentInsertEntity extends PanacheMongoEntity {

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

  public static PanacheQuery<PanacheMongoEntityBase> findByFlowNameAndIndexes(
      String reportingFlowName, List<Long> indexList) {
    return find(
        "ref_fdr_reporting_flow_name = :flowName and index in :indexes",
        Parameters.with("flowName", reportingFlowName).and("indexes", indexList).map());
  }

  public static PanacheQuery<PanacheMongoEntityBase> findByFlowNameAndPspId(
      String reportingFlowName, String pspId) {
    return find(
        "ref_fdr_reporting_flow_name = :flowName and ref_fdr_reporting_sender_psp_id = :pspId",
        Parameters.with("flowName", reportingFlowName).and("pspId", pspId).map());
  }

  public static long deleteByFlowNameAndIndexes(String reportingFlowName, List<Long> indexList) {
    return delete(
        "ref_fdr_reporting_flow_name = :flowName and index in :indexes",
        Parameters.with("flowName", reportingFlowName).and("indexes", indexList).map());
  }

  public static long deleteByFlowNameAndPspId(String reportingFlowName, String pspId) {
    return delete(
        "ref_fdr_reporting_flow_name = :flowName and ref_fdr_reporting_sender_psp_id = :pspId",
        Parameters.with("flowName", reportingFlowName).and("pspId", pspId).map());
  }

  public static long deleteByFlowName(String reportingFlowName) {
    return delete(
        "ref_fdr_reporting_flow_name = :flowName",
        Parameters.with("flowName", reportingFlowName).map());
  }

  public static void persistFdrPaymentsInsert(
      List<FdrPaymentInsertEntity> fdrPaymentInsertEntityList) {
    persist(fdrPaymentInsertEntityList);
  }
}
