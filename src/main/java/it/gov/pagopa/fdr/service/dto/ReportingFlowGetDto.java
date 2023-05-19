package it.gov.pagopa.fdr.service.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@JsonPropertyOrder({
  "status",
  "revision",
  "created",
  "updated",
  "reportingFlowName",
  "reportingFlowDate",
  "regulation",
  "regulationDate",
  "bicCodePouringBank",
  "sender",
  "receiver"
})
public class ReportingFlowGetDto {

  public Long revision;

  public Instant created;

  public Instant updated;

  public ReportingFlowStatusEnumDto status;

  private String reportingFlowName;

  private Instant reportingFlowDate;

  private SenderDto sender;

  private ReceiverDto receiver;

  private String regulation;

  private Instant regulationDate;

  private String bicCodePouringBank;

  public Long totPayments;

  public Double sumPaymnents;
}
