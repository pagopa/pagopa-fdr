package it.gov.pagopa.fdr.util.error.exception.persistence;

public class PersistenceFailureException extends Exception {

  public PersistenceFailureException(Exception e) {
    super(e);
  }
}
