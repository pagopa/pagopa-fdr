package it.gov.pagopa.fdr.service.reportingFlow.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportingFlowDto {

  public String reportingFlow;
  public Instant dateReportingFlow;

  private SenderDto sender;
  private ReceiverDto receiver;

  private String regulation;
  private Instant dateRegulation;

  private String bicCodePouringBank;
}
