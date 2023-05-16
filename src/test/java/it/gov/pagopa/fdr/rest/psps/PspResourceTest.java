package it.gov.pagopa.fdr.rest.psps;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.Header;
import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.service.psps.PspsService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openapi.quarkus.api_config_cache_json.model.*;

@QuarkusTest
public class PspResourceTest {

  private static String reportingFlowName = "2016-08-16pspLorenz-1176";
  private static String pspCode = "pspLorenz";
  private static String pspCodeNotEnabled = "pspNotEnabled";
  private static String brokerCode = "intLorenz";
  private static String channelCode = "canaleLorenz";
  private static String ecCode = "12345678900";

  private static String pspChannelPaymentTypeCode = "PAYPALL";

  private static Header header = new Header("Content-Type", "application/json");
  private static String template =
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
      }""";

  String response = """
      {"message":"Flow [2016-08-16pspLorenz-1176] saved"}""";

  @InjectMock
  Config config;

  @InjectMock
  PspsService pspsService;


  @BeforeEach
  public void setup() {
    Mockito.doNothing().when(config).init();
    Mockito.when(config.getClonedCache()).thenReturn(getConfig());

    Mockito.doNothing().when(pspsService).save(null);
  }

  @Test
  @DisplayName("PSPS create OK")
  public void testPspOk() {
    String url = "/psps/%s/flows".formatted(pspCode);
    String bodyFmt =
        template.formatted(reportingFlowName, pspCode, brokerCode, channelCode, ecCode);
    String responseFmt = response.formatted(reportingFlowName);

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
        {"httpStatusCode":400,"httpStatusDescription":"Bad Request","appErrorCode":"FDR-0704","errors":[{"message":"Reporting flow [2016-08-16pspLorenz-1176] have sender.pspId [PSP_NOT_MATCH] but not match with query param [pspLorenz]"}]}""";

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

  private static ConfigDataV1 getConfig() {
    PaymentServiceProvider paymentServiceProvider = new PaymentServiceProvider();
    paymentServiceProvider.setEnabled(true);
    paymentServiceProvider.setPspCode(pspCode);

    PaymentServiceProvider paymentServiceProviderNotEnabled = new PaymentServiceProvider();
    paymentServiceProviderNotEnabled.setEnabled(false);
    paymentServiceProviderNotEnabled.setPspCode(pspCodeNotEnabled);

    Map<String, PaymentServiceProvider> psps = new LinkedHashMap<>();
    psps.put(pspCode, paymentServiceProvider);
    psps.put(pspCodeNotEnabled, paymentServiceProviderNotEnabled);

    BrokerPsp brokerPsp = new BrokerPsp();
    brokerPsp.setEnabled(true);
    brokerPsp.setBrokerPspCode(brokerCode);
    Map<String, BrokerPsp> pspBrokers = new LinkedHashMap<>();
    pspBrokers.put(brokerCode, brokerPsp);

    Channel channel = new Channel();
    channel.setEnabled(true);
    channel.setBrokerPspCode(brokerCode);
    channel.setChannelCode(channelCode);
    Map<String, Channel> channels = new LinkedHashMap<>();
    channels.put(channelCode, channel);

    PspChannelPaymentType pspChannelPaymentType = new PspChannelPaymentType();
    pspChannelPaymentType.setPspCode(pspCode);
    pspChannelPaymentType.setChannelCode(channelCode);
    Map<String, PspChannelPaymentType> pspChannelPaymentTypeLinkedHashMap = new LinkedHashMap<>();
    pspChannelPaymentTypeLinkedHashMap.put(pspChannelPaymentTypeCode, pspChannelPaymentType);

    CreditorInstitution creditorInstitution = new CreditorInstitution();
    creditorInstitution.setEnabled(true);
    Map<String, CreditorInstitution> creditorInstitutionMap = new LinkedHashMap<>();
    creditorInstitutionMap.put(ecCode, creditorInstitution);

    ConfigDataV1 configDataV1 = new ConfigDataV1();
    configDataV1.setPsps(psps);
    configDataV1.setPspBrokers(pspBrokers);
    configDataV1.setChannels(channels);
    configDataV1.setPspChannelPaymentTypes(pspChannelPaymentTypeLinkedHashMap);
    configDataV1.setCreditorInstitutions(creditorInstitutionMap);

    return configDataV1;
  }
}
