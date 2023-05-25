package it.gov.pagopa.fdr.rest.organizations;

import static io.restassured.RestAssured.given;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.EC_CODE;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.EC_CODE_NOT_ENABLED;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.HEADER;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.PSP_CODE;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.PSP_CODE_2;
import static it.gov.pagopa.fdr.util.AppConstantTestHelper.PSP_CODE_NOT_ENABLED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.rest.BaseUnitTestHelper;
import it.gov.pagopa.fdr.rest.exceptionmapper.ErrorResponse;
import it.gov.pagopa.fdr.rest.model.GenericResponse;
import it.gov.pagopa.fdr.rest.model.ReportingFlowStatusEnum;
import it.gov.pagopa.fdr.rest.organizations.response.GetAllResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetIdResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetPaymentResponse;
import it.gov.pagopa.fdr.util.MongoResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@QuarkusTestResource(MockServerTestResource.class)
@QuarkusTestResource(MongoResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrganizationResourceTest extends BaseUnitTestHelper {

  private static final String GET_ALL_PUBLISHED_FLOW_URL = "/organizations/%s/flows?idPsp=%s";
  private static final String GET_REPORTING_FLOW_URL = "/organizations/%s/flows/%s/psps/%s";
  private static final String GET_REPORTING_FLOW_PAYMENTS_URL = "/organizations/%s/flows/%s/psps/%s/payments";
  private static final String CHANGE_READ_FLAG_URL = "/organizations/%s/flows/%s/psps/%s/read";

  private static String RESPONSE_ALL_PUBLISHED_FLOWS = """
      {
          "metadata": {
              "pageSize": 50,
              "pageNumber": 1,
              "totPage": 1
          },
          "count": 1,
          "data": [
              {
                  "name": "%s",
                  "pspId": "%s"
              }
          ]
      }""";

  private static String RESPONSE_ALL_PUBLISHED_FLOWS_NO_RESULT = """
      {
        "metadata" : {
          "pageSize" : 50,
          "pageNumber" : 1,
          "totPage" : 1
        },
        "count" : 0,
        "data" : [ ]
      }
    """;

  private static String RESPONSE_GET_REPORTING_FLOW_PAYMENTS = """
      {
        "metadata" : {
          "pageSize" : 50,
          "pageNumber" : 1,
          "totPage" : 1
        },
        "count" : 3,
        "data" : [ {
          "iuv" : "a",
          "iur" : "abcdefg",
          "index" : 1,
          "pay" : 0.01,
          "payStatus" : "EXECUTED",
          "payDate" : "2023-02-03T12:00:30.900Z"
        }, {
          "iuv" : "b",
          "iur" : "abcdefg",
          "index" : 2,
          "pay" : 0.01,
          "payStatus" : "REVOKED",
          "payDate" : "2023-02-03T12:00:30.900Z"
        }, {
          "iuv" : "c",
          "iur" : "abcdefg",
          "index" : 3,
          "pay" : 0.01,
          "payStatus" : "NO_RPT",
          "payDate" : "2023-02-03T12:00:30.900Z"
        } ]
      }""";

  private static String CHANGE_READ_FLAG_RESPONSE =
      """
      {
        "message":"Flow [%s] read"
      }
      """;

  /** ############### getAllPublishedFlow ################ */
  @Test
  @Order(1)
  @DisplayName("ORGANIZATIONS - OK - getAllPublishedFlow")
  void testOrganization_getAllPublishedFlow_Ok() {
    String flowName = getFlowName();
    pspSunnyDay(flowName);
    String url = GET_ALL_PUBLISHED_FLOW_URL.formatted(EC_CODE, PSP_CODE);
    String flowNameContained = "\"name\" : \"%s\"".formatted(flowName);
    String res = prettyPrint(given()
        .header(HEADER)
        .when()
        .get(url)
        .then()
        .statusCode(200)
        .extract()
        .as(GetAllResponse.class));
    assertThat(res, containsString(flowNameContained));
  }

  @Test
  @Order(2)
  @DisplayName("ORGANIZATIONS - OK - getAllPublishedFlow no results")
  void testOrganization_getAllPublishedFlow_OkNoResults() {
    String flowName = getFlowName();
    pspSunnyDay(flowName);
    String url = GET_ALL_PUBLISHED_FLOW_URL.formatted(EC_CODE, PSP_CODE_2);
    String responseFmt = prettyPrint(RESPONSE_ALL_PUBLISHED_FLOWS_NO_RESULT, GetAllResponse.class);
    String res = prettyPrint(given()
        .header(HEADER)
        .when()
        .get(url)
        .then()
        .statusCode(200)
        .extract()
        .as(GetAllResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @Order(3)
  @DisplayName("ORGANIZATIONS - KO FDR-0708 - psp unknown")
  void testOrganization_getAllPublishedFlow_KO_FDR0708() {
    String pspUnknown = "PSP_UNKNOWN";
    String url = GET_ALL_PUBLISHED_FLOW_URL.formatted(EC_CODE, pspUnknown, 10, 10);
    String responseFmt =
        prettyPrint("""
        {
           "httpStatusCode":400,
           "httpStatusDescription":"Bad Request",
           "appErrorCode":"FDR-0708",
           "errors":[
              {
                 "message":"Psp [PSP_UNKNOWN] unknown"
              }
           ]
        }""", ErrorResponse.class);
    String res = prettyPrint(given()
        .header(HEADER)
        .when()
        .get(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @Order(4)
  @DisplayName("ORGANIZATIONS - KO FDR-0709 - psp not enabled")
  void testOrganization_getAllPublishedFlow_KO_FDR0709() {
    String url = GET_ALL_PUBLISHED_FLOW_URL.formatted(EC_CODE, PSP_CODE_NOT_ENABLED, 10, 10);
    String responseFmt =
        prettyPrint("""
        {
           "httpStatusCode":400,
           "httpStatusDescription":"Bad Request",
           "appErrorCode":"FDR-0709",
           "errors":[
              {
                 "message":"Psp [pspNotEnabled] not enabled"
              }
           ]
        }""", ErrorResponse.class);

    String res = prettyPrint(given()
        .header(HEADER)
        .when()
        .get(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @Order(5)
  @DisplayName("ORGANIZATIONS - KO FDR-0716 - creditor institution unknown")
  void testOrganization_getAllPublishedFlow_KO_FDR0716() {
    String ecUnknown = "EC_UNKNOWN";
    String url = GET_ALL_PUBLISHED_FLOW_URL.formatted(ecUnknown, PSP_CODE, 10, 10);
    String responseFmt =
        prettyPrint("""
        {
           "httpStatusCode":400,
           "httpStatusDescription":"Bad Request",
           "appErrorCode":"FDR-0716",
           "errors":[
              {
                 "message":"Creditor institution [EC_UNKNOWN] unknown"
              }
           ]
        }""", ErrorResponse.class);

    String res = prettyPrint(given()
        .header(HEADER)
        .when()
        .get(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @Order(6)
  @DisplayName("ORGANIZATIONS - KO FDR-0717 - creditor institution not enabled")
  void testOrganization_getAllPublishedFlow_KO_FDR0717() {
    String url = GET_ALL_PUBLISHED_FLOW_URL.formatted(EC_CODE_NOT_ENABLED, PSP_CODE, 10, 10);
    String responseFmt =
        prettyPrint("""
        {
           "httpStatusCode":400,
           "httpStatusDescription":"Bad Request",
           "appErrorCode":"FDR-0717",
           "errors":[
              {
                 "message":"Creditor institution [%s] not enabled"
              }
           ]
        }""".formatted(EC_CODE_NOT_ENABLED), ErrorResponse.class);

    String res = prettyPrint(given()
        .header(HEADER)
        .when()
        .get(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  /** ################# getReportingFlow ############### */
  @Test
  @Order(7)
  @DisplayName("ORGANIZATIONS - OK - recupero di un reporting flow")
  void testOrganization_getReportingFlow_Ok() {
    String flowName = getFlowName();
    pspSunnyDay(flowName);
    String url = GET_REPORTING_FLOW_URL.formatted(EC_CODE, flowName, PSP_CODE);
    GetIdResponse res = given()
        .header(HEADER)
        .when()
        .get(url)
        .then()
        .statusCode(200)
        .extract()
        .as(GetIdResponse.class);
    assertThat(res.getReportingFlowName(), equalTo(flowName));
    assertThat(res.getReceiver().getEcId(), equalTo(EC_CODE));
    assertThat(res.getSender().getPspId(), equalTo(PSP_CODE));
    assertThat(res.getStatus(), equalTo(ReportingFlowStatusEnum.PUBLISHED));
    assertThat(res.totPayments, equalTo(3L));
  }

  @Test
  @Order(8)
  @DisplayName("ORGANIZATIONS - OK - recupero di un reporting flow pubblicato alla revision 2")
  void testOrganization_getReportingFlow_revision_2_OK() {
    String flowName = getFlowName();
    assertThat(pspSunnyDay(flowName), equalTo(Boolean.TRUE));
    assertThat(pspSunnyDay(flowName), equalTo(Boolean.TRUE));

    String url = GET_REPORTING_FLOW_URL.formatted(EC_CODE, flowName, PSP_CODE);
    String reportingFlowNameContained = "\"reportingFlowName\" : \"%s\"".formatted(flowName);
    String revisionContained = "\"revision\" : 2";
    String statusContained = "\"status\" : \"PUBLISHED\"";
    String res = prettyPrint(given()
        .header(HEADER)
        .when()
        .get(url)
        .then()
        .statusCode(200)
        .extract()
        .as(GetIdResponse.class));
    assertThat(res, containsString(reportingFlowNameContained));
    assertThat(res, containsString(revisionContained));
    assertThat(res, containsString(statusContained));
  }

  @Test
  @Order(9)
  @DisplayName("ORGANIZATIONS - KO FDR-0701 - getReportingFlow reporting flow not found")
  void testOrganization_getReportingFlow_KO_FDR0701() {
    String flowName = getFlowName();
    pspSunnyDay(flowName);
    String flowNameWrong = getFlowName();
    String url = GET_REPORTING_FLOW_URL.formatted(EC_CODE, flowNameWrong, PSP_CODE);
    String responseFmt =
        prettyPrint("""
        {
           "httpStatusCode":404,
           "httpStatusDescription":"Not Found",
           "appErrorCode":"FDR-0701",
           "errors":[
              {
                 "message":"Reporting flow [%s] not found"
              }
           ]
        }""".formatted(flowNameWrong), ErrorResponse.class);

    String res = prettyPrint(given()
        .header(HEADER)
        .when()
        .get(url)
        .then()
        .statusCode(404)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  /** ################# getReportingFlowPayments ############### */
  @Test
  @Order(10)
  @DisplayName("ORGANIZATIONS - OK - recupero dei payments di un flow pubblicato")
  void testOrganization_getReportingFlowPayments_Ok() {
    String flowName = getFlowName();
    pspSunnyDay(flowName);
    String url = GET_REPORTING_FLOW_PAYMENTS_URL.formatted(EC_CODE, flowName, PSP_CODE);
    String res = prettyPrint(given()
        .header(HEADER)
        .when()
        .get(url)
        .then()
        .statusCode(200)
        .extract()
        .as(GetPaymentResponse.class));
    assertThat(res, equalTo(RESPONSE_GET_REPORTING_FLOW_PAYMENTS));
  }

  /** ################# changeReadFlag ############### */
  @Test
  @Order(11)
  @DisplayName("ORGANIZATIONS - OK - changeReadFlag")
  void testOrganization_changeReadFlag_Ok() {
    String flowName = getFlowName();
    pspSunnyDay(flowName);
    String url = CHANGE_READ_FLAG_URL.formatted(EC_CODE, flowName, PSP_CODE);
    String responseFmt = prettyPrint(CHANGE_READ_FLAG_RESPONSE.formatted(flowName), GenericResponse.class);
    String res = prettyPrint(given()
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
  @Order(12)
  @DisplayName("ORGANIZATIONS - KO FDR-0701 - changeReadFlag reporting flow not found")
  void testOrganization_changeReadFlag_KO_FDR0701() {
    String flowName = getFlowName();
    pspSunnyDay(flowName);
    String flowNameWrong = getFlowName();
    String url = CHANGE_READ_FLAG_URL.formatted(EC_CODE, flowNameWrong, PSP_CODE);
    String responseFmt =
        prettyPrint("""
        {
           "httpStatusCode":404,
           "httpStatusDescription":"Not Found",
           "appErrorCode":"FDR-0701",
           "errors":[
              {
                 "message":"Reporting flow [%s] not found"
              }
           ]
        }""".formatted(flowNameWrong), ErrorResponse.class);

    String res = prettyPrint(given()
        .header(HEADER)
        .when()
        .put(url)
        .then()
        .statusCode(404)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

}
