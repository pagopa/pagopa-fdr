package it.gov.pagopa.fdr.service.organizations.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportingFlowByIdEcDto {

  private MetadataDto metadata;

  private Long count;

  private List<String> data;
}
