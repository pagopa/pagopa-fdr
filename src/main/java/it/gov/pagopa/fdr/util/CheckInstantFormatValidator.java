package it.gov.pagopa.fdr.util;

import io.quarkus.logging.Log;
import java.time.Instant;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CheckInstantFormatValidator
    implements ConstraintValidator<CheckInstantFormat, String> {

  @Override
  public void initialize(CheckInstantFormat constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    try {
      Instant.parse(value);
      return true;
    } catch (Exception e) {
      Log.debugf("Instant validator error with value [%s]", value, e);
      return false;
    }
  }
}
