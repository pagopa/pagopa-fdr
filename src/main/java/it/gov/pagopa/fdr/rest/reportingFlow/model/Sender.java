package it.gov.pagopa.fdr.rest.reportingFlow.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@Jacksonized
public class Sender {

  @NotNull private TipoIdentificativoUnivocoEnum type;

  @NotNull
  @Pattern(regexp = "^\\w+$")
  @Schema(example = "SELBIT2B")
  private String id;

  @NotNull
  @Pattern(regexp = "^\\w+$")
  @Schema(example = "60000000001")
  private String idPsp;

  @NotNull
  @Pattern(regexp = "^\\S+$")
  @Schema(example = "Bank")
  private String namePsp;

  @NotNull
  @Pattern(regexp = "^\\w+$")
  @Schema(example = "70000000001")
  private String idBroker;

  @NotNull
  @Pattern(regexp = "^\\w+$")
  @Schema(example = "80000000001")
  private String idChannel;

  @Pattern(regexp = "^\\S+$")
  @Schema(example = "1234567890", deprecated = true)
  private String password;
}
