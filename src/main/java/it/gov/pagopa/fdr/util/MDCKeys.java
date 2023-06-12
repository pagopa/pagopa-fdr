package it.gov.pagopa.fdr.util;

public class MDCKeys {

  public static final String TRX_ID = "trxId";
  public static final String ACTION = "action";
  public static final String PSP_ID = "pspId";
  public static final String EC_ID = "ecId";
  public static final String FLOW_NAME = "flowName";
  public static final String NDP = "ndp";

  private MDCKeys() {
    throw new IllegalStateException("Logstash MDC custom keys class");
  }
}
