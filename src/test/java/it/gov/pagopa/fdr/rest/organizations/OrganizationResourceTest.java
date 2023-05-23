package it.gov.pagopa.fdr.rest.organizations;

import static io.restassured.RestAssured.given;
import static it.gov.pagopa.fdr.Constants.brokerCode;
import static it.gov.pagopa.fdr.Constants.channelCode;
import static it.gov.pagopa.fdr.Constants.ecCode;
import static it.gov.pagopa.fdr.Constants.flowsUrl;
import static it.gov.pagopa.fdr.Constants.header;
import static it.gov.pagopa.fdr.Constants.pspCode;
import static it.gov.pagopa.fdr.Constants.pspCode2;
import static it.gov.pagopa.fdr.Constants.pspCodeNotEnabled;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.Header;
import it.gov.pagopa.fdr.rest.BaseResource;
import it.gov.pagopa.fdr.rest.exceptionmapper.ErrorResponse;
import it.gov.pagopa.fdr.rest.model.GenericResponse;
import it.gov.pagopa.fdr.rest.model.ReportingFlowStatusEnum;
import it.gov.pagopa.fdr.rest.organizations.response.GetAllResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetIdResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetPaymentResponse;
import it.gov.pagopa.fdr.service.dto.SenderTypeEnumDto;
import it.gov.pagopa.fdr.util.MongoResource;
import it.gov.pagopa.fdr.util.TestUtil;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(MockServerTestResource.class)
@QuarkusTestResource(MongoResource.class)
class OrganizationResourceTest extends BaseResource {

  @Inject TestUtil testUtil;

  private static final String ecCode = "12345678900";
  private static final String ecCodeNotEnabled = "00987654321";
  private static final Header header = new Header("Content-Type", "application/json");

  private static final String getAllPublishedFlowUrl = "/organizations/%s/flows?idPsp=%s";
  private static final String getReportingFlowUrl = "/organizations/%s/flows/%s/psps/%s";
  private static final String getReportingFlowPaymentsUrl = "/organizations/%s/flows/%s/psps/%s/payments";
  private static final String changeReadFlagUrl = "/organizations/%s/flows/%s/psps/%s/read";

  private static String responseAllPublishedFlows = """
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

  private static String responseAllPublishedFlowsNoResult = """
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

  private static String responsePublishedFlow = """
      {
         "status":"%s",
         "revision":1,
         "created":"2023-05-23T13:32:09.844Z",
         "updated":"2023-05-23T13:32:12.457Z",
         "reportingFlowName":"%s",
         "reportingFlowDate":"2023-04-05T09:21:37.810Z",
         "regulation":"SEPA - Bonifico xzy",
         "regulationDate":"2023-04-03T12:00:30.900Z",
         "bicCodePouringBank":"UNCRITMMXXX",
         "sender":{
            "type":"LEGAL_PERSON",
            "id":"SELBIT2B",
            "pspId":"%s",
            "pspName":"Bank",
            "brokerId":"intTest",
            "channelId":"canaleTest",
            "password":"1234567890"
         },
         "receiver":{
            "id":"APPBIT2B",
            "ecId":"%s",
            "ecName":"Comune di xyz"
         },
         "totPayments":3,
         "sumPayments":0.03
      }
      """;

