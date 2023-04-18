package it.gov.pagopa.fdr.rest.reportingFlow.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Optional;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@Jacksonized
public class Pagamento {

  @NotNull(message = "reporting-flow.create.payments.identificativoUnivocoVersamento.notNull")
  @NotBlank(
      message =
          "reporting-flow.create.payments.identificativoUnivocoVersamento.notBlank|${validatedValue}")
  @Schema(example = "abcdefg")
  private String identificativoUnivocoVersamento;

  @NotNull(message = "reporting-flow.create.payments.identificativoUnivocoRiscossione.notNull")
  @NotBlank(
      message =
          "reporting-flow.create.payments.identificativoUnivocoRiscossione.notBlank|${validatedValue}")
  @Schema(example = "abcdefg")
  private String identificativoUnivocoRiscossione;

  @Schema(example = "1")
  private Optional<BigInteger> indiceDatiSingoloPagamento;

  @DecimalMin(value = "0.0", inclusive = false, message = "reporting-flow.create.payments.singoloImportoPagato.decimalMin|${validatedValue}|{value}")
  @Digits(integer = Integer.MAX_VALUE, fraction = 2, message = "reporting-flow.create.payments.singoloImportoPagato.digits|${validatedValue}|{integer}|{fraction}")
  @Schema(example = "0.01")
  private BigDecimal singoloImportoPagato;

  @NotNull(message = "reporting-flow.create.payments.codiceEsitoSingoloPagamento.notNull")
  @Schema(example = "PAGAMENTO_ESEGUITO")
  private CodiceEsitoPagamento codiceEsitoSingoloPagamento;

  @NotNull(message = "reporting-flow.create.payments.dataEsitoSingoloPagamento.notNull")
  @Schema(example = "2023-02-03T12:00:30.900000Z")
  private Instant dataEsitoSingoloPagamento;
}
