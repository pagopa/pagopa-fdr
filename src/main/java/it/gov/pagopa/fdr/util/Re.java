package it.gov.pagopa.fdr.util;

import it.gov.pagopa.fdr.service.re.model.FlowActionEnum;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Re {
  public FlowActionEnum flowName() default FlowActionEnum.CREATE_FLOW;
}
