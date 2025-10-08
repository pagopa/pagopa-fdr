package it.gov.pagopa.fdr.util.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class PastLimitValidator implements ConstraintValidator<PastDateLimit, Optional<Instant>> {

  private long relativeValue;
  private ChronoUnit relativeUnit;

  @Override
  public void initialize(PastDateLimit constraintAnnotation) {
    this.relativeValue = constraintAnnotation.value();
    this.relativeUnit = constraintAnnotation.unit();
  }

  @Override
  public boolean isValid(Optional<Instant> optionalFlowDate, ConstraintValidatorContext context) {
    if (optionalFlowDate.isEmpty()) {
      return true;
    }
    Instant flowDate = optionalFlowDate.get();

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
