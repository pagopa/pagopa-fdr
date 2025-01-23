package it.gov.pagopa.fdr.service.middleware.validator.clause;

import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.validator.ValidationArgs;
import it.gov.pagopa.fdr.util.validator.ValidationResult;
import it.gov.pagopa.fdr.util.validator.ValidationStep;
import org.openapi.quarkus.api_config_cache_json.model.BrokerPsp;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;

public class BrokerPspValidator extends ValidationStep {

  @Override
  public ValidationResult validate(ValidationArgs args) {

    ConfigDataV1 configData = args.getArgument("configDataV1", ConfigDataV1.class);
    String brokerPspId = args.getArgument("brokerPspId", String.class);

    BrokerPsp brokerPsp = configData.getPspBrokers().get(brokerPspId);

    // executing a lookup in cached configuration, check if broker PSP exists
    if (brokerPsp == null) {
      return ValidationResult.asInvalid(AppErrorCodeMessageEnum.BROKER_UNKNOWN, brokerPspId);
    }

    // executing a lookup in cached configuration, check if broker PSP is enabled
    if (brokerPsp.getEnabled() == null || !brokerPsp.getEnabled()) {
      return ValidationResult.asInvalid(AppErrorCodeMessageEnum.BROKER_NOT_ENABLED, brokerPspId);
    }

    return this.checkNext(args);
  }
}
