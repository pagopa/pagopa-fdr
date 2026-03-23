package it.gov.pagopa.fdr.util.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
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
    Instant limitDate = Instant.now().atZone(ZoneOffset.UTC).minus(relativeValue, relativeUnit)
            .toLocalDate()
            .atTime(LocalTime.MIN).atZone(ZoneOffset.UTC).toInstant();

    // flowDate must be after or equal to 30th days from now at 00:00
    // ex:
    // flowDate: 2026-01-31 00:00:00Z
    // now: 2026-03-23 06:37:00Z
    // then limitDate: 2026-02-21 00:00:00Z
    // so flowDate must be after 2026-02-21 00:00:00Z to be valid
    boolean isValid = flowDate.compareTo(limitDate) >= 0;

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
