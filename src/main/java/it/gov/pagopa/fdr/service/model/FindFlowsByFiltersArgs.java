package it.gov.pagopa.fdr.service.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FindFlowsByFiltersArgs {

  private String pspId;
  private String organizationId;
  private String flowName;
  private Long revision;
  private Instant publishedGt;
  private long pageNumber;
  private long pageSize;
}
