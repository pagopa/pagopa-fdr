package it.gov.pagopa.fdr.rest.psps.validation;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.rest.validation.CommonValidationService;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;

@ApplicationScoped
public class InternalPspValidationService extends CommonValidationService {

  private final Logger log;

  public InternalPspValidationService(Logger log) {
    this.log = log;
  }

  @WithSpan(kind = SERVER)
  public void validateGetAllInternal(String action, String pspId, ConfigDataV1 configData) {
    log.info(AppMessageUtil.logValidate(action));

    // check psp
    if (null != pspId && !pspId.isBlank()) {
      checkPaymentServiceProvider(log, pspId, configData);
    }
  }

  @WithSpan(kind = SERVER)
  public void validateGetInternal(
      String action, String fdr, String pspId, String ecId, ConfigDataV1 configData) {
    log.info(AppMessageUtil.logValidate(action));

    // check ec
    checkCreditorInstitution(log, ecId, configData);

    // check psp
    checkPaymentServiceProvider(log, pspId, configData);

    // check fdr format
    checkReportingFlowFormat(log, fdr, pspId);
  }

  @WithSpan(kind = SERVER)
  public void validateGetPaymentInternal(
      String action, String fdr, String pspId, String ecId, ConfigDataV1 configData) {
    log.info(AppMessageUtil.logValidate(action));

    // check ec
    checkCreditorInstitution(log, ecId, configData);

    // check psp
    checkPaymentServiceProvider(log, pspId, configData);

    // check fdr format
    checkReportingFlowFormat(log, fdr, pspId);
  }
}
