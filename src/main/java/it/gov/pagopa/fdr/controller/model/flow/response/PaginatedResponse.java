package it.gov.pagopa.fdr.controller.model.flow.response;

import it.gov.pagopa.fdr.controller.model.common.Metadata;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@SuperBuilder
public class PaginatedResponse {

  @Schema(description = "The metadata related to the paginated response.")
  private Metadata metadata;

  @Schema(example = "100", description = "The number of elements that can be found in this page.")
  private long count;
}
