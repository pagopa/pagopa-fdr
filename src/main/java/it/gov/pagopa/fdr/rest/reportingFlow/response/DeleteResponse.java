package it.gov.pagopa.fdr.rest.reportingFlow.response;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class DeleteResponse {
  public String id;
}
