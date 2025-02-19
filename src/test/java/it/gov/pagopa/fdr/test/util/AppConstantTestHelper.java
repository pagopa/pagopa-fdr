package it.gov.pagopa.fdr.test.util;

import io.restassured.http.Header;
import it.gov.pagopa.fdr.util.constant.ControllerConstants;
import java.nio.file.Paths;

public class AppConstantTestHelper {

  public static final String FLOWS_URL = "/psps/%s/fdrs/%s";
  public static final String FLOWS_PUBLISH_URL = "/psps/%s/fdrs/%s/publish";
  public static final String FLOWS_DELETE_URL = "/psps/%s/fdrs/%s";
  public static final String PAYMENTS_ADD_URL = "/psps/%s/fdrs/%s/payments/add";
  public static final String PAYMENTS_DELETE_URL = "/psps/%s/fdrs/%s/payments/del";

  //Internal operations Controller Paths
  public static final String INTERNAL_FLOWS_URL = "/internal"+FLOWS_URL;
  public static final String INTERNAL_FLOWS_PUBLISH_URL = "/internal"+FLOWS_PUBLISH_URL;
  public static final String INTERNAL_FLOWS_DELETE_URL = "/internal"+FLOWS_DELETE_URL;
  public static final String INTERNAL_PAYMENTS_ADD_URL = "/internal"+PAYMENTS_ADD_URL;
  public static final String INTERNAL_PAYMENTS_DELETE_URL = "/internal"+PAYMENTS_DELETE_URL;

  //Organizations Controller Paths
  public static final String ORGANIZATIONS_GET_ALL_PUBLISHED_FLOW_URL =
          "/organizations/%s/fdrs?" + ControllerConstants.PARAMETER_PSP + "=%s";
  public static final String ORGANIZATIONS_GET_REPORTING_FLOW_URL =
          "/organizations/%s/fdrs/%s/revisions/%s/psps/%s";
  public static final String ORGANIZATIONS_GET_REPORTING_FLOW_PAYMENTS_URL =
          "/organizations/%s/fdrs/%s/revisions/%s/psps/%s/payments";

  //PSP Controller Paths
  public static final String PSP_GET_PAYMENTS_FDR_PUBLISHED_URL =
          "/psps/%s/published/fdrs/%s/revisions/%s/organizations/%s/payments";
  public static final String PSP_GET_FDR_PUBLISHED_URL =
          "/psps/%s/published/fdrs/%s/revisions/%s/organizations/%s";
  public static final String PSP_GET_ALL_FDR_CREATED_URL = "/psps/%s/created";
  public static final String PSP_GET_PAYMENTS_FDR_CREATED_URL =
          "/psps/%s/created/fdrs/%s/organizations/%s/payments";

  //TechnicalSupportController Paths
  public static final String GET_ALL_FDR_BY_PSP_ID_IUV = "/internal/psps/" + "%s" + "/iuv/" + "%s";
  public static final String GET_ALL_FDR_BY_PSP_ID_IUR = "/internal/psps/" + "%s" + "/iur/" + "%s";


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
  public static final String IUR_CODE="abcdefg";
  public static final String IUV_CODE_A ="a";
  public static final String IUV_CODE_B ="b";
  public static final String IUV_CODE_C ="c";
  public static final String IUV_CODE_D ="d";
  public static final String IUV_CODE_E ="e";

  public static final Header HEADER = new Header("Content-Type", "application/json");

  public static final String APP_NAME = "pagopafdr";

  //  json template paths section
  public static final String JSON_TEST_TEMPLATES_PATH = "json-test-templates";

  public static final String GENERAL_TEMPLATE_PATH =
          Paths.get(JSON_TEST_TEMPLATES_PATH, "general").toString();

  public static final String TEST_UTIL_PATH =
          Paths.get(JSON_TEST_TEMPLATES_PATH, "test-util").toString();

  //Test Util Path
  public static final String FLOW_TEMPLATE_PATH =
          Paths.get(TEST_UTIL_PATH
                  ,"flow-template.json").toString();

  public static String PAYMENTS_ADD_TEMPLATE_PATH =
          Paths.get(TEST_UTIL_PATH
                  ,"payments-add-template.json").toString();

  public static String PAYMENTS_ADD_TEMPLATE_2_PATH =
          Paths.get(TEST_UTIL_PATH
                  ,"payments-add-template-2.json").toString();

  //General template paths
  public static final String FLOW_TEMPLATE_WRONG_INSTANT_PATH =
          Paths.get(GENERAL_TEMPLATE_PATH
                  ,"flow-wrong-instant.json").toString();
  public static final String TEST_TEMPLATE_PATH =
          Paths.get(GENERAL_TEMPLATE_PATH
                  ,"test.json").toString();
  public static final String FLOW_TEMPLATE_WRONG_FIELDS_PATH =
          Paths.get(GENERAL_TEMPLATE_PATH
                  ,"flow-wrong-fields.json").toString();
  public static final String PAYMENTS_ADD_INVALID_FIELD_VALUE_TEMPLATE_PATH =
          Paths.get(GENERAL_TEMPLATE_PATH
                  ,"payments-add-invalid-value.json").toString();

  public static final String MALFORMED_JSON_PATH =
          Paths.get(GENERAL_TEMPLATE_PATH
                  ,"malformed.json").toString();
  public static final String PAYMENTS_DELETE_WRONG_TEMPLATE_PATH =
          Paths.get(GENERAL_TEMPLATE_PATH
                  ,"payments-delete-wrong-format.json").toString();
  public static final String PAYMENTS_SAME_INDEX_ADD_TEMPLATE_PATH =
          Paths.get(GENERAL_TEMPLATE_PATH
                  ,"payments-add-same-index.json").toString();
  public static final String PAYMENTS_2_ADD_TEMPLATE_PATH =
          Paths.get(GENERAL_TEMPLATE_PATH
                  ,"payments-add.json").toString();

  public static final String PAYMENTS_ADD_INVALID_FORMAT_VALUE_TEMPLATE_PATH =
          Paths.get(GENERAL_TEMPLATE_PATH
                  ,"payments-add-invalid-format-value.json").toString();

  public static final String PAYMENTS_ADD_INVALID_FORMAT_TEMPLATE_PATH =
          Paths.get(GENERAL_TEMPLATE_PATH
                  ,"payments-add-invalid-format.json").toString();

  //  Internal operations template paths
  public static final String INTERNAL_OPERATION_TEMPLATE_PATH =
          Paths.get(JSON_TEST_TEMPLATES_PATH, "internal-operation").toString();

  public static final String INTERNAL_OPERATION_PAYMENTS_DELETE_TEMPLATE_PATH =
          Paths.get(INTERNAL_OPERATION_TEMPLATE_PATH
                  ,"payments-delete.json").toString();

  //  Psp template paths
  public static final String PSP_TEMPLATE_PATH =
          Paths.get(JSON_TEST_TEMPLATES_PATH, "psp").toString();

  public static final String PSP_PAYMENTS_DELETE_TEMPLATE_PATH =
          Paths.get(PSP_TEMPLATE_PATH
                  ,"payments-delete.json").toString();

  public static final String PSP_PAYMENTS_DELETE_SAME_INDEX_TEMPLATE_PATH =
          Paths.get(PSP_TEMPLATE_PATH
                  ,"payments-delete-same-index.json").toString();

}
