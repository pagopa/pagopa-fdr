package it.gov.pagopa.fdr.rest.psps;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.Header;
import it.gov.pagopa.fdr.service.psps.PspsService;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openapi.quarkus.api_config_cache_json.model.BrokerPsp;
import org.openapi.quarkus.api_config_cache_json.model.Channel;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;
import org.openapi.quarkus.api_config_cache_json.model.CreditorInstitution;
import org.openapi.quarkus.api_config_cache_json.model.PaymentServiceProvider;
import org.openapi.quarkus.api_config_cache_json.model.PspChannelPaymentType;

@QuarkusTest
@QuarkusTestResource(MockServerTestResource.class)
public class PspResourceTest {

  private static final String reportingFlowName = "2016-08-16pspTest-1176";
  private static final String reportingFlowNamePspWrongFormat = "2016-08-16-psp-1176";
  private static final String reportingFlowNameDateWrongFormat = "2016-aa-16pspTest-1176";
  private static final String pspCode = "pspTest";
  private static final String pspCode2 = "pspTest2";
  private static final String pspCodeNotEnabled = "pspNotEnabled";
  private static final String brokerCode = "intTest";
  private static final String brokerCode2 = "intTest2";
  private static final String brokerCodeNotEnabled = "intNotEnabled";
  private static final String channelCode = "canaleTest";
  private static final String channelCodeNotEnabled = "canaleNotEnabled";
  private static final String ecCode = "12345678900";
  private static final String ecCodeNotEnabled = "00987654321";
  private static final String pspChannelPaymentTypeCode = "PAYPAL";
  private static final Header header = new Header("Content-Type", "application/json");


