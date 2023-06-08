package it.gov.pagopa.fdr.rest.organizations;

import static io.restassured.RestAssured.given;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.HEADER;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.PSP_CODE;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.PSP_CODE_2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;

import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.rest.exceptionmapper.ErrorResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetAllInternalResponse;
import it.gov.pagopa.fdr.test.util.AzuriteResource;
import it.gov.pagopa.fdr.test.util.MongoResource;
import it.gov.pagopa.fdr.test.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(MockServerTestResource.class)
@QuarkusTestResource(MongoResource.class)
@QuarkusTestResource(AzuriteResource.class)
class InternalOrganizationResourceTest {

  private static final String GET_ALL_PUBLISHED_FLOW_URL =
      "/internal/history/organizations/ndp/flows?idPsp=%s";
  private static final String GET_REPORTING_FLOW_URL =
      "/internal/history/organizations/ndp/flows/%s/rev/%s/psps/%s";
  private static final String GET_REPORTING_FLOW_PAYMENTS_URL =
      "/internal/history/organizations/ndp/flows/%s/rev/%s/psps/%s/payments";
  private static final String CHANGE_READ_FLAG_URL =
      "/internal/history/organizations/ndp/flows/%s/rev/%s/psps/%s/read";

  private static String RESPONSE_ALL_PUBLISHED_FLOWS =
      """
      {
         "metadata":{
            "pageSize":50,
            "pageNumber":1,
            "totPage":1
         },
         "count":1,
         "data":[
            {
              "name":"%s",
              "pspId":"%s",
              "revision":1
            }
         ]
      }
      """;

