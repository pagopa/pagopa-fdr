package it.gov.pagopa.fdr.service.middleware.validator.clause.request;

import it.gov.pagopa.fdr.controller.model.payment.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.validator.ValidationArgs;
import it.gov.pagopa.fdr.util.validator.ValidationResult;
import it.gov.pagopa.fdr.util.validator.ValidationStep;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DeletePaymentRequestValidator extends ValidationStep {

  @Override
  public ValidationResult validate(ValidationArgs args) {

    String flowName = args.getArgument("flowName", String.class);
    DeletePaymentRequest request = args.getArgument("request", DeletePaymentRequest.class);

    // check if there are some duplicated indexes in request
    List<Long> indexes = request.getIndexList();
    Set<Long> uniqueIndexes = new HashSet<>(indexes);
    if (indexes.size() != uniqueIndexes.size()) {
      return ValidationResult.asInvalid(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_SAME_INDEX_IN_SAME_REQUEST, flowName);
    }

    return this.checkNext(args);
  }
}