  private static String template = """
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

  String response = """
      {"message":"Flow [%s] saved"}""";

  @InjectMock
  PspsService pspsService;


  @BeforeEach
  public void setup() {
//    Mockito.doNothing().when(config).init();
//    Mockito.when(config.getClonedCache()).thenReturn(getConfig());

    Mockito.doNothing().when(pspsService).save(null);
  }

  @Test
  @DisplayName("PSPS create OK")
  public void testPspOk() {
    RandomGenerator randomGenerator = new Random();
    String flowName = reportingFlowName.substring(0, reportingFlowName.length()-4)+randomGenerator.nextInt(1111,9999);
    String url = "/psps/%s/flows".formatted(pspCode);
    String bodyFmt =
        template.formatted(flowName, pspCode, brokerCode, channelCode, ecCode);
    String responseFmt = response.formatted(flowName);

    given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(201)
        .body(containsString(responseFmt));
  }

  @Test
  @DisplayName("PSPS create KO FDR-0704")
  public void test_psp_KO_FDR0704() {
    String pspNotMatch = "PSP_NOT_MATCH";

    String url = "/psps/%s/flows".formatted(pspCode);
    String bodyFmt =
        template.formatted(reportingFlowName, pspNotMatch, brokerCode, channelCode, ecCode);
    String responseFmt =
        """
        {"httpStatusCode":400,"httpStatusDescription":"Bad Request","appErrorCode":"FDR-0704","errors":[{"message":"Reporting flow [2016-08-16pspTest-1176] have sender.pspId [PSP_NOT_MATCH] but not match with query param [pspTest]"}]}""";

    given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .body(containsString(responseFmt));
  }

  @Test
  @DisplayName("PSPS create KO FDR-0708")
  public void test_psp_KO_FDR0708() {

    String pspUnknown = "PSP_UNKNOWN";

    String url = "/psps/%s/flows".formatted(pspUnknown);
    String bodyFmt =
        template.formatted(reportingFlowName, pspUnknown, brokerCode, channelCode, ecCode);
    String responseFmt =
        """
        {"httpStatusCode":400,"httpStatusDescription":"Bad Request","appErrorCode":"FDR-0708","errors":[{"message":"Psp [PSP_UNKNOWN] unknown"}]}""";

    given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .body(containsString(responseFmt));
  }

  @Test
  @DisplayName("PSPS create KO FDR-0709")
  public void test_psp_KO_FDR0709() {
    //TODO replicare la config sul mock json per far funzionare il test

    String url = "/psps/%s/flows".formatted(pspCodeNotEnabled);
    String bodyFmt =
        template.formatted(reportingFlowName, pspCodeNotEnabled, brokerCode, channelCode, ecCode);
    String responseFmt =
        """
        {"httpStatusCode":400,"httpStatusDescription":"Bad Request","appErrorCode":"FDR-0709","errors":[{"message":"Psp [pspNotEnabled] not enabled"}]}""";

    given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .body(containsString(responseFmt));
  }

  @Test
  @DisplayName("PSPS create KO FDR-0710")
  public void test_brokerpsp_KO_FDR0710() {
    String brokerPspUnknown = "BROKERPSP_UNKNOWN";

    String url = "/psps/%s/flows".formatted(pspCode);
    String bodyFmt =
        template.formatted(reportingFlowName, pspCode, brokerPspUnknown, channelCode, ecCode);
    String responseFmt =
        """
        {"httpStatusCode":400,"httpStatusDescription":"Bad Request","appErrorCode":"FDR-0710","errors":[{"message":"Broker [BROKERPSP_UNKNOWN] unknown"}]}""";

    given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .body(containsString(responseFmt));
  }

  @Test
  @DisplayName("PSPS create KO FDR-0711")
  public void test_brokerpsp_KO_FDR0711() {
    String url = "/psps/%s/flows".formatted(pspCode);
    String bodyFmt =
        template.formatted(reportingFlowName, pspCode, brokerCodeNotEnabled, channelCode, ecCode);
    String responseFmt =
        """
        {"httpStatusCode":400,"httpStatusDescription":"Bad Request","appErrorCode":"FDR-0711","errors":[{"message":"Broker [intNotEnabled] not enabled"}]}""";

    given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .body(containsString(responseFmt));
  }

  @Test
  @DisplayName("PSPS create KO FDR-0712")
  public void test_channel_KO_FDR0712() {
    String channelUnknown = "CHANNEL_UNKNOWN";

    String url = "/psps/%s/flows".formatted(pspCode);
    String bodyFmt =
        template.formatted(reportingFlowName, pspCode, brokerCode, channelUnknown, ecCode);
    String responseFmt =
        """
        {"httpStatusCode":400,"httpStatusDescription":"Bad Request","appErrorCode":"FDR-0712","errors":[{"message":"Channel [CHANNEL_UNKNOWN] unknown"}]}""";

    given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .body(containsString(responseFmt));
  }

  @Test
  @DisplayName("PSPS create KO FDR-0713")
  public void test_channel_KO_FDR0713() {
    String url = "/psps/%s/flows".formatted(pspCode);
    String bodyFmt =
        template.formatted(reportingFlowName, pspCode, brokerCode, channelCodeNotEnabled, ecCode);
    String responseFmt =
        """
        {"httpStatusCode":400,"httpStatusDescription":"Bad Request","appErrorCode":"FDR-0713","errors":[{"message":"channelId.notEnabled"}]}""";

    given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .body(containsString(responseFmt));
  }

  @Test
  @DisplayName("PSPS create KO FDR-0714")
  public void test_channelBroker_KO_FDR0714() {
    String url = "/psps/%s/flows".formatted(pspCode);
    String bodyFmt =
        template.formatted(reportingFlowName, pspCode, brokerCode2, channelCode, ecCode);
    String responseFmt =
        """
        {"httpStatusCode":400,"httpStatusDescription":"Bad Request","appErrorCode":"FDR-0714","errors":[{"message":"Channel [canaleTest] with broker [intTest2] not authorized"}]}""";

    given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .body(containsString(responseFmt));
  }

  @Test
  @DisplayName("PSPS create KO FDR-0715")
  public void test_channelPsp_KO_FDR0715() {
    String url = "/psps/%s/flows".formatted(pspCode2);
    String bodyFmt =
        template.formatted(reportingFlowName, pspCode2, brokerCode, channelCode, ecCode);
    String responseFmt =
        """
        {"httpStatusCode":400,"httpStatusDescription":"Bad Request","appErrorCode":"FDR-0715","errors":[{"message":"Channel [canaleTest] with psp [pspTest2] not authorized"}]}""";

    given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .body(containsString(responseFmt));
  }

//  EC_NOT_ENABLED("0717", "ecId.notEnabled", RestResponse.Status.BAD_REQUEST),

  @Test
  @DisplayName("PSPS create KO FDR-0716")
  public void test_ecId_KO_FDR0716() {
    String ecUnknown = "EC_UNKNOWN";
    String url = "/psps/%s/flows".formatted(pspCode);
    String bodyFmt =
        template.formatted(reportingFlowName, pspCode, brokerCode, channelCode, ecUnknown);
    String responseFmt =
        """
        {"httpStatusCode":400,"httpStatusDescription":"Bad Request","appErrorCode":"FDR-0716","errors":[{"message":"Creditor institution [EC_UNKNOWN] unknown"}]}""";

    given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .body(containsString(responseFmt));
  }

  @Test
  @DisplayName("PSPS create KO FDR-0717")
  public void test_ecId_KO_FDR0717() {
    String url = "/psps/%s/flows".formatted(pspCode);
    String bodyFmt =
        template.formatted(reportingFlowName, pspCode, brokerCode, channelCode, ecCodeNotEnabled);
    String responseFmt =
        """
        {"httpStatusCode":400,"httpStatusDescription":"Bad Request","appErrorCode":"FDR-0717","errors":[{"message":"Creditor institution [00987654321] not enabled"}]}""";

    given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .body(containsString(responseFmt));
  }

  @Test
  @DisplayName("PSPS create KO FDR-0718")
  public void test_flowName_KO_FDR0718() {
    String url = "/psps/%s/flows".formatted(pspCode);
    String bodyFmt =
        template.formatted(reportingFlowNameDateWrongFormat, pspCode, brokerCode, channelCode, ecCode);
    String responseFmt =
        """
        {"httpStatusCode":400,"httpStatusDescription":"Bad Request","appErrorCode":"FDR-0718","errors":[{"message":"Reporting flow [2016-aa-16pspTest-1176] has wrong date"}]}""";

    given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .body(containsString(responseFmt));
  }

  @Test
  @DisplayName("PSPS create KO FDR-0719")
  public void test_flowName_KO_FDR0719() {
    String url = "/psps/%s/flows".formatted(pspCode);
    String bodyFmt =
        template.formatted(reportingFlowNamePspWrongFormat, pspCode, brokerCode, channelCode, ecCode);
    String responseFmt =
        """
        {"httpStatusCode":400,"httpStatusDescription":"Bad Request","appErrorCode":"FDR-0719","errors":[{"message":"Reporting flow [2016-08-16-psp-1176] has wrong psp"}]}""";

    given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
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