  private static String RESPONSE_ALL_PUBLISHED_FLOWS_NO_RESULT =
      """
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

  private static String RESPONSE_GET_REPORTING_FLOW_PAYMENTS =
      """
      {
        "metadata" : {
          "pageSize" : 50,
          "pageNumber" : 1,
          "totPage" : 1
        },
        "count" : 0,
        "data" : [ ]
      }""";

  private static String CHANGE_READ_FLAG_RESPONSE =
      """
      {
        "message":"Flow [%s] internal read"
      }
      """;

  /** ############### getAllPublishedFlow ################ */
  @Test
  @Order(1)
  @DisplayName("ORGANIZATIONS - OK - getAllPublishedFlow")
  void testOrganization_getAllPublishedFlow_Ok() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName);
    String url = GET_ALL_PUBLISHED_FLOW_URL.formatted(PSP_CODE);
    GetAllInternalResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(GetAllInternalResponse.class);
    assertThat(res.getCount(), greaterThan(0L));
  }

  @Test
  @DisplayName("ORGANIZATIONS - OK - getAllPublishedFlow no results")
  void testOrganization_getAllPublishedFlow_OkNoResults() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName);
    String url = GET_ALL_PUBLISHED_FLOW_URL.formatted(PSP_CODE_2);
    GetAllInternalResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(GetAllInternalResponse.class);
    assertThat(res.getCount(), equalTo(0L));
  }

  @Test
  @DisplayName("ORGANIZATIONS - KO FDR-0708 - psp unknown")
  void testOrganization_getAllPublishedFlow_KO_FDR0708() {
    String pspUnknown = "PSP_UNKNOWN";
    String url = GET_ALL_PUBLISHED_FLOW_URL.formatted(pspUnknown, 10, 10);
    ErrorResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(400)
            .extract()
            .as(ErrorResponse.class);
    assertThat(res.getHttpStatusCode(), equalTo(400));
    assertThat(res.getHttpStatusDescription(), equalTo("Bad Request"));
    assertThat(res.getAppErrorCode(), equalTo("FDR-0708"));
    assertThat(res.getErrors(), hasSize(1));
    assertThat(
        res.getErrors(),
        hasItem(hasProperty("message", equalTo(String.format("Psp [%s] unknown", pspUnknown)))));
  }

  //  @Test
  //  @DisplayName("ORGANIZATIONS - KO FDR-0709 - psp not enabled")
  //  void testOrganization_getAllPublishedFlow_KO_FDR0709() {
  //    String url = GET_ALL_PUBLISHED_FLOW_URL.formatted(PSP_CODE_NOT_ENABLED, 10, 10);
  //    String responseFmt =
  //        prettyPrint(
  //            """
  //        {
  //           "httpStatusCode":400,
  //           "httpStatusDescription":"Bad Request",
  //           "appErrorCode":"FDR-0709",
  //           "errors":[
  //              {
  //                 "message":"Psp [pspNotEnabled] not enabled"
  //              }
  //           ]
  //        }""",
  //            ErrorResponse.class);
  //
  //    String res =
  //        prettyPrint(
  //            given()
  //                .header(HEADER)
  //                .when()
  //                .get(url)
  //                .then()
  //                .statusCode(400)
  //                .extract()
  //                .as(ErrorResponse.class));
  //    assertThat(res, equalTo(responseFmt));
  //  }
  //
  //  /** ################# getReportingFlow ############### */
  //  @Test
  //  @DisplayName("ORGANIZATIONS - OK - recupero di un reporting flow")
  //  void testOrganization_getReportingFlow_Ok() {
  //    String flowName = getFlowName();
  //    pspSunnyDay(flowName);
  //    String url = GET_REPORTING_FLOW_URL.formatted(flowName, 1, PSP_CODE);
  //    GetIdResponse res =
  //        given()
  //            .header(HEADER)
  //            .when()
  //            .get(url)
  //            .then()
  //            .statusCode(200)
  //            .extract()
  //            .as(GetIdResponse.class);
  //    assertThat(res.getReportingFlowName(), equalTo(flowName));
  //    assertThat(res.getReceiver().getEcId(), equalTo(EC_CODE));
  //    assertThat(res.getSender().getPspId(), equalTo(PSP_CODE));
  //    assertThat(res.getStatus(), equalTo(ReportingFlowStatusEnum.PUBLISHED));
  //    assertThat(res.totPayments, equalTo(3L));
  //  }
  //
  //  @Test
  //  @DisplayName("ORGANIZATIONS - OK - recupero di un reporting flow pubblicato alla revision 2")
  //  void testOrganization_getReportingFlow_revision_2_OK() {
  //    String flowName = getFlowName();
  //    assertThat(pspSunnyDay(flowName), equalTo(Boolean.TRUE));
  //    assertThat(pspSunnyDay(flowName), equalTo(Boolean.TRUE));
  //
  //    String url = GET_REPORTING_FLOW_URL.formatted(flowName, 2, PSP_CODE);
  //    String res =
  //        prettyPrint(
  //            given()
  //                .header(HEADER)
  //                .when()
  //                .get(url)
  //                .then()
  //                .statusCode(200)
  //                .extract()
  //                .as(GetIdResponse.class));
  //    assertThat(res, containsString("\"reportingFlowName\" : \"%s\"".formatted(flowName)));
  //    assertThat(res, containsString("\"revision\" : 2"));
  //    assertThat(res, containsString("\"status\" : \"PUBLISHED\""));
  //  }
  //
  //  @Test
  //  @DisplayName("ORGANIZATIONS - KO FDR-0701 - getReportingFlow reporting flow not found")
  //  void testOrganization_getReportingFlow_KO_FDR0701() {
  //    String flowName = getFlowName();
  //    pspSunnyDay(flowName);
  //    String flowNameWrong = getFlowName();
  //    String url = GET_REPORTING_FLOW_URL.formatted(flowNameWrong, 1, PSP_CODE);
  //    String responseFmt =
  //        prettyPrint(
  //            """
  //        {
  //           "httpStatusCode":404,
  //           "httpStatusDescription":"Not Found",
  //           "appErrorCode":"FDR-0701",
  //           "errors":[
  //              {
  //                 "message":"Reporting flow [%s] not found"
  //              }
  //           ]
  //        }"""
  //                .formatted(flowNameWrong),
  //            ErrorResponse.class);
  //
  //    String res =
  //        prettyPrint(
  //            given()
  //                .header(HEADER)
  //                .when()
  //                .get(url)
  //                .then()
  //                .statusCode(404)
  //                .extract()
  //                .as(ErrorResponse.class));
  //    assertThat(res, equalTo(responseFmt));
  //  }
  //
  //  /** ################# getReportingFlowPayments ############### */
  //  @Test
  //  @DisplayName("ORGANIZATIONS - OK - recupero dei payments di un flow pubblicato")
  //  void testOrganization_getReportingFlowPayments_Ok() {
  //    String flowName = getFlowName();
  //    pspSunnyDay(flowName);
  //    String url = GET_REPORTING_FLOW_PAYMENTS_URL.formatted(flowName, 1, PSP_CODE);
  //    String res =
  //        prettyPrint(
  //            given()
  //                .header(HEADER)
  //                .when()
  //                .get(url)
  //                .then()
  //                .statusCode(200)
  //                .extract()
  //                .as(GetPaymentResponse.class));
  //    assertThat(3, equalTo(StringUtils.countMatches(res, "abcdefg")));
  //    assertThat(res, containsString(PaymentStatusEnum.EXECUTED.toString()));
  //    assertThat(res, containsString(PaymentStatusEnum.REVOKED.toString()));
  //    assertThat(res, containsString(PaymentStatusEnum.NO_RPT.toString()));
  //  }
  //
  //  /** ################# changeReadFlag ############### */
  //  @Test
  //  @DisplayName("ORGANIZATIONS - OK - changeReadFlag")
  //  void testOrganization_changeReadFlag_Ok() {
  //    String flowName = getFlowName();
  //    pspSunnyDay(flowName);
  //    String url = CHANGE_READ_FLAG_URL.formatted(flowName, 1, PSP_CODE);
  //    String responseFmt =
  //        prettyPrint(CHANGE_READ_FLAG_RESPONSE.formatted(flowName), GenericResponse.class);
  //    String res =
  //        prettyPrint(
  //            given()
  //                .header(HEADER)
  //                .when()
  //                .put(url)
  //                .then()
  //                .statusCode(200)
  //                .extract()
  //                .as(GenericResponse.class));
  //    assertThat(res, equalTo(responseFmt));
  //  }
  //
  //  @Test
  //  @DisplayName("ORGANIZATIONS - KO FDR-0701 - changeReadFlag reporting flow not found")
  //  void testOrganization_changeReadFlag_KO_FDR0701() {
  //    String flowName = getFlowName();
  //    pspSunnyDay(flowName);
  //    String flowNameWrong = getFlowName();
  //    String url = CHANGE_READ_FLAG_URL.formatted(flowNameWrong, 1, PSP_CODE);
  //    String responseFmt =
  //        prettyPrint(
  //            """
  //        {
  //           "httpStatusCode":404,
  //           "httpStatusDescription":"Not Found",
  //           "appErrorCode":"FDR-0701",
  //           "errors":[
  //              {
  //                 "message":"Reporting flow [%s] not found"
  //              }
  //           ]
  //        }"""
  //                .formatted(flowNameWrong),
  //            ErrorResponse.class);
  //
  //    String res =
  //        prettyPrint(
  //            given()
  //                .header(HEADER)
  //                .when()
  //                .put(url)
  //                .then()
  //                .statusCode(404)
  //                .extract()
  //                .as(ErrorResponse.class));
  //    assertThat(res, equalTo(responseFmt));
  //  }
}
