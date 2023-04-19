package it.gov.pagopa.fdr.rest.reportingFlow.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
public class Metadata {

  @Schema(example = "25")
  private int pageSize;

  @Schema(example = "1")
  private int pageNumber;

  @Schema(example = "3")
  private int totPage;

  @Schema(example = "name,lastname")
  private List<String> sortColumn;
}