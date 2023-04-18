package it.gov.pagopa.fdr.service.reportingFlow.dto;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ReportingFlowDto {

  public String reportingFlow;
  public Instant dateReportingFlow;

  private SenderDto sender;
  private ReceiverDto receiver;

  private String regulation;
  private Instant dateRegulation;

  private Optional<String> bicCodePouringBank;

  private List<PagamentoDto> payments;
}