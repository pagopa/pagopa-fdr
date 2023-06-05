package it.gov.pagopa.fdr.rest;

import static io.restassured.RestAssured.given;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.BROKER_CODE;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.CHANNEL_CODE;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.EC_CODE;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.FLOWS_PUBLISH_URL;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.FLOWS_URL;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.HEADER;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.PAYMENTS_ADD_URL;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.PSP_CODE;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.REPORTING_FLOW_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import it.gov.pagopa.fdr.rest.model.GenericResponse;
import it.gov.pagopa.fdr.service.dto.SenderTypeEnumDto;
import jakarta.inject.Inject;
import java.util.Random;
import java.util.random.RandomGenerator;
import lombok.SneakyThrows;

public class BaseUnitTestHelper {

  ObjectMapper mapper = new ObjectMapper()
      .findAndRegisterModules()
      .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

  @SneakyThrows
  public <T> String prettyPrint(String json, Class<T> clazz) {
    T obj = mapper.readValue(json, clazz);
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj).replaceAll("\\r", "");
  }

  @SneakyThrows
  public <T> String prettyPrint(T obj) {
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj).replaceAll("\\r", "");
  }

  protected static String MALFORMED_JSON = """
      {
        12345
      }
      """;

  protected static String FLOW_TEMPLATE =
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

  protected static String FLOW_TEMPLATE_WRONG_INSTANT =
      """
      {
        "reportingFlowName": "%s",
        "reportingFlowDate": "%s",
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

  protected static String FLOW_TEMPLATE_WRONG_FIELDS =
      """
      {
        "flowName": "%s",
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

  protected static String PAYMENTS_ADD_TEMPLATE =
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

  protected static String PAYMENTS_ADD_INVALID_FIELD_VALUE_FORMAT_TEMPLATE =
      """
      {
        "payments": [{
            "iuv": "a",
            "iur": "abcdefg",
            "index": 1,
            "pay": "%s",
            "payStatus": "EXECUTED",
            "payDate": "2023-02-03T12:00:30.900000Z"
          }
        ]
      }
      """;

  protected static String PAYMENTS_ADD_INVALID_FORMAT_TEMPLATE =
      """
      {
        "payments": {
            "iuv": "a",
            "iur": "abcdefg",
            "index": 1,
            "pay": "%s",
            "payStatus": "EXECUTED",
            "payDate": "2023-02-03T12:00:30.900000Z"
          }
      }
      """;

  protected static String PAYMENTS_SAME_INDEX_ADD_TEMPLATE =
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

  protected static String PAYMENTS_2_ADD_TEMPLATE =
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

  protected static String PAYMENTS_DELETE_TEMPLATE =
      """
      {
        "indexPayments": [
            1,
            2,
            3
        ]
      }
      """;

  protected static String PAYMENTS_DELETE_PARTIAL_TEMPLATE =
      """
      {
        "indexPayments": [
            1,
            2
        ]
      }
      """;

  protected static String PAYMENTS_DELETE_WRONG_TEMPLATE =
      """
      {
        "indexPayments": [
            5
        ]
      }
      """;

  protected static String RESPONSE =
      """
      {
        "message":"Flow [%s] saved"
      }
      """;

  protected static String FLOWS_DELETED_RESPONSE =
      """
      {
        "message": "Flow [%s] deleted"
      }
      """;

  private static String FLOWS_PUBLISHED_RESPONSE =
      """
      {
        "message":"Flow [%s] published"
      }
      """;

  protected static String PAYMENTS_ADD_RESPONSE =
      """
      {
        "message":"Flow [%s] payment added"
      }
      """;

  protected static String PAYMENTS_DELETE_RESPONSE =
      """
      {
        "message":"Flow [%s] payment deleted"
      }
      """;

  protected String getFlowName() {
    RandomGenerator randomGenerator = new Random();
    return REPORTING_FLOW_NAME.substring(0, REPORTING_FLOW_NAME.length() - 4)
        + randomGenerator.nextInt(1111, 9999);
  }

  protected boolean pspSunnyDay(String flowName) {
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt =
        FLOW_TEMPLATE.formatted(
            flowName,
            SenderTypeEnumDto.LEGAL_PERSON.name(),
            PSP_CODE,
            BROKER_CODE,
            CHANNEL_CODE,
            EC_CODE);
    String responseFmt = prettyPrint(RESPONSE.formatted(flowName), GenericResponse.class);

    String res =
        prettyPrint(
            given()
                .body(bodyFmt)
                .header(HEADER)
                .when()
                .post(url)
                .then()
                .statusCode(201)
                .extract()
                .body()
                .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    url = PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);
    bodyFmt = PAYMENTS_ADD_TEMPLATE;
    responseFmt = prettyPrint(PAYMENTS_ADD_RESPONSE.formatted(flowName), GenericResponse.class);
    res =
        prettyPrint(
            given()
                .body(bodyFmt)
                .header(HEADER)
                .when()
                .put(url)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    url = FLOWS_PUBLISH_URL.formatted(PSP_CODE, flowName);
    responseFmt = prettyPrint(FLOWS_PUBLISHED_RESPONSE.formatted(flowName), GenericResponse.class);
    res =
        prettyPrint(
            given()
                .header(HEADER)
                .when()
                .post(url)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));
    return Boolean.TRUE;
  }
}
