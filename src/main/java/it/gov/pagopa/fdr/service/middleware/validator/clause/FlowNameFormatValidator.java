package it.gov.pagopa.fdr.service.middleware.validator.clause;

import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.validator.ValidationArgs;
import it.gov.pagopa.fdr.util.validator.ValidationResult;
import it.gov.pagopa.fdr.util.validator.ValidationStep;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class FlowNameFormatValidator extends ValidationStep {

  @Override
  public ValidationResult validate(ValidationArgs args) {

    String flowName = args.getArgument("flowName", String.class);
    String pspId = args.getArgument("pspId", String.class);

    // check if flow name contains at least ten characters
    if (flowName.length() < 10) {
      return ValidationResult.asInvalid(
          AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_DATE_WRONG_FORMAT, flowName);
    }

    // check if flow name contains generation date on first ten characters
    try {
      String date = flowName.substring(0, 10);
      LocalDate.parse(date);
    } catch (DateTimeParseException e) {
      return ValidationResult.asInvalid(
          AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_DATE_WRONG_FORMAT, flowName);
    }

    // check if flow name contains PSP identifier after first ten characters and has a '-' separator
    String name = flowName.substring(10);
    if (!name.startsWith(String.format("%s-", pspId))) {
      return ValidationResult.asInvalid(
          AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_PSP_WRONG_FORMAT, flowName);
    }

    return this.checkNext(args);
  }
}
