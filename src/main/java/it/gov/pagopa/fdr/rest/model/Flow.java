package it.gov.pagopa.fdr.rest.model;

import lombok.Builder;
import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
public class Flow {

  @Schema(example = "AAABBB")
  private String name;

  @Schema(example = "1")
  private String pspId;
}
