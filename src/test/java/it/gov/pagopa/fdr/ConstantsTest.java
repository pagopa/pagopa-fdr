package it.gov.pagopa.fdr;

import io.restassured.http.Header;

public class ConstantsTest {

  public static final String reportingFlowName = "2016-08-16pspTest-1176";
  public static final String reportingFlowNamePspWrongFormat = "2016-08-16-psp-1176";
  public static final String reportingFlowNameDateWrongFormat = "2016-aa-16pspTest-1176";
  public static final String pspCode = "pspTest";
  public static final String pspCode2 = "pspTest2";
  public static final String pspCodeNotEnabled = "pspNotEnabled";
  public static final String brokerCode = "intTest";
  public static final String brokerCode2 = "intTest2";
  public static final String brokerCodeNotEnabled = "intNotEnabled";
  public static final String channelCode = "canaleTest";
  public static final String channelCodeNotEnabled = "canaleNotEnabled";
  public static final String ecCode = "12345678900";
  public static final String ecCodeNotEnabled = "00987654321";
  public static final String pspChannelPaymentTypeCode = "PAYPAL";
  public static final Header header = new Header("Content-Type", "application/json");

  public static final String flowsUrl = "/psps/%s/flows";
  public static final String flowsPublishUrl = "/psps/%s/flows/%s/publish";
  public static final String paymentsAddUrl = "/psps/%s/flows/%s/payments/add";
  public static final String paymentsDelUrl = "/psps/%s/flows/%s/payments/del";
  public static final String organizationFindByIdEcUrl =
      "/organizations/%s/flows?idPsp=%s&page=%d&size=%d";
  public static final String organizationfindByReportingFlowNameUrl =
      "/organizations/%s/flows/%s/psps/%s";
}
