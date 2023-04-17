package it.gov.pagopa.fdr.rest.reportingFlow.model;

import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
public enum TipoIdentificativoUnivoco {
  PERSONA_GIURIDICA,
  CODICE_ABI,
  CODICE_BIC;
}
