package it.gov.pagopa.fdr.util;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CheckInstantFormatValidator
    implements ConstraintValidator<CheckInstantFormat, String> {

  private String pattern;
  private String timezone;

  @Override
  public void initialize(CheckInstantFormat constraintAnnotation) {
    this.pattern = constraintAnnotation.pattern();
    this.timezone = constraintAnnotation.timezone();
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    try {
      SimpleDateFormat sdf = new SimpleDateFormat(pattern);
      sdf.setTimeZone(TimeZone.getTimeZone(timezone));
      sdf.parse(value);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
