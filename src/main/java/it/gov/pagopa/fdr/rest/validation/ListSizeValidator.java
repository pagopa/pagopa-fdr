package it.gov.pagopa.fdr.rest.validation;

import it.gov.pagopa.fdr.rest.model.Payment;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

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

    return value.size() >= min && value.size() <= max;
  }
}
