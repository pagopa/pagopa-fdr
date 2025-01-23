package it.gov.pagopa.fdr.service.middleware.validator;

import it.gov.pagopa.fdr.controller.model.flow.request.CreateFlowRequest;
import it.gov.pagopa.fdr.controller.model.payment.request.AddPaymentRequest;
import it.gov.pagopa.fdr.controller.model.payment.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.repository.entity.flow.FdrFlowEntity;
import it.gov.pagopa.fdr.service.middleware.validator.clause.BrokerPspValidator;
import it.gov.pagopa.fdr.service.middleware.validator.clause.ChannelValidator;
import it.gov.pagopa.fdr.service.middleware.validator.clause.ComputedPaymentsValidator;
import it.gov.pagopa.fdr.service.middleware.validator.clause.CreditorInstitutionValidator;
import it.gov.pagopa.fdr.service.middleware.validator.clause.FlowNameFormatValidator;
import it.gov.pagopa.fdr.service.middleware.validator.clause.PspValidator;
import it.gov.pagopa.fdr.service.middleware.validator.clause.PublishableStatusValidator;
import it.gov.pagopa.fdr.service.middleware.validator.clause.request.AddPaymentRequestValidator;
import it.gov.pagopa.fdr.service.middleware.validator.clause.request.CreateFlowRequestValidator;
import it.gov.pagopa.fdr.service.middleware.validator.clause.request.DeletePaymentRequestValidator;
import it.gov.pagopa.fdr.service.model.arguments.FindFlowsByFiltersArgs;
import it.gov.pagopa.fdr.util.StringUtil;
import it.gov.pagopa.fdr.util.validator.ValidationArgs;
import it.gov.pagopa.fdr.util.validator.ValidationResult;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;

public class SemanticValidator {

  private SemanticValidator() {}

  public static void validateGetSingleFlowFilters(
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

  public static void validateOnlyFlowFilters(
      ConfigDataV1 cachedConfig, String pspId, String flowName) throws AppException {

    // set validation arguments
    ValidationArgs validationArgs =
        ValidationArgs.newArgs()
            .addArgument("configDataV1", cachedConfig)
            .addArgument("pspId", pspId)
            .addArgument("flowName", flowName);

    ValidationResult validationResult =
        new PspValidator().linkTo(new FlowNameFormatValidator()).validate(validationArgs);

    if (validationResult.isInvalid()) {
      throw new AppException(validationResult.getError(), validationResult.getErrorArgs());
    }
  }

  public static void validateOnlyPspFilters(
      ConfigDataV1 cachedConfig, FindFlowsByFiltersArgs args) {

    // set validation arguments
    String pspId = args.getPspId();
    ValidationArgs validationArgs =
        ValidationArgs.newArgs()
            .addArgument("configDataV1", cachedConfig)
            .addArgument("pspId", pspId);

    ValidationResult validationResult = new PspValidator().validate(validationArgs);

    if (validationResult.isInvalid()) {
      throw new AppException(validationResult.getError(), validationResult.getErrorArgs());
    }
  }

  public static void validatePublishingFlow(FdrFlowEntity flow) throws AppException {

    // set validation arguments
    ValidationArgs validationArgs = ValidationArgs.newArgs().addArgument("flow", flow);

    ValidationResult validationResult =
        new PublishableStatusValidator()
            .linkTo(new ComputedPaymentsValidator())
            .validate(validationArgs);

    if (validationResult.isInvalid()) {
      throw new AppException(validationResult.getError(), validationResult.getErrorArgs());
    }
  }

  public static void validateGetPaginatedFlowsRequestForOrganizations(
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

  public static void validateGetPaginatedFlowsRequestForPsps(
      ConfigDataV1 cachedConfig, FindFlowsByFiltersArgs args) throws AppException {

    // set validation arguments
    String organizationId = args.getOrganizationId();
    ValidationArgs validationArgs =
        ValidationArgs.newArgs()
            .addArgument("configDataV1", cachedConfig)
            .addArgument("pspId", args.getPspId())
            .addArgument("creditorInstitutionId", organizationId);

    ValidationResult validationResult;

    // if "filter-by-organization" is not passed, exclude from validation
    if (StringUtil.isNullOrBlank(organizationId)) {
      validationResult = new PspValidator().validate(validationArgs);
    }

    // if "filter-by-organization" is passed, include it in validation
    else {
      validationResult =
          new PspValidator().linkTo(new CreditorInstitutionValidator()).validate(validationArgs);
    }

    if (validationResult.isInvalid()) {
      throw new AppException(validationResult.getError(), validationResult.getErrorArgs());
    }
  }

  public static void validateCreateFlowRequest(
      ConfigDataV1 cachedConfig, String pspId, String flowName, CreateFlowRequest request)
      throws AppException {

    // set validation arguments
    ValidationArgs validationArgs =
        ValidationArgs.newArgs()
            .addArgument("configDataV1", cachedConfig)
            .addArgument("pspId", pspId)
            .addArgument("channelId", request.getSender().getChannelId())
            .addArgument("brokerPspId", request.getSender().getPspBrokerId())
            .addArgument("creditorInstitutionId", request.getReceiver().getOrganizationId())
            .addArgument("flowName", flowName)
            .addArgument("request", request);

    ValidationResult validationResult =
        new PspValidator()
            .linkTo(new BrokerPspValidator())
            .linkTo(new ChannelValidator())
            .linkTo(new CreditorInstitutionValidator())
            .linkTo(new FlowNameFormatValidator())
            .linkTo(new CreateFlowRequestValidator())
            .validate(validationArgs);

    if (validationResult.isInvalid()) {
      throw new AppException(validationResult.getError(), validationResult.getErrorArgs());
    }
  }

  public static void validateAddPaymentRequest(
      ConfigDataV1 cachedConfig, String pspId, String flowName, AddPaymentRequest request) {

    // set validation arguments
    ValidationArgs validationArgs =
        ValidationArgs.newArgs()
            .addArgument("configDataV1", cachedConfig)
            .addArgument("pspId", pspId)
            .addArgument("flowName", flowName)
            .addArgument("request", request);

    ValidationResult validationResult =
        new PspValidator()
            .linkTo(new FlowNameFormatValidator())
            .linkTo(new AddPaymentRequestValidator())
            .validate(validationArgs);

    if (validationResult.isInvalid()) {
      throw new AppException(validationResult.getError(), validationResult.getErrorArgs());
    }
  }

  public static void validateDeletePaymentRequest(
      ConfigDataV1 cachedConfig, String pspId, String flowName, DeletePaymentRequest request) {

    // set validation arguments
    ValidationArgs validationArgs =
        ValidationArgs.newArgs()
            .addArgument("configDataV1", cachedConfig)
            .addArgument("pspId", pspId)
            .addArgument("flowName", flowName)
            .addArgument("request", request);

    ValidationResult validationResult =
        new PspValidator()
            .linkTo(new FlowNameFormatValidator())
            .linkTo(new DeletePaymentRequestValidator())
            .validate(validationArgs);

    if (validationResult.isInvalid()) {
      throw new AppException(validationResult.getError(), validationResult.getErrorArgs());
    }
  }
}
