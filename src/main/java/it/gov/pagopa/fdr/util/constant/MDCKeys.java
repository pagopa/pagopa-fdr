package it.gov.pagopa.fdr.util.constant;

public class MDCKeys {

  public static final String TRX_ID = "trxId";
  public static final String HTTP_TYPE = "httpType";
  public static final String EVENT_CATEGORY = "eventCategory";
  public static final String OUTCOME = "outcome";
  public static final String CODE = "code";
  public static final String MESSAGE = "message";
  public static final String ACTION = "action";
  public static final String URI = "uri";
  public static final String ELAPSED = "elapsed";
  public static final String STATUS_CODE = "statusCode";
  public static final String PSP_ID = "pspId";
  public static final String ORGANIZATION_ID = "organizationId";
  public static final String FDR = "fdr";
  public static final String FDR_STATUS = "fdrStatus";
  public static final String IUV = "iuv";
  public static final String IUR = "iur";
  public static final String IS_RE_ENABLED_FOR_THIS_CALL = "isReEnabledForThisCall";

  private MDCKeys() {
    throw new IllegalStateException("Logstash MDC custom keys class");
  }
}
