package it.gov.pagopa.fdr.util.validator;

import java.util.HashMap;
import java.util.Map;

public class ValidationArgs {

  private Map<String, Object> data;

  private ValidationArgs() {
    this.data = new HashMap<>();
  }

  public static ValidationArgs newArgs() {
    return new ValidationArgs();
  }

  public ValidationArgs addArgument(String key, Object value) {
    this.data.put(key.toLowerCase(), value);
    return this;
  }

  public <T> T getArgument(String key, Class<T> clazz) {
    T result = null;
    try {
      Object rawResult = this.data.get(key.toLowerCase());
      if (rawResult != null) {
        result = clazz.cast(rawResult);
      }
    } catch (ClassCastException e) {
      // nothing to do, return a null value
    }
    return result;
  }
}
