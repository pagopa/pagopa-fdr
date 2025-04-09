package it.gov.pagopa.fdr.controller.model.flow.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.fdr.controller.model.flow.Receiver;
import it.gov.pagopa.fdr.controller.model.flow.Sender;
import it.gov.pagopa.fdr.util.constant.ControllerConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@Jacksonized
public class CreateFlowRequest {

  @NotNull
  @Pattern(regexp = "[a-zA-Z0-9\\-_]{1,35}")
  @Schema(
      example = "2016-08-16pspTest-1178",
      description =
          "The value of the flow identifier.<br>In the XML request for SOAP primitives, this field"
              + " is mappable with the tag"
              + " <b>[NodoInviaFlussoRendicontazione.identificativoFlusso]</b>.")
  @JsonProperty(ControllerConstants.PARAMETER_FDR)
  private String fdr;

  @NotNull
  @Schema(
      example = "2025-01-01T12:20:30.800000Z",
      description =
          "The date related to the flow reporting.<br>In the XML request for SOAP primitives, this"
              + " field is mappable with the tag"
              + " <b>[NodoInviaFlussoRendicontazione.dataOraFlusso]</b>.")
  private Instant fdrDate;

  @NotNull
  @Valid
  @Schema(
      description =
          "The information related to the entity that will compile and send the published flow.")
  private Sender sender;

  @NotNull
  @Valid
  @Schema(
      description = "The information related to the entity that will receive the published flow.")
  private Receiver receiver;

  @NotNull
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(
      example = "SEPA - Bonifico X",
      description =
          "The description related to the regulation payment related to the flow.<br>In the XML"
              + " request for SOAP primitives, this field is mappable with the tag"
              + " <b>[FlussoRiversamento.identificativoUnivocoRegolamento]</b>.")
  private String regulation;

  @NotNull
  @Schema(
      example = "2023-04-03",
      description =
          "The date of the regulation payment related to the flow.<br>In the XML request for SOAP"
              + " primitives, this field is mappable with the tag"
              + " <b>[FlussoRiversamento.dataRegolamento]</b>.")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate regulationDate;

  @Schema(
      example = "UNCRITMMXXX",
      description =
          "The BIC code of the bank where the regulation payment related to the flow will be"
              + " poured.<br>In the XML request for SOAP primitives, this field is mappable with"
              + " the tag <b>[FlussoRiversamento.codiceBicBancaDiRiversamento]</b>.")
  @Pattern(regexp = "^(.{1,35})$")
  private String bicCodePouringBank;

  @NotNull
  @Min(value = 1)
  @Schema(
      example = "1",
      description =
          "The total number of payments to be included in the flow during the flow"
              + " compilation.<br>In the XML request for SOAP primitives, this field is mappable"
              + " with the tag <b>[FlussoRiversamento.numeroTotalePagamenti]</b>.")
  private Long totPayments;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = false)
  @Digits(integer = Integer.MAX_VALUE, fraction = 2)
  @Schema(
      example = "0.01",
      pattern = "^\\d{1,2147483647}[.]\\d{1,2}?$",
      description =
          "The total amount of payments to be calculated in the flow during the flow"
              + " compilation.<br>In the XML request for SOAP primitives, this field is mappable"
              + " with the tag <b>[FlussoRiversamento.importoTotalePagamenti]</b>.")
  private Double sumPayments;
}
