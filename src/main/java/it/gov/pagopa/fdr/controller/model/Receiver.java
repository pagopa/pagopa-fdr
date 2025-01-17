package it.gov.pagopa.fdr.controller.model;

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
  @Schema(
      example = "APPBIT2B",
      description =
          "[XML FlussoRiversamento]=[istitutoRicevente.identificativoUnivocoRicevente.codiceIdentificativoUnivoco]")
  private String id;

  @NotNull
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(
      example = "20000000001",
      description = "[XML NodoInviaFlussoRendicontazione]=[identificativoDominio]")
  @JsonProperty(AppConstant.ORGANIZATION)
  private String organizationId;

  @NotNull
  @Pattern(regexp = "^(.{1,140})$")
  @Schema(
      example = "Comune di xyz",
      description = "[XML FlussoRiversamento]=[istitutoRicevente.denominazioneRicevente]")
  private String organizationName;
}
