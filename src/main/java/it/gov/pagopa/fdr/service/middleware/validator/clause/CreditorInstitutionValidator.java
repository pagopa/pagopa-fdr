package it.gov.pagopa.fdr.service.middleware.validator.clause;

import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.validator.ValidationArgs;
import it.gov.pagopa.fdr.util.validator.ValidationResult;
import it.gov.pagopa.fdr.util.validator.ValidationStep;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;
import org.openapi.quarkus.api_config_cache_json.model.CreditorInstitution;

public class CreditorInstitutionValidator extends ValidationStep {

  @Override
  public ValidationResult validate(ValidationArgs args) {

    ConfigDataV1 configData = args.getArgument("configDataV1", ConfigDataV1.class);
    String creditorInstitutionId = args.getArgument("creditorInstitutionId", String.class);

    CreditorInstitution creditorInstitution =
        configData.getCreditorInstitutions().get(creditorInstitutionId);

    // executing a lookup in cached configuration, check if CI exists
    if (creditorInstitution == null) {
      return ValidationResult.asInvalid(AppErrorCodeMessageEnum.EC_UNKNOWN, creditorInstitutionId);
    }

    // executing a lookup in cached configuration, check if CI is enabled
    else if (creditorInstitution.getEnabled() == null || !creditorInstitution.getEnabled()) {
      return ValidationResult.asInvalid(
          AppErrorCodeMessageEnum.EC_NOT_ENABLED, creditorInstitutionId);
    }

    return checkNext(args);
  }
}
