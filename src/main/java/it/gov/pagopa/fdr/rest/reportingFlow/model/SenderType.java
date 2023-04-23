package it.gov.pagopa.fdr.rest.reportingFlow.model;

import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
public enum SenderType {
  LEGAL_PERSON,
  ABI_CODE,
  BIC_CODE;
}
