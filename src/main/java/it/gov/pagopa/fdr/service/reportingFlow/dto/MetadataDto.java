package it.gov.pagopa.fdr.service.reportingFlow.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MetadataDto {

  private int pageSize;

  private int pageNumber;

  private int totPage;

  private List<String> sortColumn;
}