package it.gov.pagopa.fdr.service.model.arguments;

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
  private Instant createdGt;
  private Instant publishedGt;
  private Instant flowDate;
  private long pageNumber;
  private long pageSize;
}
