package it.gov.pagopa.fdr.controller.model.common;

import lombok.Builder;
import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
public class Metadata {

  @Schema(example = "25", description = "The size of the current page shown in response")
  private int pageSize;

  @Schema(example = "1", description = "The index of the current page shown in response")
  private int pageNumber;

  @Schema(
      example = "3",
      description =
          "The total number of the pages that can be retrieved in order to show all elements.")
  private int totPage;
}
