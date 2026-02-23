package it.gov.pagopa.fdr.util.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

@Constraint(validatedBy = PastLimitValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PastDateLimit {

  long value();

  ChronoUnit unit();

  String message() default "The date cannot be older than {value} {unit}.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
