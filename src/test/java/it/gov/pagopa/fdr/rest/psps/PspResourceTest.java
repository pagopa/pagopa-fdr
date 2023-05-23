package it.gov.pagopa.fdr.rest.psps;

import static io.restassured.RestAssured.given;
import static it.gov.pagopa.fdr.Constants.brokerCode;
import static it.gov.pagopa.fdr.Constants.brokerCode2;
import static it.gov.pagopa.fdr.Constants.brokerCodeNotEnabled;
import static it.gov.pagopa.fdr.Constants.channelCode;
import static it.gov.pagopa.fdr.Constants.channelCodeNotEnabled;
import static it.gov.pagopa.fdr.Constants.ecCode;
import static it.gov.pagopa.fdr.Constants.ecCodeNotEnabled;
import static it.gov.pagopa.fdr.Constants.flowsDeleteUrl;
import static it.gov.pagopa.fdr.Constants.flowsUrl;
import static it.gov.pagopa.fdr.Constants.header;
import static it.gov.pagopa.fdr.Constants.paymentsAddUrl;
import static it.gov.pagopa.fdr.Constants.paymentsDeleteUrl;
import static it.gov.pagopa.fdr.Constants.pspCode;
import static it.gov.pagopa.fdr.Constants.pspCode2;
import static it.gov.pagopa.fdr.Constants.pspCodeNotEnabled;
import static it.gov.pagopa.fdr.Constants.reportingFlowName;
import static it.gov.pagopa.fdr.Constants.reportingFlowNameDateWrongFormat;
import static it.gov.pagopa.fdr.Constants.reportingFlowNamePspWrongFormat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.rest.BaseResource;
import it.gov.pagopa.fdr.rest.exceptionmapper.ErrorResponse;
import it.gov.pagopa.fdr.rest.model.GenericResponse;
import it.gov.pagopa.fdr.service.dto.SenderTypeEnumDto;
import it.gov.pagopa.fdr.util.MongoResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(MockServerTestResource.class)
@QuarkusTestResource(MongoResource.class)
class PspResourceTest extends BaseResource {

  @Test
  @DisplayName("PSPS - OK - inserimento completo e pubblicazione di un flusso")
  void test_psp_OK() {
    pspSunnyDay(getFlowName());
  }

