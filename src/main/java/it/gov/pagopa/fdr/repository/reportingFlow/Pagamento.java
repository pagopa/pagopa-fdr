package it.gov.pagopa.fdr.repository.reportingFlow;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;

@Data
public class Pagamento {

  public String identificativoUnivocoVersamento;
  public String identificativoUnivocoRiscossione;
  public Long indiceDatiSingoloPagamento;
  public BigDecimal singoloImportoPagato;
  public CodiceEsitoPagamento codiceEsitoSingoloPagamento;
  public Instant dataEsitoSingoloPagamento;
}
