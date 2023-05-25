package it.gov.pagopa.fdr.rest.psps;

import static io.restassured.RestAssured.given;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.BROKER_CODE;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.BROKER_CODE_2;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.BROKER_CODE_NOT_ENABLED;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.CHANNEL_CODE;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.CHANNEL_CODE_NOT_ENABLED;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.EC_CODE;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.EC_CODE_NOT_ENABLED;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.FLOWS_DELETE_URL;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.FLOWS_PUBLISH_URL;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.FLOWS_URL;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.HEADER;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.PAYMENTS_ADD_URL;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.PAYMENTS_DELETE_URL;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.PSP_CODE;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.PSP_CODE_2;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.PSP_CODE_NOT_ENABLED;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.REPORTING_FLOW_NAME;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.REPORTING_FLOW_NAME_DATE_WRONG_FORMAT;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.REPORTING_FLOW_NAME_PSP_WRONG_FORMAT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.rest.BaseUnitTestHelper;
import it.gov.pagopa.fdr.rest.exceptionmapper.ErrorResponse;
import it.gov.pagopa.fdr.rest.model.GenericResponse;
import it.gov.pagopa.fdr.rest.model.SenderTypeEnum;
import it.gov.pagopa.fdr.service.dto.SenderTypeEnumDto;
import it.gov.pagopa.fdr.util.MongoResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(MockServerTestResource.class)
@QuarkusTestResource(MongoResource.class)
class PspResourceTest extends BaseUnitTestHelper {

  @Test
  @DisplayName("PSPS - OK - inserimento completo e pubblicazione di un flusso")
  void test_psp_OK() {
    assertThat(pspSunnyDay(getFlowName()), equalTo(Boolean.TRUE));
  }

  @Test
  @DisplayName("PSPS - OK - inserimento di un flusso per tipo ABI_CODE")
  void test_psp_ABICODE_createFlow_OK() {
    String flowName = getFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.ABI_CODE.name(), PSP_CODE,
        BROKER_CODE, CHANNEL_CODE, EC_CODE);
    String responseFmt = prettyPrint(RESPONSE.formatted(flowName), GenericResponse.class);

    String res = prettyPrint(given()
        .body(bodyFmt)
        .header(HEADER)
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
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.BIC_CODE.name(), PSP_CODE,
        BROKER_CODE, CHANNEL_CODE, EC_CODE);
    String responseFmt = prettyPrint(RESPONSE.formatted(flowName), GenericResponse.class);

    String res = prettyPrint(given()
        .body(bodyFmt)
        .header(HEADER)
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
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);
    String responseFmt = prettyPrint(RESPONSE.formatted(flowName), GenericResponse.class);

