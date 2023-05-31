package it.gov.pagopa.fdr.repository.fdr;

import static it.gov.pagopa.fdr.repository.fdr.QueryConstants.BY_REFFLOWNAME_AND_REFPSPID;
import static it.gov.pagopa.fdr.util.AppConstant.FLOW_NAME;
import static it.gov.pagopa.fdr.util.AppConstant.PSP_ID;

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
@MongoEntity(collection = "fdr_payment_publish")
public class FdrPaymentPublishEntity extends PanacheMongoEntity {

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

  public static PanacheQuery<PanacheMongoEntityBase> findByFlowNameAndPspId(
      String reportingFlowName, String pspId, Sort sort) {
    return find(
        BY_REFFLOWNAME_AND_REFPSPID,
        sort,
        Parameters.with(FLOW_NAME, reportingFlowName).and(PSP_ID, pspId).map());
  }

  public static long deleteByFlowNameAndPspId(String reportingFlowName, String pspId) {
    return delete(
        BY_REFFLOWNAME_AND_REFPSPID,
        Parameters.with(FLOW_NAME, reportingFlowName).and(PSP_ID, pspId).map());
  }

  public static void persistFdrPaymentPublishEntities(
      List<FdrPaymentPublishEntity> fdrPaymentPublishEntities) {
    persist(fdrPaymentPublishEntities);
  }
}
