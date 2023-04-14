package it.gov.pagopa.fdr.rest.reportingFlow.validation;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.rest.reportingFlow.request.CreateRequest;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReportingFlowValidationService {

  @Inject Logger log;

  @WithSpan(kind = SERVER)
  public void validateCreateRequest(CreateRequest createRequest) {
    log.debug("Validate createRequest");
  }
}
