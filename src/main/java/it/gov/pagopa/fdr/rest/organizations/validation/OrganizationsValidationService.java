package it.gov.pagopa.fdr.rest.organizations.validation;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.rest.validation.CommonValidationService;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;

@ApplicationScoped
public class OrganizationsValidationService extends CommonValidationService {

  private final Logger log;

  public OrganizationsValidationService(Logger log) {
    this.log = log;
  }

  @WithSpan(kind = SERVER)
  public void validateGetAllByEc(
      String action, String ecId, String pspId, ConfigDataV1 configData) {
    log.info(AppMessageUtil.logValidate(action));

    // check psp
    if (null != pspId && !pspId.isBlank()) {
      checkPaymentServiceProvider(log, pspId, configData);
    }

    // check ec
    checkCreditorInstitution(log, ecId, configData);
  }

  @WithSpan(kind = SERVER)
  public void validateGet(
      String action, String fdr, String ecId, String pspId, ConfigDataV1 configData) {
    log.info(AppMessageUtil.logValidate(action));

    // check psp
    checkPaymentServiceProvider(log, pspId, configData);

    // check ec
    checkCreditorInstitution(log, ecId, configData);

    // check fdr format
    checkReportingFlowFormat(log, fdr, pspId);
  }

  @WithSpan(kind = SERVER)
  public void validateGetPayment(
      String action, String fdr, String ecId, String pspId, ConfigDataV1 configData) {
    log.info(AppMessageUtil.logValidate(action));

    // check psp
    checkPaymentServiceProvider(log, pspId, configData);

    // check ec
    checkCreditorInstitution(log, ecId, configData);

    // check fdr format
    checkReportingFlowFormat(log, fdr, pspId);
  }
}
