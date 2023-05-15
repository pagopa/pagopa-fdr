package it.gov.pagopa.fdr.rest.psps;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.Header;
import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.service.psps.PspsService;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openapi.quarkus.api_config_cache_json.model.*;

@QuarkusTest
public class PspResourceTest {

  private static String reportingFlowName = "2016-08-16pspLorenz-1176";
  private static String pspCode = "pspLorenz";
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

  @BeforeAll
  public static void setup() throws URISyntaxException {
    Config configMock = Mockito.mock(Config.class);
    Mockito.doNothing().when(configMock).init();
    Mockito.when(configMock.getClonedCache()).thenReturn(getConfig());
    QuarkusMock.installMockForType(configMock, Config.class);

    PspsService serviceMock = Mockito.mock(PspsService.class);
    Mockito.doNothing().when(serviceMock).save(null);
    QuarkusMock.installMockForType(serviceMock, PspsService.class);
  }

  @Test
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
  public void testPspKo() {
    String pspUnknown = "PSP_UNKNOWN";

    String url = "/psps/%s/flows".formatted(pspCode);
    String bodyFmt =
        template.formatted(reportingFlowName, pspUnknown, brokerCode, channelCode, ecCode);
    String responseFmt =
        """
        {"httpStatusCode":400,"httpStatusDescription":"Bad Request","appErrorCode":"FDR-0704","errors":[{"message":"Reporting flow [%s] have sender.pspId [%s] but not match with query param [pspLorenz]"}]}"""
            .formatted(reportingFlowName, pspUnknown);

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
    Map<String, PaymentServiceProvider> psps = new LinkedHashMap<>();
    psps.put(pspCode, paymentServiceProvider);

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
