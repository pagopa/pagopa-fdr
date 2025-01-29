package it.gov.pagopa.fdr.util.validator;

public abstract class ValidationStep {

  private ValidationStep nextStep;

  /**
   * @param nextStep
   * @return
   */
  public ValidationStep linkTo(ValidationStep nextStep) {

    // no next step already defined, can link to this step
    if (this.nextStep == null) {

      this.nextStep = nextStep;
    }

    /*
    Next step was already defined in previous invocation,
    add it as last step for the validation chain's last-ring
     */
    else {

      ValidationStep analyzedStep = this.nextStep;
      while (analyzedStep.nextStep != null) {
        analyzedStep = analyzedStep.nextStep;
      }
      analyzedStep.nextStep = nextStep;
    }

    return this;
  }

  /**
   * @param args
   * @return
   */
  protected ValidationResult checkNext(ValidationArgs args) {

    ValidationResult result;
    if (nextStep == null) {
      result = ValidationResult.asValid();
    } else {
      result = nextStep.validate(args);
    }
    return result;
  }

  /**
   * @param args
   * @return
   */
  public abstract ValidationResult validate(ValidationArgs args);
}
