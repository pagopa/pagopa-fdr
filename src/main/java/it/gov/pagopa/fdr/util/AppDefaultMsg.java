package it.gov.pagopa.fdr.util;

import io.quarkus.qute.Qute;
import io.quarkus.qute.i18n.Message;
import io.quarkus.qute.i18n.MessageBundle;

@MessageBundle
public interface AppDefaultMsg {
  @Message("Fruit name lehgth {value}. Expected min {min}, max {max}")
  String fruit_name_length(String value, int min, int max);

  @Message
  default String message(String key, Object... args) {
    return Qute.fmt(key, args);
  }

}
