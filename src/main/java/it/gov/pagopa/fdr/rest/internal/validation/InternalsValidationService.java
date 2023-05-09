package it.gov.pagopa.fdr.rest.internal.validation;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class InternalsValidationService {

  @Inject Logger log;

  @WithSpan(kind = SERVER)
  public void validateGetAllByInternal(String app) {
    log.debug("Validate get all by internal");

    // TODO validate if NDP
  }
}
