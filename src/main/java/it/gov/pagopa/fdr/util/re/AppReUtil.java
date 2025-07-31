package it.gov.pagopa.fdr.util.re;

import it.gov.pagopa.fdr.controller.interfaces.annotation.Re;
import it.gov.pagopa.fdr.service.model.re.FdrActionEnum;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;

public class AppReUtil {

  public static FdrActionEnum getFdrActionByAnnotation(Annotation[] annotations) {
    Optional<Annotation> reAnnotation =
        Arrays.stream(annotations)
            .filter(a -> a.annotationType().isAssignableFrom(Re.class))
            .findFirst();

    FdrActionEnum fdrActionEnum =
        reAnnotation
            .map(
                a -> {
                  try {
                    return (FdrActionEnum) a.annotationType().getMethod("action").invoke(a);
                  } catch (IllegalAccessException
                      | InvocationTargetException
                      | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                  }
                })
            .orElse(null);

    return fdrActionEnum;
  }
}
