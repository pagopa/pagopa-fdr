package it.gov.pagopa.fdr.service.reportingFlow.dto;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class PagamentoDto {

  private String identificativoUnivocoVersamento;
  private String identificativoUnivocoRiscossione;
  private Optional<BigInteger> indiceDatiSingoloPagamento;
  private BigDecimal singoloImportoPagato;
  private CodiceEsitoPagamentoDto codiceEsitoSingoloPagamento;
  private Instant dataEsitoSingoloPagamento;
}
