package it.gov.pagopa.fdr.service.reportingFlow.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ReportingFlowGetDto extends ReportingFlowDto {

  public String id;

  public ReportingFlowStatusEnumDto status;
}
