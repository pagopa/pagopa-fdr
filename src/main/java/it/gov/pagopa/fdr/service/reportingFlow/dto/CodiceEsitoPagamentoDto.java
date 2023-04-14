package it.gov.pagopa.fdr.service.reportingFlow.dto;

public enum CodiceEsitoPagamentoDto {
  PAGAMENTO_ESEGUITO("0"),
  PAGAMENTO_REVOCATO("3"),
  PAGAMENTO_NO_RPT("9");

  private final String value;

  CodiceEsitoPagamentoDto(String value) {
    this.value = value;
  }
}
