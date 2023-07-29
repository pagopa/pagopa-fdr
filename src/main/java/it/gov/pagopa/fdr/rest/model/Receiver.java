package it.gov.pagopa.fdr.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.fdr.util.AppConstant;
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
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(example = "APPBIT2B")
  private String id;

  @NotNull
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(example = "20000000001")
  @JsonProperty(AppConstant.EC)
  private String ecId;

  @NotNull
  @Pattern(regexp = "^(.{1,140})$")
  @Schema(example = "Comune di xyz")
  private String ecName;
}
