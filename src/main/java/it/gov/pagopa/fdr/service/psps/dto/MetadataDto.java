package it.gov.pagopa.fdr.service.psps.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MetadataDto {

  private long pageSize;

  private long pageNumber;

  private long totPage;
}
