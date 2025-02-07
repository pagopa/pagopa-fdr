package it.gov.pagopa.fdr.service.middleware.validator.clause;

import it.gov.pagopa.fdr.repository.entity.FlowEntity;
import it.gov.pagopa.fdr.repository.enums.FlowStatusEnum;
import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.validator.ValidationArgs;
import it.gov.pagopa.fdr.util.validator.ValidationResult;
import it.gov.pagopa.fdr.util.validator.ValidationStep;

public class PublishableStatusValidator extends ValidationStep {

  @Override
  public ValidationResult validate(ValidationArgs args) {

    FlowEntity flow = args.getArgument("flow", FlowEntity.class);

    // check if flow status is different from INSERTED
    String flowStatus = flow.getStatus();
    if (!FlowStatusEnum.INSERTED.name().equals(flowStatus)) {
      return ValidationResult.asInvalid(
          AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION, flow.getName(), flowStatus);
    }

    return this.checkNext(args);
  }
}
