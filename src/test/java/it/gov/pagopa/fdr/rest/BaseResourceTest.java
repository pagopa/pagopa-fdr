package it.gov.pagopa.fdr.rest;

import static io.restassured.RestAssured.given;
import static it.gov.pagopa.fdr.ConstantsTest.brokerCode;
import static it.gov.pagopa.fdr.ConstantsTest.channelCode;
import static it.gov.pagopa.fdr.ConstantsTest.ecCode;
import static it.gov.pagopa.fdr.ConstantsTest.flowsPublishUrl;
import static it.gov.pagopa.fdr.ConstantsTest.flowsUrl;
import static it.gov.pagopa.fdr.ConstantsTest.header;
import static it.gov.pagopa.fdr.ConstantsTest.paymentsAddUrl;
import static it.gov.pagopa.fdr.ConstantsTest.pspCode;
import static it.gov.pagopa.fdr.ConstantsTest.reportingFlowName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import it.gov.pagopa.fdr.rest.model.GenericResponse;
import it.gov.pagopa.fdr.util.TestUtil;
import jakarta.inject.Inject;
import java.util.Random;
import java.util.random.RandomGenerator;

public class BaseResourceTest {

  @Inject protected TestUtil testUtil;

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

  protected static String paymentsAddTemplate =
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

  protected static String paymentsDeleteTemplate =
      """
      {
        "indexPayments": [
            1,
            2,
            3
        ]
      }
      """;

  protected static String response =
      """
      {
        "message":"Flow [%s] saved"
      }
      """;

  protected static String flowsDeletedResponse =
      """
      {
        "message": "Flow [%s] deleted"
      }
      """;

  private static String flowsPublishedResponse =
      """
      {
        "message":"Flow [%s] published"
      }
      """;

  protected static String paymentsAddResponse =
      """
      {
        "message":"Flow [%s] payment added"
      }
      """;

  private static String paymentsDelResponse =
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
    String responseFmt = testUtil.prettyPrint(response.formatted(flowName), GenericResponse.class);

    String res =
        testUtil.prettyPrint(
            given()
                .body(bodyFmt)
                .header(header)
                .when()
                .post(url)
                .then()
                .statusCode(201)
                .extract()
                .body()
                .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    url = paymentsAddUrl.formatted(pspCode, flowName);
    bodyFmt = paymentsAddTemplate;
    responseFmt =
        testUtil.prettyPrint(paymentsAddResponse.formatted(flowName), GenericResponse.class);
    res =
        testUtil.prettyPrint(
            given()
                .body(bodyFmt)
                .header(header)
                .when()
                .put(url)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    url = flowsPublishUrl.formatted(pspCode, flowName);
    responseFmt =
        testUtil.prettyPrint(flowsPublishedResponse.formatted(flowName), GenericResponse.class);
    res =
        testUtil.prettyPrint(
            given()
                .header(header)
                .when()
                .post(url)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));
  }
}
