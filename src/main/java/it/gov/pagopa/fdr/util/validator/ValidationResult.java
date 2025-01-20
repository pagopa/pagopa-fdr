package it.gov.pagopa.fdr.util.validator;

import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import lombok.Getter;

public class ValidationResult {

  private final boolean isValid;

  @Getter private final AppErrorCodeMessageEnum error;

  @Getter private final String[] errorArgs;

  private ValidationResult(boolean isValid, AppErrorCodeMessageEnum error, String... errorArgs) {
    this.isValid = isValid;
    this.error = error;
    this.errorArgs = errorArgs;
  }

  public static ValidationResult asValid() {
    return new ValidationResult(true, null, null);
  }

  public static ValidationResult asInvalid(AppErrorCodeMessageEnum error, String... errorArgs) {
    return new ValidationResult(false, error, errorArgs);
  }

  public boolean isValid() {
    return this.isValid;
  }

  public boolean isInvalid() {
    return !isValid();
  }
}
