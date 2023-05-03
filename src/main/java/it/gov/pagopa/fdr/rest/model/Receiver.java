package it.gov.pagopa.fdr.rest.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@Jacksonized
public class Receiver {

  @NotNull
  @Pattern(regexp = "^\\w+$")
  @Schema(example = "APPBIT2B")
  private String id;

  @NotNull
  @Pattern(regexp = "^\\w+$")
  @Schema(example = "20000000001")
  private String ecId;

  @NotNull
  @Schema(example = "Comune di xyz")
  private String ecName;
}
