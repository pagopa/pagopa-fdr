package it.gov.pagopa.fdr.service.middleware.validator.clause;

import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.validator.ValidationArgs;
import it.gov.pagopa.fdr.util.validator.ValidationResult;
import it.gov.pagopa.fdr.util.validator.ValidationStep;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;
import org.openapi.quarkus.api_config_cache_json.model.PaymentServiceProvider;

public class PspValidator extends ValidationStep {

  @Override
  public ValidationResult validate(ValidationArgs args) {

    ConfigDataV1 configData = args.getArgument("configDataV1", ConfigDataV1.class);
    String pspId = args.getArgument("pspId", String.class);

    PaymentServiceProvider psp = configData.getPsps().get(pspId);

    // executing a lookup in cached configuration, check if PSP exists
    if (psp == null) {
      return ValidationResult.asInvalid(AppErrorCodeMessageEnum.PSP_UNKNOWN, pspId);
    }

    // executing a lookup in cached configuration, check if PSP is enabled
    if (psp.getEnabled() == null || !psp.getEnabled()) {
      return ValidationResult.asInvalid(AppErrorCodeMessageEnum.PSP_NOT_ENABLED, pspId);
    }

    return this.checkNext(args);
  }
}
