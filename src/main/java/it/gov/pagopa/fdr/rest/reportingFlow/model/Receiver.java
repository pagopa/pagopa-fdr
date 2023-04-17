package it.gov.pagopa.fdr.rest.reportingFlow.model;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class Receiver {

  private String id;
  private String name;
}
