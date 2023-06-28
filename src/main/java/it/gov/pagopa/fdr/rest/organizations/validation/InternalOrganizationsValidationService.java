package it.gov.pagopa.fdr.rest.organizations.validation;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.rest.validation.CommonValidationService;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;

@ApplicationScoped
public class InternalOrganizationsValidationService extends CommonValidationService {

  @Inject Logger log;

  @WithSpan(kind = SERVER)
  public void validateGetAllInternal(String action, String pspId, ConfigDataV1 configData) {
    log.info(AppMessageUtil.logValidate(action));

    // check psp
    checkPaymentServiceProvider(log, pspId, configData);
  }

  @WithSpan(kind = SERVER)
  public void validateGetInternal(
      String action, String fdr, String pspId, ConfigDataV1 configData) {
    log.info(AppMessageUtil.logValidate(action));

    // check psp
    checkPaymentServiceProvider(log, pspId, configData);

    // check reportingFlowName format
    checkReportingFlowFormat(log, fdr, pspId);
  }

  @WithSpan(kind = SERVER)
  public void validateGetPaymentInternal(
      String action, String fdr, String pspId, ConfigDataV1 configData) {
    log.info(AppMessageUtil.logValidate(action));

    // check psp
    checkPaymentServiceProvider(log, pspId, configData);

    // check reportingFlowName format
    checkReportingFlowFormat(log, fdr, pspId);
  }

  //  @WithSpan(kind = SERVER)
  //  public void validateChangeInternalReadFlag(
  //      String action, String fdr, String pspId, ConfigDataV1 configData) {
  //    log.info(AppMessageUtil.logValidate(action));
  //
  //    // check psp
  //    checkPaymentServiceProvider(log, pspId, configData);
  //
  //    // check reportingFlowName format
  //    checkReportingFlowFormat(log, fdr, pspId);
  //  }
}