  @Test
  @DisplayName("PSPS - OK - inserimento di un flusso per tipo ABI_CODE")
  void test_psp_ABICODE_createFlow_OK() {
    String flowName = getFlowName();
    String url = flowsUrl.formatted(pspCode);
    String bodyFmt = flowTemplate.formatted(flowName, SenderTypeEnumDto.ABI_CODE.name(), pspCode, brokerCode, channelCode, ecCode);
    String responseFmt = testUtil.prettyPrint(response.formatted(flowName), GenericResponse.class);

    String res = testUtil.prettyPrint(given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - OK - inserimento di un flusso per tipo BIC_CODE")
  void test_psp_BIC_CODE_createFlow_OK() {
    String flowName = getFlowName();
    String url = flowsUrl.formatted(pspCode);
    String bodyFmt = flowTemplate.formatted(flowName, SenderTypeEnumDto.BIC_CODE.name(), pspCode, brokerCode, channelCode, ecCode);
    String responseFmt = testUtil.prettyPrint(response.formatted(flowName), GenericResponse.class);

    String res = testUtil.prettyPrint(given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - OK - inserimento e cancellazione di un flusso")
  void test_psp_deleteFlow_OK() {
    String flowName = getFlowName();
    String url = flowsUrl.formatted(pspCode);
    String bodyFmt = flowTemplate.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(), pspCode, brokerCode, channelCode, ecCode);
    String responseFmt = testUtil.prettyPrint(response.formatted(flowName), GenericResponse.class);

    String res = testUtil.prettyPrint(given()
            .body(bodyFmt)
            .header(header)
            .when()
            .post(url)
            .then()
            .statusCode(201)
            .extract()
            .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    url = flowsDeleteUrl.formatted(pspCode, flowName);
    responseFmt = testUtil.prettyPrint(flowsDeletedResponse.formatted(flowName), GenericResponse.class);
    res = testUtil.prettyPrint(given()
            .body(bodyFmt)
            .header(header)
            .when()
            .delete(url)
            .then()
            .statusCode(200)
            .extract()
            .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    url = flowsDeleteUrl.formatted(pspCode, flowName);
    responseFmt = testUtil.prettyPrint("""
        {
          "httpStatusCode" : 404,
          "httpStatusDescription" : "Not Found",
          "appErrorCode" : "FDR-0701",
          "errors" : [ {
            "message" : "Reporting flow [%s] not found"
          } ]
        }
        """.formatted(flowName), ErrorResponse.class);
    res = testUtil.prettyPrint(given()
        .body(bodyFmt)
        .header(header)
        .when()
        .delete(url)
        .then()
        .statusCode(404)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - OK - inserimento completo e cancellazione dei payments")
  public void test_psp_deletePayments_OK() {
    String flowName = getFlowName();
    String url = flowsUrl.formatted(pspCode);
    String bodyFmt = flowTemplate.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(), pspCode, brokerCode, channelCode, ecCode);
    String responseFmt = testUtil.prettyPrint(response.formatted(flowName), GenericResponse.class);

    String res = testUtil.prettyPrint(given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    url = paymentsAddUrl.formatted(pspCode, flowName);
    responseFmt = testUtil.prettyPrint(paymentsAddResponse.formatted(flowName), GenericResponse.class);
    res = testUtil.prettyPrint(
            given()
                .body(paymentsAddTemplate)
                .header(header)
                .when()
                .put(url)
                .then()
                .statusCode(200)
                .extract()
                .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    url = paymentsDeleteUrl.formatted(pspCode, flowName);
    responseFmt = testUtil.prettyPrint(paymentsDeleteResponse.formatted(flowName), GenericResponse.class);
    res = testUtil.prettyPrint(given()
        .body(paymentsDeleteTemplate)
        .header(header)
        .when()
        .put(url)
        .then()
        .statusCode(200)
        .extract()
        .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    responseFmt = testUtil.prettyPrint("""
        {
           "httpStatusCode":400,
           "httpStatusDescription":"Bad Request",
           "appErrorCode":"FDR-0703",
           "errors":[
              {
                 "message":"Reporting flow [%s] exist but in [CREATED] status"
              }
           ]
        }
        """.formatted(flowName), ErrorResponse.class);
    res = testUtil.prettyPrint(given()
        .body(paymentsDeleteTemplate)
        .header(header)
        .when()
        .put(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0702 - flow already exists")
  void test_psp_KO_FDR0702() {
    String flowName = getFlowName();
    String url = flowsUrl.formatted(pspCode);
    String bodyFmt = flowTemplate.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(), pspCode, brokerCode, channelCode, ecCode);
    String responseFmt = testUtil.prettyPrint(response.formatted(flowName), GenericResponse.class);

    String res = testUtil.prettyPrint(given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    responseFmt =
        testUtil.prettyPrint("""
        {
          "httpStatusCode":400,
          "httpStatusDescription":"Bad Request",
          "appErrorCode":"FDR-0702",
          "errors": [
            {
              "message":"Reporting flow [%s] already exist in [CREATED] status"
            }
          ]
        }
        """.formatted(flowName), ErrorResponse.class);
    res = testUtil.prettyPrint(given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0703 - reporting flow wrong action")
  public void test_psp_KO_FDR0703() {
    String flowName = getFlowName();
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
                .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    url = paymentsDeleteUrl.formatted(pspCode, flowName);
    responseFmt = testUtil.prettyPrint("""
        {
          "httpStatusCode":400,
          "httpStatusDescription":"Bad Request",
          "appErrorCode":"FDR-0703",
          "errors": [
            {
              "message":"Reporting flow [%s] exist but in [CREATED] status"
            }
          ]
        }
        """.formatted(flowName), ErrorResponse.class);
    res = testUtil.prettyPrint(given()
        .body(paymentsDeleteTemplate)
        .header(header)
        .when()
        .put(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0704 - psp param and psp body not match")
  void test_psp_KO_FDR0704() {
    String pspNotMatch = "PSP_NOT_MATCH";
    String url = flowsUrl.formatted(pspCode);
    String bodyFmt = flowTemplate.formatted(reportingFlowName, SenderTypeEnumDto.LEGAL_PERSON.name(), pspNotMatch, brokerCode, channelCode, ecCode);
    String responseFmt = testUtil.prettyPrint("""
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
        """, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(testUtil.prettyPrint(res), equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0705 - payments with same index in same request")
  void test_psp_KO_FDR0705() {
    String flowName = getFlowName();
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
                .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    url = paymentsAddUrl.formatted(pspCode, flowName);
    bodyFmt = paymentsSameIndexAddTemplate;
    responseFmt = testUtil.prettyPrint("""
        {
          "httpStatusCode":400,
          "httpStatusDescription":"Bad Request",
          "appErrorCode":"FDR-0705",
          "errors":[
             {
                "message":"Exist one or more payment index in same request on reporting flow [%s]"
             }
          ]
        }""".formatted(flowName), ErrorResponse.class);
    res =
        testUtil.prettyPrint(
            given()
                .body(bodyFmt)
                .header(header)
                .when()
                .put(url)
                .then()
                .statusCode(400)
                .extract()
                .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0706 - payments with same index")
  void test_psp_KO_FDR0706() {
    String flowName = getFlowName();
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

    bodyFmt = payments2AddTemplate;
    responseFmt = testUtil.prettyPrint("""
        {
          "httpStatusCode":400,
          "httpStatusDescription":"Bad Request",
          "appErrorCode":"FDR-0706",
          "errors":[
             {
                "message":"One or more payment index already added on reporting flow [%s]"
             }
          ]
        }""".formatted(flowName), ErrorResponse.class);
    res =
        testUtil.prettyPrint(
            given()
                .body(bodyFmt)
                .header(header)
                .when()
                .put(url)
                .then()
                .statusCode(400)
                .extract()
                .body()
                .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0707 - payments unknown index delete")
  void test_psp_KO_FDR0707() {
    String flowName = getFlowName();
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

    url = paymentsDeleteUrl.formatted(pspCode, flowName);
    responseFmt = testUtil.prettyPrint("""
        {
          "httpStatusCode":400,
          "httpStatusDescription":"Bad Request",
          "appErrorCode":"FDR-0707",
          "errors":[
             {
                "message":"Index of payment not match with index loaded on reporting flow [%s]"
             }
          ]
        }""".formatted(flowName), ErrorResponse.class);
    res = testUtil.prettyPrint(given()
        .body(paymentsDeleteWrongTemplate)
        .header(header)
        .when()
        .put(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0708 - psp unknown")
  void test_psp_KO_FDR0708() {
    String pspUnknown = "PSP_UNKNOWN";
    String url = flowsUrl.formatted(pspUnknown);
    String bodyFmt = flowTemplate.formatted(reportingFlowName, SenderTypeEnumDto.LEGAL_PERSON.name(), pspUnknown, brokerCode, channelCode, ecCode);
    String responseFmt =
        """
        {
          "httpStatusCode":400,
          "httpStatusDescription":"Bad Request",
          "appErrorCode":"FDR-0708",
          "errors":[
             {
                "message":"Psp [PSP_UNKNOWN] unknown"
             }
          ]
        }""";
    String responseExpected = testUtil.prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(testUtil.prettyPrint(res), equalTo(responseExpected));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0709 - psp not enabled")
  void test_psp_KO_FDR0709() {
    String url = "/psps/%s/flows".formatted(pspCodeNotEnabled);
    String bodyFmt =
        flowTemplate.formatted(reportingFlowName, SenderTypeEnumDto.LEGAL_PERSON.name(), pspCodeNotEnabled, brokerCode, channelCode, ecCode);
    String responseFmt =
        """
        {
           "httpStatusCode":400,
           "httpStatusDescription":"Bad Request",
           "appErrorCode":"FDR-0709",
           "errors":[
              {
                 "message":"Psp [pspNotEnabled] not enabled"
              }
           ]
        }""";
    String responseExpected = testUtil.prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(testUtil.prettyPrint(res), equalTo(responseExpected));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0710 - brokerPsp unknown")
  void test_brokerpsp_KO_FDR0710() {
    String brokerPspUnknown = "BROKERPSP_UNKNOWN";
    String url = flowsUrl.formatted(pspCode);
    String bodyFmt =
        flowTemplate.formatted(reportingFlowName, SenderTypeEnumDto.LEGAL_PERSON.name(), pspCode, brokerPspUnknown, channelCode, ecCode);
    String responseFmt =
        """
        {
          "httpStatusCode":400,
          "httpStatusDescription":"Bad Request",
          "appErrorCode":"FDR-0710",
          "errors":[
             {
                "message":"Broker [BROKERPSP_UNKNOWN] unknown"
             }
          ]
        }""";
    String responseExpected = testUtil.prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(testUtil.prettyPrint(res), equalTo(responseExpected));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0711 - brokerPsp not enabled")
  void test_brokerpsp_KO_FDR0711() {
    String url = flowsUrl.formatted(pspCode);
    String bodyFmt =
        flowTemplate.formatted(reportingFlowName, SenderTypeEnumDto.LEGAL_PERSON.name(), pspCode, brokerCodeNotEnabled, channelCode, ecCode);
    String responseFmt =
        """
          {
             "httpStatusCode":400,
             "httpStatusDescription":"Bad Request",
             "appErrorCode":"FDR-0711",
             "errors":[
                {
                   "message":"Broker [intNotEnabled] not enabled"
                }
             ]
          }""";
    String responseExpected = testUtil.prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(testUtil.prettyPrint(res), equalTo(responseExpected));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0712 - channel unknown")
  void test_channel_KO_FDR0712() {
    String channelUnknown = "CHANNEL_UNKNOWN";

    String url = flowsUrl.formatted(pspCode);
    String bodyFmt =
        flowTemplate.formatted(reportingFlowName, SenderTypeEnumDto.LEGAL_PERSON.name(), pspCode, brokerCode, channelUnknown, ecCode);
    String responseFmt =
        """
        {
           "httpStatusCode":400,
           "httpStatusDescription":"Bad Request",
           "appErrorCode":"FDR-0712",
           "errors":[
              {
                 "message":"Channel [CHANNEL_UNKNOWN] unknown"
              }
           ]
        }""";
    String responseExpected = testUtil.prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(testUtil.prettyPrint(res), equalTo(responseExpected));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0713 - channel not enabled")
  void test_channel_KO_FDR0713() {
    String url = flowsUrl.formatted(pspCode);
    String bodyFmt =
        flowTemplate.formatted(reportingFlowName, SenderTypeEnumDto.LEGAL_PERSON.name(), pspCode, brokerCode, channelCodeNotEnabled, ecCode);
    String responseFmt =
        """
        {
           "httpStatusCode":400,
           "httpStatusDescription":"Bad Request",
           "appErrorCode":"FDR-0713",
           "errors":[
              {
                 "message":"channelId.notEnabled"
              }
           ]
        }""";
    String responseExpected = testUtil.prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(testUtil.prettyPrint(res), equalTo(responseExpected));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0714 - channel with brokerPsp not authorized")
  void test_channelBroker_KO_FDR0714() {
    String url = flowsUrl.formatted(pspCode);
    String bodyFmt =
        flowTemplate.formatted(reportingFlowName, SenderTypeEnumDto.LEGAL_PERSON.name(), pspCode, brokerCode2, channelCode, ecCode);
    String responseFmt =
        """
        {
           "httpStatusCode":400,
           "httpStatusDescription":"Bad Request",
           "appErrorCode":"FDR-0714",
           "errors":[
              {
                 "message":"Channel [canaleTest] with broker [intTest2] not authorized"
              }
           ]
        }""";
    String responseExpected = testUtil.prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(testUtil.prettyPrint(res), equalTo(responseExpected));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0715 - channel with psp not authorized")
  void test_channelPsp_KO_FDR0715() {
    String url = flowsUrl.formatted(pspCode2);
    String bodyFmt =
        flowTemplate.formatted(reportingFlowName, SenderTypeEnumDto.LEGAL_PERSON.name(), pspCode2, brokerCode, channelCode, ecCode);
    String responseFmt =
        """
        {
           "httpStatusCode":400,
           "httpStatusDescription":"Bad Request",
           "appErrorCode":"FDR-0715",
           "errors":[
              {
                 "message":"Channel [canaleTest] with psp [pspTest2] not authorized"
              }
           ]
        }""";
    String responseExpected = testUtil.prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(testUtil.prettyPrint(res), equalTo(responseExpected));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0716 - ec unknown")
  void test_ecId_KO_FDR0716() {
    String ecUnknown = "EC_UNKNOWN";
    String url = flowsUrl.formatted(pspCode);
    String bodyFmt =
        flowTemplate.formatted(reportingFlowName, SenderTypeEnumDto.LEGAL_PERSON.name(), pspCode, brokerCode, channelCode, ecUnknown);
    String responseFmt =
        """
        {
           "httpStatusCode":400,
           "httpStatusDescription":"Bad Request",
           "appErrorCode":"FDR-0716",
           "errors":[
              {
                 "message":"Creditor institution [EC_UNKNOWN] unknown"
              }
           ]
        }""";
    String responseExpected = testUtil.prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(testUtil.prettyPrint(res), equalTo(responseExpected));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0717 - ec not enabled")
  void test_ecId_KO_FDR0717() {
    String url = flowsUrl.formatted(pspCode);
    String bodyFmt =
        flowTemplate.formatted(reportingFlowName, SenderTypeEnumDto.LEGAL_PERSON.name(), pspCode, brokerCode, channelCode, ecCodeNotEnabled);
    String responseFmt =
        """
        {
           "httpStatusCode":400,
           "httpStatusDescription":"Bad Request",
           "appErrorCode":"FDR-0717",
           "errors":[
              {
                 "message":"Creditor institution [00987654321] not enabled"
              }
           ]
        }""";
    String responseExpected = testUtil.prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(testUtil.prettyPrint(res), equalTo(responseExpected));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0718 - flow format wrong date")
  void test_flowName_KO_FDR0718() {
    String url = flowsUrl.formatted(pspCode);
    String bodyFmt =
        flowTemplate.formatted(reportingFlowNameDateWrongFormat, SenderTypeEnumDto.LEGAL_PERSON.name(), pspCode, brokerCode, channelCode, ecCode);
    String responseFmt =
        """
        {
           "httpStatusCode":400,
           "httpStatusDescription":"Bad Request",
           "appErrorCode":"FDR-0718",
           "errors":[
              {
                 "message":"Reporting flow [2016-aa-16pspTest-1176] has wrong date"
              }
           ]
        }""";
    String responseExpected = testUtil.prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(testUtil.prettyPrint(res), equalTo(responseExpected));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0719 - flow format wrong psp")
  void test_flowName_KO_FDR0719() {
    String url = flowsUrl.formatted(pspCode);
    String bodyFmt =
        flowTemplate.formatted(reportingFlowNamePspWrongFormat, SenderTypeEnumDto.LEGAL_PERSON.name(), pspCode, brokerCode, channelCode, ecCode);
    String responseFmt =
        """
        {
           "httpStatusCode":400,
           "httpStatusDescription":"Bad Request",
           "appErrorCode":"FDR-0719",
           "errors":[
              {
                 "message":"Reporting flow [2016-08-16-psp-1176] has wrong psp"
              }
           ]
        }""";
    String responseExpected = testUtil.prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(header)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(testUtil.prettyPrint(res), equalTo(responseExpected));
  }

}
