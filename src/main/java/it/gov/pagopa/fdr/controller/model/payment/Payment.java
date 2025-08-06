package it.gov.pagopa.fdr.controller.model.payment;

import it.gov.pagopa.fdr.controller.model.payment.enums.PaymentStatusEnum;
import jakarta.validation.constraints.*;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@Jacksonized
public class Payment {

  @NotNull
  @Min(value = 1)
  @Schema(example = "1", description = "Unique index of the payment in the flow")
  private Long index;

  @NotNull
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(
      example = "17854456582215",
      description =
          "The value of the 'Identificativo Univoco Versamento' code related to the payment.<br>In"
              + " the XML request for SOAP primitives, this field is mappable with the tag"
              + " <b>[FlussoRiversamento.datiSingoliPagamenti.identificativoUnivocoVersamento]</b>.")
  private String iuv;

  @NotNull
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(
      example = "3354426511008",
      description =
          "The value of the 'Identificativo Univoco Riscossione' code related to the payment in the"
              + " flow.<br>In the XML request for SOAP primitives, this field is mappable with the"
              + " tag <b>[FlussoRiversamento.datiSingoliPagamenti.identificativoUnivocoRiscossione]</b>.")
  private String iur;

  @NotNull
  @Min(value = 1)
  @Max(value = 5)
  @Schema(
      example = "1",
      description =
          "The value of the transfer identifier related to the payment during the payment"
              + " process.<br>In the XML request for SOAP primitives, this field is mappable with"
              + " the tag"
              + " <b>[FlussoRiversamento.datiSingoliPagamenti.indiceDatiSingoloPagamento]</b>.")
  private Long idTransfer;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = false)
  @Digits(integer = Integer.MAX_VALUE, fraction = 2)
  @Schema(
      example = "0.01",
      description =
          "The value of the payment amount in decimal euro.<br>In the XML request for SOAP"
              + " primitives, this field is mappable with the tag"
              + " <b>[FlussoRiversamento.datiSingoliPagamenti.singoloImportoPagato]</b>.")
  private Double pay;

  @NotNull
  @Schema(
      example = "EXECUTED",
      enumeration = {"EXECUTED", "REVOKED", "STAND_IN", "STAND_IN_NO_RPT", "NO_RPT"},
      description =
          "The value of the status of the payment in relation to ist completion.<br>In the XML"
              + " request for SOAP primitives, this field is mappable with the tag"
              + " <b>[FlussoRiversamento.datiSingoliPagamenti.codiceEsitoSingoloPagamento]</b>.<br>The"
              + " enumeration is mapped with the following values:<br><ul><li>EXECUTED ->"
              + " 0</li><li>REVOKED -> 3</li><li>STAND_IN -> 4</li><li>STAND_IN_NO_RPT ->"
              + " 8</li><li>NO_RPT -> 9</li></ul>")
  private PaymentStatusEnum payStatus;

  @NotNull
  @Schema(
      example = "2025-01-01T12:30:50.900000Z",
      description =
          "The value of the date of the payment in relation to its completion.<br>In the XML"
              + " request for SOAP primitives, this field is mappable with the tag"
              + " <b>[FlussoRiversamento.datiSingoliPagamenti.dataEsitoSingoloPagamento]</b>.")
  private Instant payDate;
}
