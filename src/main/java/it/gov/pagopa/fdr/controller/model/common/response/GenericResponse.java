package it.gov.pagopa.fdr.controller.model.common.response;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@Jacksonized
public class GenericResponse {

  @Schema(
      example = "Success",
      description =
          "The descriptive information that shows a message related to the executed operation.")
  private String message;
}
