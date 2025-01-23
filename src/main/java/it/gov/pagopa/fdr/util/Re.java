package it.gov.pagopa.fdr.util;

import it.gov.pagopa.fdr.service.model.re.FdrActionEnum;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Re {

  FdrActionEnum action();
}
