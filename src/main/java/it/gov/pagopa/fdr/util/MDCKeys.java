package it.gov.pagopa.fdr.util;

public class MDCKeys {

  public static final String TRX_ID = "trxId";
  public static final String ACTION = "action";
  public static final String PSP_ID = "pspId";
  public static final String ORGANIZATION_ID = "organizationId";
  public static final String FDR = "fdr";
  public static final String IUV= "iuv";
  public static final String IUR= "iur";

  private MDCKeys() {
    throw new IllegalStateException("Logstash MDC custom keys class");
  }
}
