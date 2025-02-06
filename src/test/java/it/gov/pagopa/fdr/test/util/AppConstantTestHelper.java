package it.gov.pagopa.fdr.test.util;

import io.restassured.http.Header;

public class AppConstantTestHelper {

  public static final String FLOWS_URL = "/psps/%s/fdrs/%s";
  public static final String FLOWS_PUBLISH_URL = "/psps/%s/fdrs/%s/publish";
  public static final String FLOWS_DELETE_URL = "/psps/%s/fdrs/%s";
  public static final String PAYMENTS_ADD_URL = "/psps/%s/fdrs/%s/payments/add";
  public static final String PAYMENTS_DELETE_URL = "/psps/%s/fdrs/%s/payments/del";

  public static final String INTERNAL_FLOWS_URL = "/internal"+FLOWS_URL;
  public static final String INTERNAL_FLOWS_PUBLISH_URL = "/internal"+FLOWS_PUBLISH_URL;
  public static final String INTERNAL_FLOWS_DELETE_URL = "/internal"+FLOWS_DELETE_URL;
  public static final String INTERNAL_PAYMENTS_ADD_URL = "/internal"+PAYMENTS_ADD_URL;
  public static final String INTERNAL_PAYMENTS_DELETE_URL = "/internal"+PAYMENTS_DELETE_URL;


  public static final String REPORTING_FLOW_NAME_PSP_WRONG_FORMAT = "2016-08-16-psp-1176";
  public static final String REPORTING_FLOW_NAME_DATE_WRONG_FORMAT = "2016-aa-1660000000001-1176";
  public static final String PSP_CODE = "60000000001";
  public static final String PSP_CODE_2 = "idPsp1";
  public static final String PSP_CODE_3 = "idPsp2";
  public static final String PSP_CODE_NOT_ENABLED = "NOT_ENABLED";
  public static final String BROKER_CODE = "60000000001";
  public static final String BROKER_CODE_2 = "INTPSPtest1";
  public static final String BROKER_CODE_NOT_ENABLED = "INT_NOT_ENABLED";
  public static final String CHANNEL_CODE = "15376371009_04";
  public static final String CHANNEL_CODE_NOT_ENABLED = "CANALE_NOT_ENABLED";
  public static final String EC_CODE = "15376371009";
  public static final String EC_CODE_NOT_ENABLED = "PAtestDOFF";

  public static final Header HEADER = new Header("Content-Type", "application/json");
}
