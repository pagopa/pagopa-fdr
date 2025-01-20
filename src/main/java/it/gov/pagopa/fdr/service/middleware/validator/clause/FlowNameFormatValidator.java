package it.gov.pagopa.fdr.service.middleware.validator.clause;

import it.gov.pagopa.fdr.util.validator.ValidationResult;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;

public class FlowNameFormatValidator {

  public ValidationResult validate(String pspId, ConfigDataV1 configData) {

    ValidationResult result = ValidationResult.asValid();

    return result;
  }
}
