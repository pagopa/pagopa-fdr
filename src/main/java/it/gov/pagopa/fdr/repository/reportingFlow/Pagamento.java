package it.gov.pagopa.fdr.repository.reportingFlow;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Optional;

public class Pagamento {

  public String identificativoUnivocoVersamento;
  public String identificativoUnivocoRiscossione;
  public Optional<BigInteger> indiceDatiSingoloPagamento;
  public BigDecimal singoloImportoPagato;
  public CodiceEsitoPagamento codiceEsitoSingoloPagamento;
  public Instant dataEsitoSingoloPagamento;
}
