package it.gov.pagopa.fdr.repository.fdr;

import static it.gov.pagopa.fdr.repository.fdr.QueryConstants.BY_ECID;
import static it.gov.pagopa.fdr.repository.fdr.QueryConstants.BY_ECID_AND_PSPID;
import static it.gov.pagopa.fdr.repository.fdr.QueryConstants.BY_FLOWNAME_AND_PSPID;
import static it.gov.pagopa.fdr.repository.fdr.QueryConstants.BY_INTERNAL_NDP_READ;
import static it.gov.pagopa.fdr.util.AppConstant.EC_ID;
import static it.gov.pagopa.fdr.util.AppConstant.FLOW_NAME;
import static it.gov.pagopa.fdr.util.AppConstant.INTERNAL_READ;
import static it.gov.pagopa.fdr.util.AppConstant.PSP_ID;

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
        BY_FLOWNAME_AND_PSPID,
        Parameters.with(FLOW_NAME, reportingFlowName).and(PSP_ID, pspId).map());
  }

  public static PanacheQuery<FdrPublishEntity> findByEcIdAndPspId(
      String ecId, String pspId, Sort sort) {
    return find(BY_ECID_AND_PSPID, sort, Parameters.with(EC_ID, ecId).and(PSP_ID, pspId).map());
  }

  public static PanacheQuery<FdrPublishEntity> findByEcId(String ecId, Sort sort) {
    return find(BY_ECID, sort, Parameters.with(EC_ID, ecId).map());
  }

  public static PanacheQuery<FdrPublishEntity> findByInternalRead(
      Boolean internalNdpRead, Sort sort) {
    return find(BY_INTERNAL_NDP_READ, sort, Parameters.with(INTERNAL_READ, internalNdpRead).map());
  }

  public static long deleteByFlowNameAndPspId(String reportingFlowName, String pspId) {
    return delete(
        BY_FLOWNAME_AND_PSPID,
        Parameters.with(FLOW_NAME, reportingFlowName).and(PSP_ID, pspId).map());
  }

  public void persistEntity() {
    persist();
  }
}
