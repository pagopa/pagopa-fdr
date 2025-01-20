package it.gov.pagopa.fdr.service.middleware.validator;

import it.gov.pagopa.fdr.service.middleware.validator.clause.CreditorInstitutionValidator;
import it.gov.pagopa.fdr.service.middleware.validator.clause.PspValidator;
import it.gov.pagopa.fdr.service.model.FindFlowsByFiltersArgs;
import it.gov.pagopa.fdr.util.StringUtil;
import it.gov.pagopa.fdr.util.validator.ValidationArgs;
import it.gov.pagopa.fdr.util.validator.ValidationResult;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;

public class SemanticValidator {

  private SemanticValidator() {}

  public static ValidationResult validateGetPaginatedFlowsRequest(
      ConfigDataV1 cachedConfig, FindFlowsByFiltersArgs args) {

    // set validation arguments
    String pspId = args.getPspId();
    ValidationArgs validationArgs =
        ValidationArgs.newArgs()
            .addArgument("configDataV1", cachedConfig)
            .addArgument("pspId", pspId)
            .addArgument("creditorInstitutionId", args.getOrganizationId());

    ValidationResult result;

    // if "filter-by-psp" is not passed, exclude from validation
    if (StringUtil.isNullOrBlank(pspId)) {
      result = new CreditorInstitutionValidator().validate(validationArgs);
    }

    // if "filter-by-psp" is passed, include it in validation
    else {
      result =
          new PspValidator().linkTo(new CreditorInstitutionValidator()).validate(validationArgs);
    }
    return result;
  }
}
