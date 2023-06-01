package it.gov.pagopa.fdr.rest.organizations.validation;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;

@ApplicationScoped
public class InternalOrganizationsValidationService {

  @Inject Logger log;

  @WithSpan(kind = SERVER)
  public void validateGetAllInternal(String pspId, ConfigDataV1 configData) {
    log.debug("Validate get all");
  }

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
