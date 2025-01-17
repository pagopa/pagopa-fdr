package it.gov.pagopa.fdr.controller.model.flow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.fdr.controller.model.flow.enums.SenderTypeEnum;
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
public class Sender {

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

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Pattern(regexp = "^(\\w{8,15})$")
  @Schema(
      example = "1234567890",
      deprecated = true,
      description = "[XML NodoInviaFlussoRendicontazione]=[password]")
  private String password;
}
