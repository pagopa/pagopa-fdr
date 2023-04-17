package it.gov.pagopa.fdr.util.validation;

import io.quarkus.logging.Log;
import java.util.Arrays;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OrSizeValidator implements ConstraintValidator<OrSize, String> {

  private int[] lengths;

  @Override
  public void initialize(OrSize constraintAnnotation) {
    this.lengths = constraintAnnotation.lengths();
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    try {
      return Arrays.stream(lengths).anyMatch(a -> a == value.length());
    } catch (Exception e) {
      Log.debugf("OrSize error with value [%s]", value, e);
      return false;
    }
  }
}