    String res = prettyPrint(given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .post(url)
            .then()
            .statusCode(201)
            .extract()
            .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    url = FLOWS_DELETE_URL.formatted(PSP_CODE, flowName);
    responseFmt = prettyPrint(FLOWS_DELETED_RESPONSE.formatted(flowName), GenericResponse.class);
    res = prettyPrint(given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .delete(url)
            .then()
            .statusCode(200)
            .extract()
            .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    url = FLOWS_DELETE_URL.formatted(PSP_CODE, flowName);
    responseFmt = prettyPrint("""
        {
          "httpStatusCode" : 404,
          "httpStatusDescription" : "Not Found",
          "appErrorCode" : "FDR-0701",
          "errors" : [ {
            "message" : "Reporting flow [%s] not found"
          } ]
        }
        """.formatted(flowName), ErrorResponse.class);
    res = prettyPrint(given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .delete(url)
        .then()
        .statusCode(404)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - OK - inserimento completo e cancellazione del flusso con payments")
  void test_psp_deleteFlowWithPayment_OK() {
    String flowName = getFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);
    String responseFmt = prettyPrint(RESPONSE.formatted(flowName), GenericResponse.class);

    String res = prettyPrint(given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    url = PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);
    responseFmt = prettyPrint(PAYMENTS_ADD_RESPONSE.formatted(flowName), GenericResponse.class);
    res = prettyPrint(
        given()
            .body(PAYMENTS_ADD_TEMPLATE)
            .header(HEADER)
            .when()
            .put(url)
            .then()
            .statusCode(200)
            .extract()
            .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    url = FLOWS_DELETE_URL.formatted(PSP_CODE, flowName);
    responseFmt = prettyPrint(FLOWS_DELETED_RESPONSE.formatted(flowName), GenericResponse.class);
    res = prettyPrint(given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .delete(url)
        .then()
        .statusCode(200)
        .extract()
        .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - OK - inserimento completo e cancellazione dei payments")
  void test_psp_deletePayments_OK() {
    String flowName = getFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);
    String responseFmt = prettyPrint(RESPONSE.formatted(flowName), GenericResponse.class);

    String res = prettyPrint(given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    url = PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);
    responseFmt = prettyPrint(PAYMENTS_ADD_RESPONSE.formatted(flowName), GenericResponse.class);
    res = prettyPrint(
            given()
                .body(PAYMENTS_ADD_TEMPLATE)
                .header(HEADER)
                .when()
                .put(url)
                .then()
                .statusCode(200)
                .extract()
                .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    url = PAYMENTS_DELETE_URL.formatted(PSP_CODE, flowName);
    responseFmt = prettyPrint(PAYMENTS_DELETE_RESPONSE.formatted(flowName), GenericResponse.class);
    res = prettyPrint(given()
        .body(PAYMENTS_DELETE_TEMPLATE)
        .header(HEADER)
        .when()
        .put(url)
        .then()
        .statusCode(200)
        .extract()
        .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    responseFmt = prettyPrint("""
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
    res = prettyPrint(given()
        .body(PAYMENTS_DELETE_TEMPLATE)
        .header(HEADER)
        .when()
        .put(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - OK - inserimento completo e cancellazione parziale dei payments")
  void test_psp_deletePayments_partial_OK() {
    String flowName = getFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);
    String responseFmt = prettyPrint(RESPONSE.formatted(flowName), GenericResponse.class);

    String res = prettyPrint(given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    url = PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);
    responseFmt = prettyPrint(PAYMENTS_ADD_RESPONSE.formatted(flowName), GenericResponse.class);
    res = prettyPrint(
        given()
            .body(PAYMENTS_ADD_TEMPLATE)
            .header(HEADER)
            .when()
            .put(url)
            .then()
            .statusCode(200)
            .extract()
            .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    url = PAYMENTS_DELETE_URL.formatted(PSP_CODE, flowName);
    responseFmt = prettyPrint(PAYMENTS_DELETE_RESPONSE.formatted(flowName), GenericResponse.class);
    res = prettyPrint(given()
        .body(PAYMENTS_DELETE_PARTIAL_TEMPLATE)
        .header(HEADER)
        .when()
        .put(url)
        .then()
        .statusCode(200)
        .extract()
        .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0701 - flow not found in publish")
  void test_psp_publish_KO_FDR0701() {
    String flowName = getFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);
    String responseFmt = prettyPrint(RESPONSE.formatted(flowName), GenericResponse.class);

    String res = prettyPrint(given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    url = PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);
    bodyFmt = PAYMENTS_ADD_TEMPLATE;
    responseFmt =
        prettyPrint(PAYMENTS_ADD_RESPONSE.formatted(flowName), GenericResponse.class);
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

    flowName = getFlowName();
    url = FLOWS_PUBLISH_URL.formatted(PSP_CODE, flowName);
    responseFmt =
        prettyPrint("""
        {
          "httpStatusCode":404,
          "httpStatusDescription":"Not Found",
          "appErrorCode":"FDR-0701",
          "errors": [
            {
              "message":"Reporting flow [%s] not found"
            }
          ]
        }
        """.formatted(flowName), ErrorResponse.class);
    res = prettyPrint(given()
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(404)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0701 - flow not found in add payments new flowName")
  void test_psp_payments_add_KO_FDR0701() {
    String flowName = getFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);
    String responseFmt = prettyPrint(RESPONSE.formatted(flowName), GenericResponse.class);

    String res = prettyPrint(given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    flowName = getFlowName();
    url = PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);
    responseFmt =
        prettyPrint("""
        {
          "httpStatusCode":404,
          "httpStatusDescription":"Not Found",
          "appErrorCode":"FDR-0701",
          "errors": [
            {
              "message":"Reporting flow [%s] not found"
            }
          ]
        }
        """.formatted(flowName), ErrorResponse.class);
    res = prettyPrint(
        given()
            .body(PAYMENTS_ADD_TEMPLATE)
            .header(HEADER)
            .when()
            .put(url)
            .then()
            .statusCode(404)
            .extract()
            .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0701 - flow not found in delete payments new flowName")
  void test_psp_payments_delete_KO_FDR0701() {
    String flowName = getFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);
    String responseFmt = prettyPrint(RESPONSE.formatted(flowName), GenericResponse.class);

    String res = prettyPrint(given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    flowName = getFlowName();
    url = PAYMENTS_DELETE_URL.formatted(PSP_CODE, flowName);
    responseFmt =
        prettyPrint("""
        {
          "httpStatusCode":404,
          "httpStatusDescription":"Not Found",
          "appErrorCode":"FDR-0701",
          "errors": [
            {
              "message":"Reporting flow [%s] not found"
            }
          ]
        }
        """.formatted(flowName), ErrorResponse.class);
    res = prettyPrint(given()
        .body(PAYMENTS_DELETE_TEMPLATE)
        .header(HEADER)
        .when()
        .put(url)
        .then()
        .statusCode(404)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0702 - flow already exists")
  void test_psp_KO_FDR0702() {
    String flowName = getFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);
    String responseFmt = prettyPrint(RESPONSE.formatted(flowName), GenericResponse.class);

    String res = prettyPrint(given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    responseFmt =
        prettyPrint("""
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
    res = prettyPrint(given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0703 - flow not found in publish flow CREATED")
  void test_psp_CREATED_publish_KO_FDR0703() {
    String flowName = getFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);
    String responseFmt = prettyPrint(RESPONSE.formatted(flowName), GenericResponse.class);

    String res = prettyPrint(given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    url = FLOWS_PUBLISH_URL.formatted(PSP_CODE, flowName);
    responseFmt =
        prettyPrint("""
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
    res = prettyPrint(given()
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0703 - reporting flow wrong action delete payments")
  void test_psp_payments_delete_KO_FDR0703() {
    String flowName = getFlowName();
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
                .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    url = PAYMENTS_DELETE_URL.formatted(PSP_CODE, flowName);
    responseFmt = prettyPrint("""
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
    res = prettyPrint(given()
        .body(PAYMENTS_DELETE_TEMPLATE)
        .header(HEADER)
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
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt = FLOW_TEMPLATE.formatted(REPORTING_FLOW_NAME, SenderTypeEnumDto.LEGAL_PERSON.name(), pspNotMatch,
        BROKER_CODE, CHANNEL_CODE, EC_CODE);
    String responseFmt = prettyPrint("""
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
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(prettyPrint(res), equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0705 - add payments with same index in same request")
  void test_psp_payments_add_KO_FDR0705() {
    String flowName = getFlowName();
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
                .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    url = PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);
    bodyFmt = PAYMENTS_SAME_INDEX_ADD_TEMPLATE;
    responseFmt = prettyPrint("""
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
        prettyPrint(
            given()
                .body(bodyFmt)
                .header(HEADER)
                .when()
                .put(url)
                .then()
                .statusCode(400)
                .extract()
                .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0705 - delete payments with same index in same request")
  void test_psp_payments_delete_KO_FDR0705() {
    String flowName = getFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);
    String responseFmt = prettyPrint(RESPONSE.formatted(flowName), GenericResponse.class);

    String res = prettyPrint(given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));

    url = PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);
    bodyFmt = PAYMENTS_ADD_TEMPLATE;
    responseFmt =
        prettyPrint(PAYMENTS_ADD_RESPONSE.formatted(flowName), GenericResponse.class);
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

    url = PAYMENTS_DELETE_URL.formatted(PSP_CODE, flowName);
    bodyFmt = """
        {
          "indexPayments": [
              1,
              1
          ]
        }
        """;
    responseFmt =
        prettyPrint("""
        {
          "httpStatusCode":400,
          "httpStatusDescription":"Bad Request",
          "appErrorCode":"FDR-0705",
          "errors": [
            {
              "message":"Exist one or more payment index in same request on reporting flow [%s]"
            }
          ]
        }
        """.formatted(flowName), ErrorResponse.class);
    res = prettyPrint(given()
        .body(bodyFmt)
        .header(HEADER)
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
    responseFmt =
        prettyPrint(PAYMENTS_ADD_RESPONSE.formatted(flowName), GenericResponse.class);
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

    bodyFmt = PAYMENTS_2_ADD_TEMPLATE;
    responseFmt = prettyPrint("""
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
        prettyPrint(
            given()
                .body(bodyFmt)
                .header(HEADER)
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
    responseFmt =
        prettyPrint(PAYMENTS_ADD_RESPONSE.formatted(flowName), GenericResponse.class);
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

    url = PAYMENTS_DELETE_URL.formatted(PSP_CODE, flowName);
    responseFmt = prettyPrint("""
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
    res = prettyPrint(given()
        .body(PAYMENTS_DELETE_WRONG_TEMPLATE)
        .header(HEADER)
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
    String url = FLOWS_URL.formatted(pspUnknown);
    String bodyFmt = FLOW_TEMPLATE.formatted(REPORTING_FLOW_NAME, SenderTypeEnumDto.LEGAL_PERSON.name(), pspUnknown,
        BROKER_CODE, CHANNEL_CODE, EC_CODE);
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
    String responseExpected = prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(prettyPrint(res), equalTo(responseExpected));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0709 - psp not enabled")
  void test_psp_KO_FDR0709() {
    String url = "/psps/%s/flows".formatted(PSP_CODE_NOT_ENABLED);
    String bodyFmt =
        FLOW_TEMPLATE.formatted(REPORTING_FLOW_NAME, SenderTypeEnumDto.LEGAL_PERSON.name(),
            PSP_CODE_NOT_ENABLED, BROKER_CODE, CHANNEL_CODE, EC_CODE);
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
    String responseExpected = prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(prettyPrint(res), equalTo(responseExpected));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0710 - brokerPsp unknown")
  void test_brokerpsp_KO_FDR0710() {
    String brokerPspUnknown = "BROKERPSP_UNKNOWN";
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt =
        FLOW_TEMPLATE.formatted(REPORTING_FLOW_NAME, SenderTypeEnumDto.LEGAL_PERSON.name(), PSP_CODE, brokerPspUnknown,
            CHANNEL_CODE, EC_CODE);
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
    String responseExpected = prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(prettyPrint(res), equalTo(responseExpected));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0711 - brokerPsp not enabled")
  void test_brokerpsp_KO_FDR0711() {
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt =
        FLOW_TEMPLATE.formatted(REPORTING_FLOW_NAME, SenderTypeEnumDto.LEGAL_PERSON.name(), PSP_CODE,
            BROKER_CODE_NOT_ENABLED, CHANNEL_CODE, EC_CODE);
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
    String responseExpected = prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(prettyPrint(res), equalTo(responseExpected));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0712 - channel unknown")
  void test_channel_KO_FDR0712() {
    String channelUnknown = "CHANNEL_UNKNOWN";

    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt =
        FLOW_TEMPLATE.formatted(REPORTING_FLOW_NAME, SenderTypeEnumDto.LEGAL_PERSON.name(), PSP_CODE,
            BROKER_CODE, channelUnknown, EC_CODE);
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
    String responseExpected = prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(prettyPrint(res), equalTo(responseExpected));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0713 - channel not enabled")
  void test_channel_KO_FDR0713() {
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt =
        FLOW_TEMPLATE.formatted(REPORTING_FLOW_NAME, SenderTypeEnumDto.LEGAL_PERSON.name(), PSP_CODE,
            BROKER_CODE, CHANNEL_CODE_NOT_ENABLED, EC_CODE);
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
    String responseExpected = prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(prettyPrint(res), equalTo(responseExpected));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0714 - channel with brokerPsp not authorized")
  void test_channelBroker_KO_FDR0714() {
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt =
        FLOW_TEMPLATE.formatted(REPORTING_FLOW_NAME, SenderTypeEnumDto.LEGAL_PERSON.name(), PSP_CODE,
            BROKER_CODE_2, CHANNEL_CODE, EC_CODE);
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
    String responseExpected = prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(prettyPrint(res), equalTo(responseExpected));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0715 - channel with psp not authorized")
  void test_channelPsp_KO_FDR0715() {
    String url = FLOWS_URL.formatted(PSP_CODE_2);
    String bodyFmt =
        FLOW_TEMPLATE.formatted(REPORTING_FLOW_NAME, SenderTypeEnumDto.LEGAL_PERSON.name(),
            PSP_CODE_2, BROKER_CODE, CHANNEL_CODE, EC_CODE);
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
    String responseExpected = prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(prettyPrint(res), equalTo(responseExpected));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0716 - ec unknown")
  void test_ecId_KO_FDR0716() {
    String ecUnknown = "EC_UNKNOWN";
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt =
        FLOW_TEMPLATE.formatted(REPORTING_FLOW_NAME, SenderTypeEnumDto.LEGAL_PERSON.name(), PSP_CODE,
            BROKER_CODE, CHANNEL_CODE, ecUnknown);
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
    String responseExpected = prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(prettyPrint(res), equalTo(responseExpected));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0717 - ec not enabled")
  void test_ecId_KO_FDR0717() {
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt =
        FLOW_TEMPLATE.formatted(REPORTING_FLOW_NAME, SenderTypeEnumDto.LEGAL_PERSON.name(), PSP_CODE,
            BROKER_CODE, CHANNEL_CODE, EC_CODE_NOT_ENABLED);
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
    String responseExpected = prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(prettyPrint(res), equalTo(responseExpected));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0718 - flow format wrong date")
  void test_flowName_KO_FDR0718() {
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt =
        FLOW_TEMPLATE.formatted(REPORTING_FLOW_NAME_DATE_WRONG_FORMAT, SenderTypeEnumDto.LEGAL_PERSON.name(),
            PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);
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
    String responseExpected = prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(prettyPrint(res), equalTo(responseExpected));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0719 - flow format wrong psp")
  void test_flowName_KO_FDR0719() {
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt =
        FLOW_TEMPLATE.formatted(REPORTING_FLOW_NAME_PSP_WRONG_FORMAT, SenderTypeEnumDto.LEGAL_PERSON.name(),
            PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);
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
    String responseExpected = prettyPrint(responseFmt, ErrorResponse.class);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(prettyPrint(res), equalTo(responseExpected));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0400 - JSON input wrong fields")
  void test_psp_KO_FDR0400() {
    String flowName = getFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt = FLOW_TEMPLATE_WRONG_FIELDS.formatted(
        flowName,
        SenderTypeEnum.LEGAL_PERSON.name(),
        PSP_CODE,
        BROKER_CODE,
        CHANNEL_CODE,
        EC_CODE);

    String responseFmt =
        prettyPrint("""
        {
          "httpStatusCode":400,
          "httpStatusDescription":"Bad Request",
          "appErrorCode":"FDR-0400",
          "errors": [
            {
              "path":"createFlow.createFlowRequest.reportingFlowName",
              "message":"non deve essere null"
            }
          ]
        }
        """.formatted(""), ErrorResponse.class);
    String res = prettyPrint(given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0401 - JSON incorrect value")
  void test_psp_KO_FDR0401() {
    String flowName = getFlowName();
    String url = PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);
    String wrongFormatDecimal = "0,01";
    String bodyFmt = PAYMENTS_ADD_INVALID_FIELD_VALUE_FORMAT_TEMPLATE.formatted(wrongFormatDecimal);
    String responseFmt =
        prettyPrint("""
        {
          "httpStatusCode":400,
          "httpStatusDescription":"Bad Request",
          "appErrorCode":"FDR-0401",
          "errors": [
            {
              "message":"Bad request. Field [payments.pay] is [%s]. Not match a correct value"
            }
          ]
        }
        """.formatted(wrongFormatDecimal), ErrorResponse.class);
    String res =
        prettyPrint(
            given()
                .body(bodyFmt)
                .header(HEADER)
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
  @DisplayName("PSPS - KO FDR-0402 - JSON invalid input instant")
  void test_psp_KO_FDR0402() {
    String flowName = getFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE);
    String wrongFormatDate = "2023-04-05";
    String bodyFmt = FLOW_TEMPLATE_WRONG_INSTANT.formatted(
        flowName,
        wrongFormatDate,
        SenderTypeEnum.LEGAL_PERSON.name(),
        PSP_CODE,
        BROKER_CODE,
        CHANNEL_CODE,
        EC_CODE);

    String responseFmt =
        prettyPrint("""
        {
          "httpStatusCode":400,
          "httpStatusDescription":"Bad Request",
          "appErrorCode":"FDR-0402",
          "errors": [
            {
              "message":"Bad request. Field [reportingFlowDate] is [%s]. Expected ISO-8601 [2011-12-03T10:15:30Z] [2023-04-05T09:21:37.810000Z]"
            }
          ]
        }
        """.formatted(wrongFormatDate), ErrorResponse.class);
    String res = prettyPrint(given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0403 - JSON invalid input enum")
  void test_psp_KO_FDR0403() {
    String flowName = getFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE);
    String wrongEnum = "WRONG_ENUM";
    String bodyFmt = FLOW_TEMPLATE.formatted(
        flowName,
        wrongEnum,
        PSP_CODE,
        BROKER_CODE,
        CHANNEL_CODE,
        EC_CODE);

    String responseFmt =
        prettyPrint("""
        {
          "httpStatusCode":400,
          "httpStatusDescription":"Bad Request",
          "appErrorCode":"FDR-0403",
          "errors": [
            {
              "message":"Bad request. Field [sender.type] is [%s]. Expected value one of [LEGAL_PERSON, ABI_CODE, BIC_CODE]"
            }
          ]
        }
        """.formatted(wrongEnum), ErrorResponse.class);
    String res = prettyPrint(given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0404 - JSON deserialization error")
  void test_psp_KO_FDR0404() {
    String flowName = getFlowName();
    String url = PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);
    String bodyFmt = PAYMENTS_ADD_INVALID_FORMAT_TEMPLATE;
    String responseFmt =
        prettyPrint("""
        {
          "httpStatusCode":400,
          "httpStatusDescription":"Bad Request",
          "appErrorCode":"FDR-0404",
          "errors": [
            {
              "message":"Bad request. Field [payments] generate an deserialize error. Set correct value"
            }
          ]
        }
        """, ErrorResponse.class);
    String res =
        prettyPrint(
            given()
                .body(bodyFmt)
                .header(HEADER)
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
  @DisplayName("PSPS - KO FDR-0405 - JSON malformed")
  void test_psp_KO_FDR0405() {
    String url = FLOWS_URL.formatted(PSP_CODE);
    String bodyFmt = MALFORMED_JSON;

    String responseFmt =
        prettyPrint("""
        {
          "httpStatusCode":400,
          "httpStatusDescription":"Bad Request",
          "appErrorCode":"FDR-0405",
          "errors": [
            {
              "message":"Bad request. Json format not valid"
            }
          ]
        }
        """, ErrorResponse.class);
    String res = prettyPrint(given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }


}
