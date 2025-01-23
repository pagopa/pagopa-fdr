package it.gov.pagopa.fdr.test.util;

import static io.restassured.RestAssured.given;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.BROKER_CODE;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.CHANNEL_CODE;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.EC_CODE;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.FLOWS_PUBLISH_URL;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.FLOWS_URL;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.HEADER;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.PAYMENTS_ADD_URL;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.PSP_CODE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import it.gov.pagopa.fdr.controller.model.common.response.GenericResponse;
import it.gov.pagopa.fdr.controller.model.flow.enums.SenderTypeEnum;
import java.time.Instant;

public class TestUtil {

  public static String getDynamicFlowName() {
    return getDynamicFlowName(PSP_CODE);
  }

  public static String getDynamicFlowName(String psp) {
    return String.format("2016-08-16%s-%s", psp, Instant.now().toEpochMilli());
  }

  public static String FLOW_TEMPLATE =
      """
          {
            "fdr": "%s",
            "fdrDate": "2023-04-05T09:21:37.810000Z",
            "sender": {
              "type": "%s",
              "id": "SELBIT2B",
              "pspId": "%s",
              "pspName": "Bank",
              "pspBrokerId": "%s",
              "channelId": "%s",
              "password": "1234567890"
            },
            "receiver": {
              "id": "APPBIT2B",
              "organizationId": "%s",
              "organizationName": "Comune di xyz"
            },
            "regulation": "SEPA - Bonifico xzy",
            "regulationDate": "2023-04-03T12:00:30.900000Z",
            "bicCodePouringBank": "UNCRITMMXXX",
            "totPayments": 5,
            "sumPayments": 0.05
          }
          """;

  public static String PAYMENTS_ADD_TEMPLATE =
      """
          {
            "payments": [{
                "index": 100,
                "iuv": "a",
                "iur": "abcdefg",
                "idTransfer": 1,
                "pay": 0.01,
                "payStatus": "EXECUTED",
                "payDate": "2023-02-03T12:00:30.900000Z"
              },{
                "index": 101,
                "iuv": "b",
                "iur": "abcdefg",
                "idTransfer": 2,
                "pay": 0.01,
                "payStatus": "REVOKED",
                "payDate": "2023-02-03T12:00:30.900000Z"
              },{
                "index": 102,
                "iuv": "c",
                "iur": "abcdefg",
                "idTransfer": 3,
                "pay": 0.01,
                "payStatus": "NO_RPT",
                "payDate": "2023-02-03T12:00:30.900000Z"
              },{
                "index": 103,
                "iuv": "d",
                "iur": "abcdefg",
                "idTransfer": 4,
                "pay": 0.01,
                "payStatus": "STAND_IN",
                "payDate": "2023-02-03T12:00:30.900000Z"
              },{
                "index": 104,
                "iuv": "e",
                "iur": "abcdefg",
                "idTransfer": 5,
                "pay": 0.01,
                "payStatus": "STAND_IN_NO_RPT",
                "payDate": "2023-02-03T12:00:30.900000Z"
              }
            ]
          }
          """;

  public static String PAYMENTS_ADD_TEMPLATE_2 =
      """
          {
            "payments": [{
                "index": 105,
                "iuv": "f",
                "iur": "abcdefg",
                "idTransfer": 5,
                "pay": 0.01,
                "payStatus": "EXECUTED",
                "payDate": "2023-02-03T12:00:30.900000Z"
              },{
                "index": 106,
                "iuv": "g",
                "iur": "abcdefg",
                "idTransfer": 5,
                "pay": 0.01,
                "payStatus": "REVOKED",
                "payDate": "2023-02-03T12:00:30.900000Z"
              },{
                "index": 107,
                "iuv": "h",
                "iur": "abcdefg",
                "idTransfer": 5,
                "pay": 0.01,
                "payStatus": "NO_RPT",
                "payDate": "2023-02-03T12:00:30.900000Z"
              },{
                "index": 108,
                "iuv": "i",
                "iur": "abcdefg",
                "idTransfer": 5,
                "pay": 0.01,
                "payStatus": "STAND_IN",
                "payDate": "2023-02-03T12:00:30.900000Z"
              },{
                "index": 109,
                "iuv": "l",
                "iur": "abcdefg",
                "idTransfer": 5,
                "pay": 0.01,
                "payStatus": "STAND_IN_NO_RPT",
                "payDate": "2023-02-03T12:00:30.900000Z"
              }
            ]
          }
          """;

  public static void pspSunnyDay(String flowName) {
    String urlPspFlow = FLOWS_URL.formatted(PSP_CODE, flowName);
    String bodyFmtPspFlow =
        FLOW_TEMPLATE.formatted(
            flowName,
            SenderTypeEnum.LEGAL_PERSON.name(),
            PSP_CODE,
            BROKER_CODE,
            CHANNEL_CODE,
            EC_CODE);

    GenericResponse resPspFlow =
        given()
            .body(bodyFmtPspFlow)
            .header(HEADER)
            .when()
            .post(urlPspFlow)
            .then()
            .statusCode(201)
            .extract()
            .body()
            .as(GenericResponse.class);
    assertThat(resPspFlow.getMessage(), equalTo(String.format("Fdr [%s] saved", flowName)));

    String urlPayment = PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);
    String bodyPayment = PAYMENTS_ADD_TEMPLATE;
    GenericResponse resPayment =
        given()
            .body(bodyPayment)
            .header(HEADER)
            .when()
            .put(urlPayment)
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(GenericResponse.class);
    assertThat(resPayment.getMessage(), equalTo(String.format("Fdr [%s] payment added", flowName)));

    String urlPublish = FLOWS_PUBLISH_URL.formatted(PSP_CODE, flowName);
    GenericResponse resPublish =
        given()
            .header(HEADER)
            .when()
            .post(urlPublish)
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(GenericResponse.class);
    assertThat(resPublish.getMessage(), equalTo(String.format("Fdr [%s] published", flowName)));
  }
}
