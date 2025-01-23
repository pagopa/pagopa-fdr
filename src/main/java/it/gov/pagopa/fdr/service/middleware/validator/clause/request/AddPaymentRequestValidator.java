package it.gov.pagopa.fdr.service.middleware.validator.clause.request;

import it.gov.pagopa.fdr.controller.model.payment.Payment;
import it.gov.pagopa.fdr.controller.model.payment.request.AddPaymentRequest;
import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.validator.ValidationArgs;
import it.gov.pagopa.fdr.util.validator.ValidationResult;
import it.gov.pagopa.fdr.util.validator.ValidationStep;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AddPaymentRequestValidator extends ValidationStep {

  @Override
  public ValidationResult validate(ValidationArgs args) {

    String flowName = args.getArgument("flowName", String.class);
    AddPaymentRequest request = args.getArgument("request", AddPaymentRequest.class);

    // check if there are some duplicated indexes in request
    List<Payment> payments = request.getPayments();
    Set<Long> uniqueIndexes = payments.stream().map(Payment::getIndex).collect(Collectors.toSet());
    if (payments.size() != uniqueIndexes.size()) {
      return ValidationResult.asInvalid(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_SAME_INDEX_IN_SAME_REQUEST, flowName);
    }

    return this.checkNext(args);
  }
}
