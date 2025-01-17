package it.gov.pagopa.fdr.controller.model;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@Jacksonized
public class GenericResponse {

  @Schema(example = "Success")
  private String message;
}
