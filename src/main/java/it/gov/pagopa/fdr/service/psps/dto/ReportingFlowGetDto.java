package it.gov.pagopa.fdr.service.psps.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
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
