package it.gov.pagopa.fdr.util.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class PastLimitValidator implements ConstraintValidator<PastLimit, Instant> {

  private long relativeValue;
  private ChronoUnit relativeUnit;

  @Override
  public void initialize(PastLimit constraintAnnotation) {
    this.relativeValue = constraintAnnotation.value();
    this.relativeUnit = constraintAnnotation.unit();
  }

  @Override
  public boolean isValid(Instant flowDate, ConstraintValidatorContext context) {
    if (flowDate == null) {
      return true;
    }

    Instant now = Instant.now();
    ZonedDateTime nowUtc = now.atZone(ZoneOffset.UTC);

    ZonedDateTime limitZoned = nowUtc.minus(relativeValue, relativeUnit);
    Instant limitDate = limitZoned.toInstant();

    boolean isValid = !flowDate.isBefore(limitDate);

    if (!isValid) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              context
                  .getDefaultConstraintMessageTemplate()
                  .replace("{value}", String.valueOf(relativeValue))
                  .replace("{unit}", relativeUnit.name().toLowerCase()))
          .addConstraintViolation();
    }

    return isValid;
  }
}
