package it.gov.pagopa.fdr.rest.model;

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
  @Schema(example = "1", description = "Identificativo del pagamento univoco nel flusso")
  private Long index;

  @NotNull
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(
      example = "abcdefg",
      description =
          "[XML FlussoRiversamento]=[datiSingoliPagamenti.identificativoUnivocoVersamento]")
  private String iuv;

  @NotNull
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(
      example = "abcdefg",
      description =
          "[XML FlussoRiversamento]=[datiSingoliPagamenti.identificativoUnivocoRiscossione]")
  private String iur;

  @NotNull
  @Min(value = 1)
  @Max(value = 5)
  @Schema(
      example = "1",
      description = "[XML FlussoRiversamento]=[datiSingoliPagamenti.indiceDatiSingoloPagamento]")
  private Long idTransfer;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = false)
  @Digits(integer = Integer.MAX_VALUE, fraction = 2)
  @Schema(
      example = "0.01",
      description = "[XML FlussoRiversamento]=[datiSingoliPagamenti.singoloImportoPagato]")
  private Double pay;

  @NotNull
  @Schema(
      example = "EXECUTED",
      description =
          "[XML FlussoRiversamento]=[datiSingoliPagamenti.codiceEsitoSingoloPagamento] \n"
              + "0 -> EXECUTED\n"
              + "3 -> REVOKED\n"
              + "9 -> NO_RPT\n"
              + "4 -> STAND_IN")
  private PaymentStatusEnum payStatus;

  @NotNull
  @Schema(
      example = "2023-02-03T12:00:30.900000Z",
      description = "[XML FlussoRiversamento]=[datiSingoliPagamenti.dataEsitoSingoloPagamento]")
  private Instant payDate;
}
