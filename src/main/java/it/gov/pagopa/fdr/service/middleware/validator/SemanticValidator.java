package it.gov.pagopa.fdr.service.middleware.validator;

import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.service.middleware.validator.clause.CreditorInstitutionValidator;
import it.gov.pagopa.fdr.service.middleware.validator.clause.FlowNameFormatValidator;
import it.gov.pagopa.fdr.service.middleware.validator.clause.PspValidator;
import it.gov.pagopa.fdr.service.model.FindFlowsByFiltersArgs;
import it.gov.pagopa.fdr.util.StringUtil;
import it.gov.pagopa.fdr.util.validator.ValidationArgs;
import it.gov.pagopa.fdr.util.validator.ValidationResult;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;

public class SemanticValidator {

  private SemanticValidator() {}

  public static void validateGetPaginatedFlowsRequest(
      ConfigDataV1 cachedConfig, FindFlowsByFiltersArgs args) throws AppException {

    // set validation arguments
    String pspId = args.getPspId();
    ValidationArgs validationArgs =
        ValidationArgs.newArgs()
            .addArgument("configDataV1", cachedConfig)
            .addArgument("pspId", pspId)
            .addArgument("creditorInstitutionId", args.getOrganizationId());

    ValidationResult validationResult;

    // if "filter-by-psp" is not passed, exclude from validation
    if (StringUtil.isNullOrBlank(pspId)) {
      validationResult = new CreditorInstitutionValidator().validate(validationArgs);
    }

    // if "filter-by-psp" is passed, include it in validation
    else {
      validationResult =
          new PspValidator().linkTo(new CreditorInstitutionValidator()).validate(validationArgs);
    }

    if (validationResult.isInvalid()) {
      throw new AppException(validationResult.getError(), validationResult.getErrorArgs());
    }
  }

  public static void validateGetSingleFlowRequest(
      ConfigDataV1 cachedConfig, FindFlowsByFiltersArgs args) throws AppException {

    // set validation arguments
    String pspId = args.getPspId();
    ValidationArgs validationArgs =
        ValidationArgs.newArgs()
            .addArgument("configDataV1", cachedConfig)
            .addArgument("pspId", pspId)
            .addArgument("creditorInstitutionId", args.getOrganizationId())
            .addArgument("flowName", args.getFlowName());

    ValidationResult validationResult =
        new PspValidator()
            .linkTo(new CreditorInstitutionValidator())
            .linkTo(new FlowNameFormatValidator())
            .validate(validationArgs);

    if (validationResult.isInvalid()) {
      throw new AppException(validationResult.getError(), validationResult.getErrorArgs());
    }
  }

  public static void validateGetPaymentsFromPublishedFlow(
      ConfigDataV1 cachedConfig, FindFlowsByFiltersArgs args) throws AppException {

    // set validation arguments
    String pspId = args.getPspId();
    ValidationArgs validationArgs =
        ValidationArgs.newArgs()
            .addArgument("configDataV1", cachedConfig)
            .addArgument("pspId", pspId)
            .addArgument("creditorInstitutionId", args.getOrganizationId())
            .addArgument("flowName", args.getFlowName());

    ValidationResult validationResult =
        new PspValidator()
            .linkTo(new CreditorInstitutionValidator())
            .linkTo(new FlowNameFormatValidator())
            .validate(validationArgs);

    if (validationResult.isInvalid()) {
      throw new AppException(validationResult.getError(), validationResult.getErrorArgs());
    }
  }
}
