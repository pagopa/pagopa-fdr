package it.gov.pagopa.fdr.util;

import it.gov.pagopa.fdr.service.re.model.FlowActionEnum;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;

public class AppReUtil {

  public static FlowActionEnum getFlowNamebyAnnotation(Annotation[] annotations) {
    Optional<Annotation> reAnnotaion =
        Arrays.stream(annotations)
            .filter(a -> a.annotationType().isAssignableFrom(Re.class))
            .findFirst();

    FlowActionEnum flowActionEnum =
        reAnnotaion
            .map(
                a -> {
                  try {
                    return (FlowActionEnum) a.annotationType().getMethod("flowName").invoke(a);
                  } catch (IllegalAccessException
                      | InvocationTargetException
                      | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                  }
                })
            .orElseGet(null);

    return flowActionEnum;
  }
}
