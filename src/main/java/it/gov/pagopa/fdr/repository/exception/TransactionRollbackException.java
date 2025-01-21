package it.gov.pagopa.fdr.repository.exception;

public class TransactionRollbackException extends RuntimeException {

  public TransactionRollbackException(Exception e) {
    super(e);
  }
}
