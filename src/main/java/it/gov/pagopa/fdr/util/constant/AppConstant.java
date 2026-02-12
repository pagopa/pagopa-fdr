package it.gov.pagopa.fdr.util.constant;

public class AppConstant {

  public static final String SERVICE_CODE_APP = "FDR";

  public static final String SERVICE_IDENTIFIER = "FDR03";

  public static final String REQUEST = "REQ";
  public static final String RESPONSE = "RES";

  public static final String OK = "OK";
  public static final String KO = "KO";

  public static final int MAX_PAYMENT = 20000;

  private AppConstant() {
    throw new IllegalStateException("Constants class");
  }
}
