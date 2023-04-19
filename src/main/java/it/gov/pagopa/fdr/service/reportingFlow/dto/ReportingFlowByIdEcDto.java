package it.gov.pagopa.fdr.service.reportingFlow.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportingFlowByIdEcDto {

  private MetadataDto metadata;

  private long count;

  private List<String> data;
}
