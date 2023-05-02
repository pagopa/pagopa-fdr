package it.gov.pagopa.fdr.service.psps.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportingFlowDto {

  private String reportingFlowName;

  private Instant reportingFlowDate;

  private SenderDto sender;

  private ReceiverDto receiver;

  private String regulation;

  private Instant regulationDate;

  private String bicCodePouringBank;
}
