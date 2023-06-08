package it.gov.pagopa.fdr.test.util;

import io.restassured.http.Header;

public class AppConstantTestHelper {

  public static final String FLOWS_URL = "/psps/%s/flows";
  public static final String FLOWS_PUBLISH_URL = "/psps/%s/flows/%s/publish";
  public static final String FLOWS_DELETE_URL = "/psps/%s/flows/%s";
  public static final String PAYMENTS_ADD_URL = "/psps/%s/flows/%s/payments/add";
  public static final String PAYMENTS_DELETE_URL = "/psps/%s/flows/%s/payments/del";

  public static final String REPORTING_FLOW_NAME_PSP_WRONG_FORMAT = "2016-08-16-psp-1176";
  public static final String REPORTING_FLOW_NAME_DATE_WRONG_FORMAT = "2016-aa-16pspTest-1176";
  public static final String PSP_CODE = "pspTest";
  public static final String PSP_CODE_2 = "pspTest2";
  public static final String PSP_CODE_NOT_ENABLED = "pspNotEnabled";
  public static final String BROKER_CODE = "intTest";
  public static final String BROKER_CODE_2 = "intTest2";
  public static final String BROKER_CODE_NOT_ENABLED = "intNotEnabled";
  public static final String CHANNEL_CODE = "canaleTest";
  public static final String CHANNEL_CODE_NOT_ENABLED = "canaleNotEnabled";
  public static final String EC_CODE = "12345678900";
  public static final String EC_CODE_NOT_ENABLED = "00987654321";

  public static final Header HEADER = new Header("Content-Type", "application/json");
}
