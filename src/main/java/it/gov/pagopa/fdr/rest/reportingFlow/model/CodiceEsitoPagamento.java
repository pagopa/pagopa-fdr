package it.gov.pagopa.fdr.rest.reportingFlow.model;

public enum CodiceEsitoPagamento {
  PAGAMENTO_ESEGUITO("0"),
  PAGAMENTO_REVOCATO("3"),
  PAGAMENTO_NO_RPT("9");

  private final String value;

  CodiceEsitoPagamento(String value) {
    this.value = value;
  }
}
