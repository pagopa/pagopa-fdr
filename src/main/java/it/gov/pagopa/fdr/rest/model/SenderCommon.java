package it.gov.pagopa.fdr.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.fdr.util.AppConstant;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@SuperBuilder
@Jacksonized
public class SenderCommon {

  @NotNull
  @Schema(
      example = "LEGAL_PERSON",
      description =
          "[XML FlussoRiversamento]=[istitutoMittente.identificativoUnivocoMittente.tipoIdentificativoUnivoco]"
              + " \n"
              + "G -> LEGAL_PERSON\n"
              + "A -> ABI_CODE\n"
              + "B -> BIC_CODE")
  private SenderTypeEnum type;

  @NotNull
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(
      example = "SELBIT2B",
      description =
          "[XML FlussoRiversamento]=[istitutoMittente.identificativoUnivocoMittente.codiceIdentificativoUnivoco]")
  private String id;

  @NotNull
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(
      example = "60000000001",
      description = "[XML NodoInviaFlussoRendicontazione]=[identificativoPSP]")
  @JsonProperty(AppConstant.PSP)
  private String pspId;

  @NotNull
  @Pattern(regexp = "^(.{3,70})$")
  @Schema(
      example = "Bank",
      description = "[XML FlussoRiversamento]=[istitutoMittente.denominazioneMittente]")
  private String pspName;

  @NotNull
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(
      example = "70000000001",
      description = "[XML NodoInviaFlussoRendicontazione]=[identificativoIntermediarioPSP]")
  private String pspBrokerId;

  @NotNull
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(
      example = "80000000001",
      description = "[XML NodoInviaFlussoRendicontazione]=[identificativoCanale]")
  private String channelId;

}