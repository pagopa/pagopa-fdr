package it.gov.pagopa.fdr.service.middleware.validator.clause;

import it.gov.pagopa.fdr.repository.sql.FlowEntity;
import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.validator.ValidationArgs;
import it.gov.pagopa.fdr.util.validator.ValidationResult;
import it.gov.pagopa.fdr.util.validator.ValidationStep;
import java.math.BigDecimal;

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
    // Double totAmount = flow.getTotAmount();
    // Double computedTotAmount = flow.getComputedTotAmount();
    BigDecimal totAmount = flow.getTotAmount();
    BigDecimal computedTotAmount = flow.getComputedTotAmount();
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
