package it.gov.pagopa.fdr.controller.model.flow.response;

import it.gov.pagopa.fdr.controller.model.common.Metadata;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@SuperBuilder
public class PaginatedResponse {

  private Metadata metadata;

  @Schema(example = "100")
  private long count;
}
