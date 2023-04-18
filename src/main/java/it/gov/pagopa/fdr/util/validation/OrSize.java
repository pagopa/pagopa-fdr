package it.gov.pagopa.fdr.util.validation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({
  ElementType.FIELD,
  ElementType.METHOD,
  ElementType.PARAMETER,
  ElementType.ANNOTATION_TYPE,
  ElementType.TYPE_USE
})
@Retention(RUNTIME)
@Constraint(validatedBy = OrSizeValidator.class)
@Documented
public @interface OrSize {

  String message() default "{OrSize.invalid}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  int[] lengths() default {0};
}