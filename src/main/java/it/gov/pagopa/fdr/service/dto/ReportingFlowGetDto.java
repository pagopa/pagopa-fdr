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

  private Long revision;

  private Instant created;

  private Instant updated;

  private ReportingFlowStatusEnumDto status;

  private String reportingFlowName;

  private Instant reportingFlowDate;

  private SenderDto sender;

  private ReceiverDto receiver;

  private String regulation;

  private Instant regulationDate;

  private String bicCodePouringBank;

  private Long totPayments;

  private Double sumPaymnents;
}
