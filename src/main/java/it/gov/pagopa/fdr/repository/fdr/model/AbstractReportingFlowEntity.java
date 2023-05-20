package it.gov.pagopa.fdr.repository.fdr.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractReportingFlowEntity extends PanacheMongoEntity {
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

  @BsonProperty("sum_paymnents")
  private Double sumPaymnents;

  @BsonProperty("internal_ndp_read")
  private Boolean internalNdpRead;

  private Boolean read;
}
