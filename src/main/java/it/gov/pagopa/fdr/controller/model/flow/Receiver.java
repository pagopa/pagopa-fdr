package it.gov.pagopa.fdr.controller.model.flow;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class Receiver {

  @NotNull
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(
      example = "APPBIT2B",
      description =
          "The identifier of the Creditor Institution receiver entity.<br>In the XML request for"
              + " SOAP primitives, this field is mappable with the tag"
              + " <b>[FlussoRiversamento.istitutoRicevente.identificativoUnivocoRicevente.codiceIdentificativoUnivoco]</b>.")
  private String id;

  @NotNull
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(
      example = "20000000001",
      description =
          "The domain identifier of the Creditor Institution receiver entity.<br>In the XML request"
              + " for SOAP primitives, this field is mappable with the tag"
              + " <b>[NodoInviaFlussoRendicontazione.identificativoDominio]</b>.")
  @JsonProperty(ControllerConstants.PARAMETER_ORGANIZATION)
  private String organizationId;

  @NotNull
  @Pattern(regexp = "^(.{1,140})$")
  @Schema(
      example = "Comune di Roma",
      description =
          "The fiscal name of the Creditor Institution receiver entity.<br>In the XML request for"
              + " SOAP primitives, this field is mappable with the tag"
              + " <b>[FlussoRiversamento.istitutoRicevente.denominazioneRicevente]</b>.")
  private String organizationName;
}
