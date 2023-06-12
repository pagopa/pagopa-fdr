package it.gov.pagopa.fdr.repository.fdr;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.gov.pagopa.fdr.repository.fdr.model.ReceiverEntity;
import it.gov.pagopa.fdr.repository.fdr.model.ReportingFlowStatusEnumEntity;
import it.gov.pagopa.fdr.repository.fdr.model.SenderEntity;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "fdr_publish")
public class FdrPublishEntity extends PanacheMongoEntity {

  private Long revision;

  private Instant created;

  private Instant updated;

  @BsonProperty("reporting_flow_name")
  private String reportingFlowName;

  @BsonProperty("reporting_flow_date")
  private Instant reportingFlowDate;

  private SenderEntity sender;

  private ReceiverEntity receiver;

  private String regulation;

  @BsonProperty("regulation_date")
  private Instant regulationDate;

  @BsonProperty("bic_code_pouring_bank")
  private String bicCodePouringBank;

  private ReportingFlowStatusEnumEntity status;

  @BsonProperty("tot_payments")
  private Long totPayments;

  @BsonProperty("sum_payments")
  private Double sumPayments;

  @BsonProperty("internal_ndp_read")
  private Boolean internalNdpRead;

  private Boolean read;

  public static PanacheQuery<PanacheMongoEntityBase> findByFlowNameAndPspId(
      String reportingFlowName, String pspId) {
    return find(
        "reporting_flow_name = :flowName and sender.psp_id = :pspId",
        Parameters.with("flowName", reportingFlowName).and("pspId", pspId).map());
  }

  public static PanacheQuery<FdrPublishEntity> findByEcIdAndPspId(
      String ecId, String pspId, Sort sort) {
    return find(
        "receiver.ec_id = :ecId and sender.psp_id = :pspId",
        sort,
        Parameters.with("ecId", ecId).and("pspId", pspId).map());
  }

  public static PanacheQuery<FdrPublishEntity> findByEcId(String ecId, Sort sort) {
    return find("receiver.ec_id = :ecId", sort, Parameters.with("ecId", ecId).map());
  }

  public static PanacheQuery<FdrPublishEntity> findByInternalRead(
      Boolean internalNdpRead, Sort sort) {
    return find(
        "receiver.internal_ndp_read = :internalRead",
        sort,
        Parameters.with("internalRead", internalNdpRead).map());
  }

  public static long deleteByFlowNameAndPspId(String reportingFlowName, String pspId) {
    return delete(
        "reporting_flow_name = :flowName and sender.psp_id = :pspId",
        Parameters.with("flowName", reportingFlowName).and("pspId", pspId).map());
  }

  public void persistEntity() {
    persist();
  }
}
