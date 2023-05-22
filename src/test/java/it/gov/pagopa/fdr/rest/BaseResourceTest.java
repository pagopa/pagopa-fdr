package it.gov.pagopa.fdr.rest;

import static io.restassured.RestAssured.given;
import static it.gov.pagopa.fdr.ConstantsTest.brokerCode;
import static it.gov.pagopa.fdr.ConstantsTest.brokerCode2;
import static it.gov.pagopa.fdr.ConstantsTest.brokerCodeNotEnabled;
import static it.gov.pagopa.fdr.ConstantsTest.channelCode;
import static it.gov.pagopa.fdr.ConstantsTest.channelCodeNotEnabled;
import static it.gov.pagopa.fdr.ConstantsTest.ecCode;
import static it.gov.pagopa.fdr.ConstantsTest.ecCodeNotEnabled;
import static it.gov.pagopa.fdr.ConstantsTest.flowsPublishUrl;
import static it.gov.pagopa.fdr.ConstantsTest.flowsUrl;
import static it.gov.pagopa.fdr.ConstantsTest.header;
import static it.gov.pagopa.fdr.ConstantsTest.paymentsAddUrl;
import static it.gov.pagopa.fdr.ConstantsTest.pspChannelPaymentTypeCode;
import static it.gov.pagopa.fdr.ConstantsTest.pspCode;
import static it.gov.pagopa.fdr.ConstantsTest.pspCode2;
import static it.gov.pagopa.fdr.ConstantsTest.pspCodeNotEnabled;
import static it.gov.pagopa.fdr.ConstantsTest.reportingFlowName;
import static org.hamcrest.Matchers.containsString;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.random.RandomGenerator;
import org.openapi.quarkus.api_config_cache_json.model.BrokerPsp;
import org.openapi.quarkus.api_config_cache_json.model.Channel;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;
import org.openapi.quarkus.api_config_cache_json.model.CreditorInstitution;
import org.openapi.quarkus.api_config_cache_json.model.PaymentServiceProvider;
import org.openapi.quarkus.api_config_cache_json.model.PspChannelPaymentType;

public class BaseResourceTest {

  protected static String flowTemplate =
      """
        {
          "reportingFlowName": "%s",
          "reportingFlowDate": "2023-04-05T09:21:37.810000Z",
          "sender": {
            "type": "LEGAL_PERSON",
            "id": "SELBIT2B",
            "pspId": "%s",
            "pspName": "Bank",
            "brokerId": "%s",
            "channelId": "%s",
            "password": "1234567890"
          },
          "receiver": {
            "id": "APPBIT2B",
            "ecId": "%s",
            "ecName": "Comune di xyz"
          },
          "regulation": "SEPA - Bonifico xzy",
          "regulationDate": "2023-04-03T12:00:30.900000Z",
          "bicCodePouringBank": "UNCRITMMXXX"
        }
        """;

  protected static String paymentsTemplate =
      """
      {
        "payments": [{
            "iuv": "a",
            "iur": "abcdefg",
            "index": 1,
            "pay": 0.01,
            "payStatus": "EXECUTED",
            "payDate": "2023-02-03T12:00:30.900000Z"
          },{
            "iuv": "b",
            "iur": "abcdefg",
            "index": 2,
            "pay": 0.01,
            "payStatus": "EXECUTED",
            "payDate": "2023-02-03T12:00:30.900000Z"
          },{
            "iuv": "c",
            "iur": "abcdefg",
            "index": 3,
            "pay": 0.01,
            "payStatus": "EXECUTED",
            "payDate": "2023-02-03T12:00:30.900000Z"
          }
        ]
      }
      """;

  String response = """
      {
        "message":"Flow [%s] saved"
      }
      """;

  String flowsPublishedResponse =
      """
      {
        "message":"Flow [%s] published"
      }
      """;

  String paymentsAddResponse =
      """
      {
        "message":"Flow [%s] payment added"
      }
     """;

  String paymentsDelResponse =
      """
      {
        "message":"Flow [%s] payment deleted"
      }
      """;

  protected String getFlowName() {
    RandomGenerator randomGenerator = new Random();
    return reportingFlowName.substring(0, reportingFlowName.length() - 4)
        + randomGenerator.nextInt(1111, 9999);
  }

