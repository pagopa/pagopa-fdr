package it.gov.pagopa.fdr.rest.organizations.validation;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class InternalOrganizationsValidationService {

  @Inject Logger log;

  @WithSpan(kind = SERVER)
  public void validateGetInternal(String fdr) {
    log.debug("Validate get");
  }

  @WithSpan(kind = SERVER)
  public void validateGetPaymentInternal(String fdr) {
    log.debug("Validate get payment");
  }

  @WithSpan(kind = SERVER)
  public void validateChangeInternalReadFlag(String fdr) {
    log.debug("Validate change internal read flag");
  }
}
