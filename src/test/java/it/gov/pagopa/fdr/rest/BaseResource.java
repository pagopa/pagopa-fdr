package it.gov.pagopa.fdr.rest;

import static io.restassured.RestAssured.given;
import static it.gov.pagopa.fdr.Constants.brokerCode;
import static it.gov.pagopa.fdr.Constants.channelCode;
import static it.gov.pagopa.fdr.Constants.ecCode;
import static it.gov.pagopa.fdr.Constants.flowsPublishUrl;
import static it.gov.pagopa.fdr.Constants.flowsUrl;
import static it.gov.pagopa.fdr.Constants.header;
import static it.gov.pagopa.fdr.Constants.paymentsAddUrl;
import static it.gov.pagopa.fdr.Constants.pspCode;
import static it.gov.pagopa.fdr.Constants.reportingFlowName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import it.gov.pagopa.fdr.rest.model.GenericResponse;
import it.gov.pagopa.fdr.service.dto.SenderTypeEnumDto;
import it.gov.pagopa.fdr.util.TestUtil;
import jakarta.inject.Inject;
import java.util.Random;
import java.util.random.RandomGenerator;

public class BaseResource {

  @Inject protected TestUtil testUtil;

  protected static String flowTemplate =
      """
      {
        "reportingFlowName": "%s",
        "reportingFlowDate": "2023-04-05T09:21:37.810000Z",
        "sender": {
          "type": "%s",
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
            "payStatus": "REVOKED",
            "payDate": "2023-02-03T12:00:30.900000Z"
          },{
            "iuv": "c",
            "iur": "abcdefg",
            "index": 3,
            "pay": 0.01,
            "payStatus": "NO_RPT",
            "payDate": "2023-02-03T12:00:30.900000Z"
          }
        ]
      }
      """;

  protected static String paymentsSameIndexAddTemplate =
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
            "index": 1,
            "pay": 0.01,
            "payStatus": "REVOKED",
            "payDate": "2023-02-03T12:00:30.900000Z"
          }
        ]
      }
      """;

  protected static String payments2AddTemplate =
      """
      {
        "payments": [{
          "iuv": "a",
          "iur": "abcdefg",
          "index": 1,
          "pay": 0.01,
          "payStatus": "EXECUTED",
          "payDate": "2023-02-03T12:00:30.900000Z"
        }]
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

  protected static String paymentsDeleteWrongTemplate =
      """
      {
        "indexPayments": [
            5
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

  protected static String paymentsDeleteResponse =
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
    String bodyFmt =
        flowTemplate.formatted(
            flowName,
            SenderTypeEnumDto.LEGAL_PERSON.name(),
            pspCode,
            brokerCode,
            channelCode,
            ecCode);
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