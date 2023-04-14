package it.gov.pagopa.fdr.repository.reportingFlow;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Optional;

public class Pagamento {

  private String identificativoUnivocoVersamento;
  private String identificativoUnivocoRiscossione;
  private Optional<BigInteger> indiceDatiSingoloPagamento;
  private BigDecimal singoloImportoPagato;
  private CodiceEsitoPagamento codiceEsitoSingoloPagamento;
  private Instant dataEsitoSingoloPagamento;
}
