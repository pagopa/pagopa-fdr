package it.gov.pagopa.fdr.repository.exception;

public class PersistenceFailureException extends Exception {

  public PersistenceFailureException(Exception e) {
    super(e);
  }
}
