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

import it.gov.pagopa.fdr.rest.model.GenericResponse;
import it.gov.pagopa.fdr.service.dto.SenderTypeEnumDto;
import java.util.Random;
import java.util.random.RandomGenerator;

public class TestUtil {
  public static String getDynamicFlowName() {
    return getDynamicFlowName(PSP_CODE);
  }

  public static String getDynamicFlowName(String psp) {
    RandomGenerator randomGenerator = new Random();
    return String.format("2016-08-16%s-%s", psp, randomGenerator.nextInt(1111, 9999));
  }

  public static String FLOW_TEMPLATE =
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

  public static String PAYMENTS_ADD_TEMPLATE =
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

  public static void pspSunnyDay(String flowName) {
    String urlPspFlow = FLOWS_URL.formatted(PSP_CODE, flowName);
    String bodyFmtPspFlow =
        FLOW_TEMPLATE.formatted(
            flowName,
            SenderTypeEnumDto.LEGAL_PERSON.name(),
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
    assertThat(resPspFlow.getMessage(), equalTo(String.format("Flow [%s] saved", flowName)));

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
    assertThat(
        resPayment.getMessage(), equalTo(String.format("Flow [%s] payment added", flowName)));

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
    assertThat(resPublish.getMessage(), equalTo(String.format("Flow [%s] published", flowName)));
  }
}
