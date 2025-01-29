package it.gov.pagopa.fdr.util.error.exception.persistence;

public class TransactionRollbackException extends RuntimeException {

  public TransactionRollbackException(Exception e) {
    super(e);
  }
}
