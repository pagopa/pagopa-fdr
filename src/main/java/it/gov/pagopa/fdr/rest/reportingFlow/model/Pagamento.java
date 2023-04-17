package it.gov.pagopa.fdr.rest.reportingFlow.model;

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
public class Pagamento {

  private String identificativoUnivocoVersamento;
  private String identificativoUnivocoRiscossione;
  private Optional<BigInteger> indiceDatiSingoloPagamento;
  private BigDecimal singoloImportoPagato;
  private CodiceEsitoPagamento codiceEsitoSingoloPagamento;
  private Instant dataEsitoSingoloPagamento;
}
