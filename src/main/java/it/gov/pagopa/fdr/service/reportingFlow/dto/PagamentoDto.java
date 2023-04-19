package it.gov.pagopa.fdr.service.reportingFlow.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PagamentoDto {

  private String identificativoUnivocoVersamento;
  private String identificativoUnivocoRiscossione;
  private Long indiceDatiSingoloPagamento;
  private BigDecimal singoloImportoPagato;
  private CodiceEsitoPagamentoDto codiceEsitoSingoloPagamento;
  private Instant dataEsitoSingoloPagamento;
}
