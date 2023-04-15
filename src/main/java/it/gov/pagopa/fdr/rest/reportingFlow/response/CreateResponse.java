package it.gov.pagopa.fdr.rest.reportingFlow.response;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@Jacksonized
public class CreateResponse {

  @Schema(example = "643accaa4733f71aea4c71bf")
  public String id;
}
