package it.gov.pagopa.fdr.rest.reportingFlow.model;

public enum TipoIdentificativoUnivoco {
  PERSONA_GIURIDICA("G"),
  CODICE_ABI("A"),
  CODICE_BIC("B");

  private final String value;

  TipoIdentificativoUnivoco(String value) {
    this.value = value;
  }
}
