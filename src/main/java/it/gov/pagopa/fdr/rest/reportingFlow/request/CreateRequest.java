package it.gov.pagopa.fdr.rest.reportingFlow.request;

import it.gov.pagopa.fdr.rest.reportingFlow.model.ReportingFlow;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@SuperBuilder
@Jacksonized
public class CreateRequest extends ReportingFlow {}
