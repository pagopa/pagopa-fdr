package it.gov.pagopa.fdr.rest.model;

import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
public enum SenderTypeEnum {
  LEGAL_PERSON,
  ABI_CODE,
  BIC_CODE;
}
