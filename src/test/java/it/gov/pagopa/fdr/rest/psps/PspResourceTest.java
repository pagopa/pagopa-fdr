package it.gov.pagopa.fdr.rest.psps;

import static io.restassured.RestAssured.given;
import static it.gov.pagopa.fdr.ConstantsTest.brokerCode;
import static it.gov.pagopa.fdr.ConstantsTest.brokerCode2;
import static it.gov.pagopa.fdr.ConstantsTest.brokerCodeNotEnabled;
import static it.gov.pagopa.fdr.ConstantsTest.channelCode;
import static it.gov.pagopa.fdr.ConstantsTest.channelCodeNotEnabled;
import static it.gov.pagopa.fdr.ConstantsTest.ecCode;
import static it.gov.pagopa.fdr.ConstantsTest.ecCodeNotEnabled;
import static it.gov.pagopa.fdr.ConstantsTest.flowsUrl;
import static it.gov.pagopa.fdr.ConstantsTest.header;
import static it.gov.pagopa.fdr.ConstantsTest.pspCode;
import static it.gov.pagopa.fdr.ConstantsTest.pspCode2;
import static it.gov.pagopa.fdr.ConstantsTest.pspCodeNotEnabled;
import static it.gov.pagopa.fdr.ConstantsTest.reportingFlowName;
import static it.gov.pagopa.fdr.ConstantsTest.reportingFlowNameDateWrongFormat;
import static it.gov.pagopa.fdr.ConstantsTest.reportingFlowNamePspWrongFormat;
import static org.hamcrest.Matchers.containsString;

import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.rest.BaseResourceTest;
import it.gov.pagopa.fdr.util.MongoResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(MockServerTestResource.class)
@QuarkusTestResource(MongoResource.class)
public class PspResourceTest extends BaseResourceTest {

  @Test
  @DisplayName("PSPS publish OK")
  public void testPspOk() {
    pspSunnyDay(getFlowName());
  }

  @Test
  @DisplayName("PSPS create KO FDR-0704")
  public void test_psp_KO_FDR0704() {
    String pspNotMatch = "PSP_NOT_MATCH";

    String url = flowsUrl.formatted(pspCode);
    String bodyFmt =
        flowTemplate.formatted(reportingFlowName, pspNotMatch, brokerCode, channelCode, ecCode);
    String responseFmt =
        """
        {
          "httpStatusCode":400,
          "httpStatusDescription":"Bad Request",
          "appErrorCode":"FDR-0704",
          "errors": [
            {
              "message":"Reporting flow [2016-08-16pspTest-1176] have sender.pspId [PSP_NOT_MATCH] but not match with query param [pspTest]"
            }
          ]
        }
        """;

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

    String url = flowsUrl.formatted(pspUnknown);
    String bodyFmt =
        flowTemplate.formatted(reportingFlowName, pspUnknown, brokerCode, channelCode, ecCode);
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
        flowTemplate.formatted(reportingFlowName, pspCodeNotEnabled, brokerCode, channelCode, ecCode);
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

    String url = flowsUrl.formatted(pspCode);
    String bodyFmt =
        flowTemplate.formatted(reportingFlowName, pspCode, brokerPspUnknown, channelCode, ecCode);
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
    String url = flowsUrl.formatted(pspCode);
    String bodyFmt =
        flowTemplate.formatted(reportingFlowName, pspCode, brokerCodeNotEnabled, channelCode, ecCode);
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

    String url = flowsUrl.formatted(pspCode);
    String bodyFmt =
        flowTemplate.formatted(reportingFlowName, pspCode, brokerCode, channelUnknown, ecCode);
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
    String url = flowsUrl.formatted(pspCode);
    String bodyFmt =
        flowTemplate.formatted(reportingFlowName, pspCode, brokerCode, channelCodeNotEnabled, ecCode);
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
    String url = flowsUrl.formatted(pspCode);
    String bodyFmt =
        flowTemplate.formatted(reportingFlowName, pspCode, brokerCode2, channelCode, ecCode);
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
    String url = flowsUrl.formatted(pspCode2);
    String bodyFmt =
        flowTemplate.formatted(reportingFlowName, pspCode2, brokerCode, channelCode, ecCode);
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

  @Test
  @DisplayName("PSPS create KO FDR-0716")
  public void test_ecId_KO_FDR0716() {
    String ecUnknown = "EC_UNKNOWN";
    String url = flowsUrl.formatted(pspCode);
    String bodyFmt =
        flowTemplate.formatted(reportingFlowName, pspCode, brokerCode, channelCode, ecUnknown);
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
    String url = flowsUrl.formatted(pspCode);
    String bodyFmt =
        flowTemplate.formatted(reportingFlowName, pspCode, brokerCode, channelCode, ecCodeNotEnabled);
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
    String url = flowsUrl.formatted(pspCode);
    String bodyFmt =
        flowTemplate.formatted(reportingFlowNameDateWrongFormat, pspCode, brokerCode, channelCode, ecCode);
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
    String url = flowsUrl.formatted(pspCode);
    String bodyFmt =
        flowTemplate.formatted(reportingFlowNamePspWrongFormat, pspCode, brokerCode, channelCode, ecCode);
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

}