  protected void pspSunnyDay(String flowName) {
    String url = flowsUrl.formatted(pspCode);
    String bodyFmt = flowTemplate.formatted(flowName, pspCode, brokerCode, channelCode, ecCode);
    String responseFmt = response.formatted(flowName);

    given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(201)
        .body(containsString(responseFmt));

    url = paymentsAddUrl.formatted(pspCode, flowName);
    bodyFmt = paymentsTemplate;
    responseFmt = paymentsAddResponse.formatted(flowName);
    given()
        .body(bodyFmt)
        .header(header)
        .when()
        .put(url)
        .then()
        .statusCode(200)
        .body(containsString(responseFmt));

    url = flowsPublishUrl.formatted(pspCode, flowName);
    responseFmt = flowsPublishedResponse.formatted(flowName);
    given()
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(200)
        .body(containsString(responseFmt));
  }

  private static ConfigDataV1 getConfig() {
    PaymentServiceProvider paymentServiceProvider = new PaymentServiceProvider();
    paymentServiceProvider.setEnabled(true);
    paymentServiceProvider.setPspCode(pspCode);

    PaymentServiceProvider paymentServiceProviderNotEnabled = new PaymentServiceProvider();
    paymentServiceProviderNotEnabled.setEnabled(false);
    paymentServiceProviderNotEnabled.setPspCode(pspCodeNotEnabled);

    PaymentServiceProvider paymentServiceProvider2 = new PaymentServiceProvider();
    paymentServiceProvider2.setEnabled(true);
    paymentServiceProvider2.setPspCode(pspCode2);

    Map<String, PaymentServiceProvider> psps = new LinkedHashMap<>();
    psps.put(pspCode, paymentServiceProvider);
    psps.put(pspCode2, paymentServiceProvider2);
    psps.put(pspCodeNotEnabled, paymentServiceProviderNotEnabled);

    BrokerPsp brokerPsp = new BrokerPsp();
    brokerPsp.setEnabled(true);
    brokerPsp.setBrokerPspCode(brokerCode);

    BrokerPsp brokerPsp2 = new BrokerPsp();
    brokerPsp2.setEnabled(true);
    brokerPsp2.setBrokerPspCode(brokerCode2);

    BrokerPsp brokerPspNotEnabled = new BrokerPsp();
    brokerPspNotEnabled.setEnabled(false);
    brokerPspNotEnabled.setBrokerPspCode(brokerCodeNotEnabled);

    Map<String, BrokerPsp> pspBrokers = new LinkedHashMap<>();
    pspBrokers.put(brokerCode, brokerPsp);
    pspBrokers.put(brokerCode2, brokerPsp2);
    pspBrokers.put(brokerCodeNotEnabled, brokerPspNotEnabled);

    Channel channel = new Channel();
    channel.setEnabled(true);
    channel.setBrokerPspCode(brokerCode);
    channel.setChannelCode(channelCode);

    Channel channelNotEnabled = new Channel();
    channelNotEnabled.setEnabled(false);
    channelNotEnabled.setBrokerPspCode(brokerCode);
    channelNotEnabled.setChannelCode(channelCodeNotEnabled);

    Map<String, Channel> channels = new LinkedHashMap<>();
    channels.put(channelCode, channel);
    channels.put(channelCodeNotEnabled, channelNotEnabled);

    PspChannelPaymentType pspChannelPaymentType = new PspChannelPaymentType();
    pspChannelPaymentType.setPspCode(pspCode);
    pspChannelPaymentType.setChannelCode(channelCode);
    Map<String, PspChannelPaymentType> pspChannelPaymentTypeLinkedHashMap = new LinkedHashMap<>();
    pspChannelPaymentTypeLinkedHashMap.put(pspChannelPaymentTypeCode, pspChannelPaymentType);

    CreditorInstitution creditorInstitution = new CreditorInstitution();
    creditorInstitution.setCreditorInstitutionCode(ecCode);
    creditorInstitution.setEnabled(true);

    CreditorInstitution creditorInstitutionNotEnabled = new CreditorInstitution();
    creditorInstitutionNotEnabled.setCreditorInstitutionCode(ecCodeNotEnabled);
    creditorInstitutionNotEnabled.setEnabled(false);

    Map<String, CreditorInstitution> creditorInstitutionMap = new LinkedHashMap<>();
    creditorInstitutionMap.put(ecCode, creditorInstitution);
    creditorInstitutionMap.put(ecCodeNotEnabled, creditorInstitutionNotEnabled);

    ConfigDataV1 configDataV1 = new ConfigDataV1();
    configDataV1.setPsps(psps);
    configDataV1.setPspBrokers(pspBrokers);
    configDataV1.setChannels(channels);
    configDataV1.setPspChannelPaymentTypes(pspChannelPaymentTypeLinkedHashMap);
    configDataV1.setCreditorInstitutions(creditorInstitutionMap);

    return configDataV1;
  }
}
