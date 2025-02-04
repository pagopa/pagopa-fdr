package it.gov.pagopa.fdr.service.middleware.validator.clause;

import it.gov.pagopa.fdr.repository.entity.FlowEntity;
import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.validator.ValidationArgs;
import it.gov.pagopa.fdr.util.validator.ValidationResult;
import it.gov.pagopa.fdr.util.validator.ValidationStep;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ComputedPaymentsValidator extends ValidationStep {

  @Override
  public ValidationResult validate(ValidationArgs args) {

    // FdrFlowEntity flow = args.getArgument("flow", FdrFlowEntity.class);
    FlowEntity flow = args.getArgument("flow", FlowEntity.class);

    // check if computed total payments is equals to pre-set total payments
    Long totPayments = flow.getTotPayments();
    Long computedTotPayments = flow.getComputedTotPayments();
    if (!totPayments.equals(computedTotPayments)) {
      return ValidationResult.asInvalid(
          AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_TOT_PAYMENT,
          flow.getName(),
          totPayments.toString(),
          computedTotPayments.toString());
    }

    // check if computed total amount is equals to pre-set total amount
    BigDecimal totAmount = flow.getTotAmount().setScale(2, RoundingMode.HALF_UP);
    BigDecimal computedTotAmount = flow.getComputedTotAmount().setScale(2, RoundingMode.HALF_UP);
    if (!totAmount.equals(computedTotAmount)) {
      return ValidationResult.asInvalid(
          AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_SUM_PAYMENT,
          flow.getName(),
          totAmount.toString(),
          computedTotAmount.toString());
    }

    return this.checkNext(args);
  }
}
