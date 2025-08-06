package it.gov.pagopa.fdr.controller.model.flow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.fdr.controller.model.flow.enums.SenderTypeEnum;
import it.gov.pagopa.fdr.util.constant.ControllerConstants;
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
      enumeration = {"LEGAL_PERSON", "ABI_CODE", "BIC_CODE"},
      description =
          "The type of the PSP sender entity.<br>In the XML request for SOAP primitives, this field"
              + " is mappable with the tag"
              + " <b>[FlussoRiversamento.istitutoMittente.identificativoUnivocoMittente.tipoIdentificativoUnivoco]</b>.")
  private SenderTypeEnum type;

  @NotNull
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(
      example = "SELBIT2B",
      description =
          "The identifier of the PSP sender entity.<br>In the XML request for SOAP primitives, this"
              + " field is mappable with the tag"
              + " <b>[FlussoRiversamento.istitutoMittente.identificativoUnivocoMittente.codiceIdentificativoUnivoco]</b>.")
  private String id;

  @NotNull
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(
      example = "60000000001",
      description =
          "The domain identifier of the PSP sender entity.<br>In the XML request for SOAP"
              + " primitives, this field is mappable with the tag"
              + " <b>[NodoInviaFlussoRendicontazione.identificativoPSP]</b>.")
  @JsonProperty(ControllerConstants.PARAMETER_PSP)
  private String pspId;

  @NotNull
  @Pattern(regexp = "^(.{3,70})$")
  @Schema(
      example = "PSP Name",
      description =
          "The fiscal name of the PSP sender entity.<br>In the XML request for SOAP primitives,"
              + " this field is mappable with the tag"
              + " <b>[FlussoRiversamento.istitutoMittente.denominazioneMittente]</b>.")
  private String pspName;

  @NotNull
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(
      example = "70000000001",
      description =
          "The domain identifier of the PSP sender entity's Broker.<br>In the XML request for SOAP"
              + " primitives, this field is mappable with the tag"
              + " <b>[NodoInviaFlussoRendicontazione.identificativoIntermediarioPSP]</b>.")
  private String pspBrokerId;

  @NotNull
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(
      example = "80000000001",
      description =
          "The identifier of the PSP sender entity's Channel.<br>In the XML request for SOAP"
              + " primitives, this field is mappable with the tag"
              + " <b>[NodoInviaFlussoRendicontazione.identificativoCanale]</b>.")
  private String channelId;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Pattern(regexp = "^(\\w{8,15})$")
  @Schema(
      example = "password",
      deprecated = true,
      description =
          "The password of the PSP sender entity's Channel.<br>In the XML request for SOAP"
              + " primitives, this field is mappable with the tag"
              + " <b>[NodoInviaFlussoRendicontazione.password]</b>.")
  private String password;
}
