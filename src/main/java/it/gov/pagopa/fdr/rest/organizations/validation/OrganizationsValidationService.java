package it.gov.pagopa.fdr.rest.organizations.validation;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.rest.validation.CommonValidationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;

@ApplicationScoped
public class OrganizationsValidationService extends CommonValidationService {

  @Inject Logger log;

  @WithSpan(kind = SERVER)
  public void validateGetAllByEc(String ecId, String pspId, ConfigDataV1 configData) {
    log.debug("Validate get all by ec");

    // check psp
    checkPaymentServiceProvider(pspId, configData);

    // check ec
    checkCreditorInstitution(ecId, configData);
  }

  @WithSpan(kind = SERVER)
  public void validateGet(String fdr, String ecId, String pspId, ConfigDataV1 configData) {
    log.debug("Validate get");

    // check psp
    checkPaymentServiceProvider(pspId, configData);

    // check ec
    checkCreditorInstitution(ecId, configData);

    // check reportingFlowName format
    checkReportingFlowFormat(fdr, pspId);
  }

  @WithSpan(kind = SERVER)
  public void validateGetPayment(String fdr, String ecId, String pspId, ConfigDataV1 configData) {
    log.debug("Validate get payment");

    // check psp
    checkPaymentServiceProvider(pspId, configData);

    // check ec
    checkCreditorInstitution(ecId, configData);

    // check reportingFlowName format
    checkReportingFlowFormat(fdr, pspId);
  }

  @WithSpan(kind = SERVER)
  public void validateChangeReadFlag(
      String fdr, String ecId, String pspId, ConfigDataV1 configData) {
    log.debug("Validate change read flag");

    // check psp
    checkPaymentServiceProvider(pspId, configData);

    // check ec
    checkCreditorInstitution(ecId, configData);

    // check reportingFlowName format
    checkReportingFlowFormat(fdr, pspId);
  }
}
