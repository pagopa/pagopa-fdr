package it.gov.pagopa.fdr.util.validation;

import it.gov.pagopa.fdr.rest.reportingFlow.model.Payment;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ListSizeValidator implements ConstraintValidator<ListSize, java.util.List<Payment>> {

  private int min;
  private int max;

  @Override
  public void initialize(ListSize constraintAnnotation) {
    this.min = constraintAnnotation.min();
    this.max = constraintAnnotation.max();
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(java.util.List<Payment> value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    if (value.size() >= min && value.size() <= max) {
      return true;
    } else {
      return false;
    }
  }
}
