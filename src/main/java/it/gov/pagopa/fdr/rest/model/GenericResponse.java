package it.gov.pagopa.fdr.rest.model;

import lombok.Builder;
import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
public class GenericResponse {

  @Schema(example = "Success")
  private String message;
}