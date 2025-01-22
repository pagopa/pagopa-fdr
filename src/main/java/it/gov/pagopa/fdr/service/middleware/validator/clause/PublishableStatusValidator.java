package it.gov.pagopa.fdr.service.middleware.validator.clause;

import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.repository.entity.flow.FdrFlowEntity;
import it.gov.pagopa.fdr.repository.enums.FlowStatusEnum;
import it.gov.pagopa.fdr.util.validator.ValidationArgs;
import it.gov.pagopa.fdr.util.validator.ValidationResult;
import it.gov.pagopa.fdr.util.validator.ValidationStep;

public class PublishableStatusValidator extends ValidationStep {

  @Override
  public ValidationResult validate(ValidationArgs args) {

    FdrFlowEntity flow = args.getArgument("flow", FdrFlowEntity.class);

    // check if flow status is different from INSERTED
    FlowStatusEnum flowStatus = flow.getStatus();
    if (flowStatus != FlowStatusEnum.INSERTED) {
      return ValidationResult.asInvalid(
          AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION, flow.getName(), flowStatus.name());
    }

    return this.checkNext(args);
  }
}
