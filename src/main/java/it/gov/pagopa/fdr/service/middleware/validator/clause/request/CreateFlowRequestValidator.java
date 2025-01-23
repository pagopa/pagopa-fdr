package it.gov.pagopa.fdr.service.middleware.validator.clause.request;

import it.gov.pagopa.fdr.controller.model.flow.request.CreateFlowRequest;
import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.validator.ValidationArgs;
import it.gov.pagopa.fdr.util.validator.ValidationResult;
import it.gov.pagopa.fdr.util.validator.ValidationStep;

public class CreateFlowRequestValidator extends ValidationStep {

  @Override
  public ValidationResult validate(ValidationArgs args) {

    CreateFlowRequest request = args.getArgument("request", CreateFlowRequest.class);
    String pspId = args.getArgument("pspId", String.class);
    String flowName = args.getArgument("flowName", String.class);

    String flowNameFromRequest = request.getFdr();
    String pspIdFromRequest = request.getSender().getPspId();

    // check if PSP identifier extracted from request is equals to URL parameter
    if (!pspId.equals(pspIdFromRequest)) {
      return ValidationResult.asInvalid(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PSP_ID_NOT_MATCH,
          flowName,
          pspId,
          pspIdFromRequest);
    }

    // check if flow name extracted from request is equals to URL parameter
    if (!flowName.equals(flowNameFromRequest)) {
      return ValidationResult.asInvalid(
          AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_NOT_MATCH, flowNameFromRequest, flowName);
    }

    return this.checkNext(args);
  }
}
