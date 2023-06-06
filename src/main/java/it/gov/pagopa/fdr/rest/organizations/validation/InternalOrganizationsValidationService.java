package it.gov.pagopa.fdr.rest.organizations.validation;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.rest.validation.CommonValidationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;

@ApplicationScoped
public class InternalOrganizationsValidationService extends CommonValidationService {

  @Inject Logger log;

  @WithSpan(kind = SERVER)
  public void validateGetAllInternal(String pspId, ConfigDataV1 configData) {
    log.debug("Validate get all by ec");

    // check psp
    checkPaymentServiceProvider(pspId, configData);
  }

  @WithSpan(kind = SERVER)
  public void validateGetInternal(String fdr, String pspId, ConfigDataV1 configData) {
    log.debug("Validate get");

    // check psp
    checkPaymentServiceProvider(pspId, configData);

    // check reportingFlowName format
    checkReportingFlowFormat(fdr, pspId);
  }

  @WithSpan(kind = SERVER)
  public void validateGetPaymentInternal(String fdr, String pspId, ConfigDataV1 configData) {
    log.debug("Validate get payment");

    // check psp
    checkPaymentServiceProvider(pspId, configData);

    // check reportingFlowName format
    checkReportingFlowFormat(fdr, pspId);
  }

  @WithSpan(kind = SERVER)
  public void validateChangeInternalReadFlag(String fdr, String pspId, ConfigDataV1 configData) {
    log.debug("Validate change read flag");

    // check psp
    checkPaymentServiceProvider(pspId, configData);

    // check reportingFlowName format
    checkReportingFlowFormat(fdr, pspId);
  }
}
