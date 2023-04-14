package it.gov.pagopa.fdr.service.reportingFlow.dto;

public enum TipoIdentificativoUnivocoDto {
  PERSONA_GIURIDICA("G"),
  CODICE_ABI("A"),
  CODICE_BIC("B");

  private final String value;

  TipoIdentificativoUnivocoDto(String value) {
    this.value = value;
  }
}