  private static String responseGetReportingFlowPayments = """
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

  private static String changeReadFlagResponse =
      """
      {
        "message":"Flow [%s] read"
      }
      """;

  /** ############### getAllPublishedFlow ################ */
  @Test
  @DisplayName("ORGANIZATIONS getAllPublishedFlow Ok")
  void testOrganization_getAllPublishedFlow_Ok() {
    String flowName = getFlowName();
    pspSunnyDay(flowName);
    String url = getAllPublishedFlowUrl.formatted(ecCode, pspCode);
    String responseFmt = testUtil.prettyPrint(responseAllPublishedFlows.formatted(flowName, pspCode), GetAllResponse.class);
    String res = testUtil.prettyPrint(given()
        .header(header)
        .when()
        .get(url)
        .then()
        .statusCode(200)
        .extract()
        .as(GetAllResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("ORGANIZATIONS getAllPublishedFlow no results OK")
  void testOrganization_getAllPublishedFlow_OkNoResults() {
    String flowName = getFlowName();
    pspSunnyDay(flowName);
    String url = getAllPublishedFlowUrl.formatted(ecCode, pspCode2);
    String responseFmt = testUtil.prettyPrint(responseAllPublishedFlowsNoResult, GetAllResponse.class);
    String res = testUtil.prettyPrint(given()
        .header(header)
        .when()
        .get(url)
        .then()
        .statusCode(200)
        .extract()
        .as(GetAllResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("ORGANIZATIONS - KO FDR-0708 - psp unknown")
  void testOrganization_getAllPublishedFlow_KO_FDR0708() {
    String pspUnknown = "PSP_UNKNOWN";
    String url = getAllPublishedFlowUrl.formatted(ecCode, pspUnknown, 10, 10);
    String responseFmt =
        testUtil.prettyPrint("""
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
    String res = testUtil.prettyPrint(given()
        .header(header)
        .when()
        .get(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("ORGANIZATIONS - KO FDR-0709 - psp not enabled")
  void testOrganization_getAllPublishedFlow_KO_FDR0709() {
    String url = getAllPublishedFlowUrl.formatted(ecCode, pspCodeNotEnabled, 10, 10);
    String responseFmt =
        testUtil.prettyPrint("""
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

    String res = testUtil.prettyPrint(given()
        .header(header)
        .when()
        .get(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("ORGANIZATIONS - KO FDR-0716 - creditor institution unknown")
  void testOrganization_getAllPublishedFlow_KO_FDR0716() {
    String ecUnknown = "EC_UNKNOWN";
    String url = getAllPublishedFlowUrl.formatted(ecUnknown, pspCode, 10, 10);
    String responseFmt =
        testUtil.prettyPrint("""
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

    String res = testUtil.prettyPrint(given()
        .header(header)
        .when()
        .get(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("ORGANIZATIONS - KO FDR-0717 - creditor institution not enabled")
  void testOrganization_getAllPublishedFlow_KO_FDR0717() {
    String url = getAllPublishedFlowUrl.formatted(ecCodeNotEnabled, pspCode, 10, 10);
    String responseFmt =
        testUtil.prettyPrint("""
        {
           "httpStatusCode":400,
           "httpStatusDescription":"Bad Request",
           "appErrorCode":"FDR-0717",
           "errors":[
              {
                 "message":"Creditor institution [%s] not enabled"
              }
           ]
        }""".formatted(ecCodeNotEnabled), ErrorResponse.class);

    String res = testUtil.prettyPrint(given()
        .header(header)
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
  @DisplayName("ORGANIZATIONS getReportingFlow Ok")
  void testOrganization_getReportingFlow_Ok() {
    String flowName = getFlowName();
    pspSunnyDay(flowName);
    String url = getReportingFlowUrl.formatted(ecCode, flowName, pspCode);
    GetIdResponse res = given()
        .header(header)
        .when()
        .get(url)
        .then()
        .statusCode(200)
        .extract()
        .as(GetIdResponse.class);
    assertThat(res.getReportingFlowName(), equalTo(flowName));
    assertThat(res.getReceiver().getEcId(), equalTo(ecCode));
    assertThat(res.getSender().getPspId(), equalTo(pspCode));
    assertThat(res.getStatus(), equalTo(ReportingFlowStatusEnum.PUBLISHED));
    assertThat(res.totPayments, equalTo(3L));
  }

  @Test
  @DisplayName("ORGANIZATIONS - KO FDR-0701 - getReportingFlow reporting flow not found")
  void testOrganization_getReportingFlow_KO_FDR0701() {
    String flowName = getFlowName();
    pspSunnyDay(flowName);
    String flowNameWrong = getFlowName();
    String url = getReportingFlowUrl.formatted(ecCode, flowNameWrong, pspCode);
    String responseFmt =
        testUtil.prettyPrint("""
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

    String res = testUtil.prettyPrint(given()
        .header(header)
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
  @DisplayName("ORGANIZATIONS getReportingFlowPayments Ok")
  void testOrganization_getReportingFlowPayments_Ok() {
    String flowName = getFlowName();
    pspSunnyDay(flowName);
    String url = getReportingFlowPaymentsUrl.formatted(ecCode, flowName, pspCode);
    String res = testUtil.prettyPrint(given()
        .header(header)
        .when()
        .get(url)
        .then()
        .statusCode(200)
        .extract()
        .as(GetPaymentResponse.class));
    assertThat(res, equalTo(responseGetReportingFlowPayments));
  }

  /** ################# changeReadFlag ############### */
  @Test
  @DisplayName("ORGANIZATIONS changeReadFlag Ok")
  void testOrganization_changeReadFlag_Ok() {
    String flowName = getFlowName();
    pspSunnyDay(flowName);
    String url = changeReadFlagUrl.formatted(ecCode, flowName, pspCode);
    String responseFmt = testUtil.prettyPrint(changeReadFlagResponse.formatted(flowName), GenericResponse.class);
    String res = testUtil.prettyPrint(given()
        .header(header)
        .when()
        .put(url)
        .then()
        .statusCode(200)
        .extract()
        .as(GenericResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

  @Test
  @DisplayName("ORGANIZATIONS - KO FDR-0701 - changeReadFlag reporting flow not found")
  void testOrganization_changeReadFlag_KO_FDR0701() {
    String flowName = getFlowName();
    pspSunnyDay(flowName);
    String flowNameWrong = getFlowName();
    String url = changeReadFlagUrl.formatted(ecCode, flowNameWrong, pspCode);
    String responseFmt =
        testUtil.prettyPrint("""
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

    String res = testUtil.prettyPrint(given()
        .header(header)
        .when()
        .put(url)
        .then()
        .statusCode(404)
        .extract()
        .as(ErrorResponse.class));
    assertThat(res, equalTo(responseFmt));
  }

}
