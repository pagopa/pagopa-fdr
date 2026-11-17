package it.gov.pagopa.fdr.repository.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BlobTagNameEnum {

  PROCESSING_STATUS("processingStatus"),
  PROCESSING_UPDATED_AT("processingUpdatedAt"),
  PROCESSING_ATTEMPT("processingAttempt");

  private final String value;
}
