package it.gov.pagopa.fdr.rest.organizations;

import static io.restassured.RestAssured.given;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;

import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.rest.exceptionmapper.ErrorResponse;
import it.gov.pagopa.fdr.rest.model.PaymentStatusEnum;
import it.gov.pagopa.fdr.rest.model.ReportingFlowStatusEnum;
import it.gov.pagopa.fdr.rest.organizations.response.GetAllResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetPaymentResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetResponse;
import it.gov.pagopa.fdr.test.util.AzuriteResource;
import it.gov.pagopa.fdr.test.util.MongoResource;
import it.gov.pagopa.fdr.test.util.TestUtil;
import it.gov.pagopa.fdr.util.AppConstant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(MockServerTestResource.class)
@QuarkusTestResource(MongoResource.class)
@QuarkusTestResource(AzuriteResource.class)
class InternalOrganizationResourceTest {

  private static final String GET_ALL_PUBLISHED_FLOW_URL =
      "/internal/organizations/%s/fdrs?" + AppConstant.PSP + "=%s";
  private static final String GET_REPORTING_FLOW_URL =
      "/internal/organizations/%s/fdrs/%s/revisions/%s/psps/%s";
  private static final String GET_REPORTING_FLOW_PAYMENTS_URL =
      "/internal/organizations/%s/fdrs/%s/revisions/%s/psps/%s/payments";

  /** ############### getAllPublishedFlow ################ */
  @Test
  @DisplayName("ORGANIZATIONS - OK - getAllPublishedFlow")
  void testOrganization_getAllPublishedFlow_Ok() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName);
    String url = GET_ALL_PUBLISHED_FLOW_URL.formatted(EC_CODE, PSP_CODE);
    GetAllResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(GetAllResponse.class);
    assertThat(res.getCount(), greaterThan(0L));
    assertThat(
        res.getData(),
        hasItem(
            anyOf(
                hasProperty("name", equalTo(flowName)), hasProperty("pspId", equalTo(PSP_CODE)))));
  }

  @Test
  @DisplayName("ORGANIZATIONS - OK - getAllPublishedFlow no results")
  void testOrganization_getAllPublishedFlow_OkNoResults() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName);
    String url = GET_ALL_PUBLISHED_FLOW_URL.formatted(EC_CODE, PSP_CODE_2);
    GetAllResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(GetAllResponse.class);
    assertThat(res.getCount(), equalTo(0L));
  }

  @Test
  @DisplayName("ORGANIZATIONS - KO FDR-0708 - psp unknown")
  void testOrganization_getAllPublishedFlow_KO_FDR0708() {
    String pspUnknown = "PSP_UNKNOWN";
    String url = GET_ALL_PUBLISHED_FLOW_URL.formatted(EC_CODE, pspUnknown);
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

  @Test
  @DisplayName("ORGANIZATIONS - KO FDR-0709 - psp not enabled")
  void testOrganization_getAllPublishedFlow_KO_FDR0709() {
    String url = GET_ALL_PUBLISHED_FLOW_URL.formatted(EC_CODE, PSP_CODE_NOT_ENABLED);

    ErrorResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(400)
            .extract()
            .as(ErrorResponse.class);
    assertThat(res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.PSP_NOT_ENABLED.errorCode()));
    assertThat(
        res.getErrors(),
        hasItem(
            hasProperty(
                "message", equalTo("Psp [%s] not enabled".formatted(PSP_CODE_NOT_ENABLED)))));
  }

  /** ################# getReportingFlow ############### */
  @Test
  @DisplayName("ORGANIZATIONS - OK - recupero di un reporting flow")
  void testOrganization_getReportingFlow_Ok() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName);
    String url = GET_REPORTING_FLOW_URL.formatted(EC_CODE, flowName, 1, PSP_CODE);
    GetResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(GetResponse.class);
    assertThat(res.getFdr(), equalTo(flowName));
    assertThat(res.getReceiver().getOrganizationId(), equalTo(EC_CODE));
    assertThat(res.getSender().getPspId(), equalTo(PSP_CODE));
    assertThat(res.getStatus(), equalTo(ReportingFlowStatusEnum.PUBLISHED));
    assertThat(res.getComputedTotPayments(), equalTo(5L));
  }

  @Test
  @DisplayName("ORGANIZATIONS - OK - recupero di un reporting flow pubblicato alla revision 2")
  void testOrganization_getReportingFlow_revision_2_OK() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName);
    TestUtil.pspSunnyDay(flowName);

    String url = GET_REPORTING_FLOW_URL.formatted(EC_CODE, flowName, 2, PSP_CODE);
    GetResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(GetResponse.class);
    assertThat(res.getFdr(), equalTo(flowName));
    assertThat(res.getRevision(), equalTo(2L));
    assertThat(res.getStatus(), equalTo(ReportingFlowStatusEnum.PUBLISHED));
  }

  @Test
  @DisplayName("ORGANIZATIONS - KO FDR-0701 - getReportingFlow reporting flow not found")
  void testOrganization_getReportingFlow_KO_FDR0701() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName);

    String flowNameWrong = TestUtil.getDynamicFlowName();
    String url = GET_REPORTING_FLOW_URL.formatted(EC_CODE, flowNameWrong, 1, PSP_CODE);

    ErrorResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(404)
            .extract()
            .as(ErrorResponse.class);
    assertThat(
        res.getAppErrorCode(),
        equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND.errorCode()));
    assertThat(
        res.getErrors(),
        hasItem(
            hasProperty("message", equalTo(String.format("Fdr [%s] not found", flowNameWrong)))));
  }

  /** ################# getReportingFlowPayments ############### */
  @Test
  @DisplayName("ORGANIZATIONS - OK - recupero dei payments di un flow pubblicato")
  void testOrganization_getReportingFlowPayments_Ok() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName);

    String url = GET_REPORTING_FLOW_PAYMENTS_URL.formatted(EC_CODE, flowName, 1, PSP_CODE);
    GetPaymentResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(GetPaymentResponse.class);
    assertThat(res.getCount(), equalTo(5L));
    List<String> expectedList =
        List.of(
            PaymentStatusEnum.EXECUTED.name(),
            PaymentStatusEnum.REVOKED.name(),
            PaymentStatusEnum.NO_RPT.name(),
            PaymentStatusEnum.STAND_IN.name(),
            PaymentStatusEnum.STAND_IN_NO_RPT.name());
    assertThat(
        res.getData().stream().map(o -> o.getPayStatus().name()).toList(), equalTo(expectedList));
    assertThat(
        res.getData().stream().map(o -> o.getPayStatus().name()).toList(),
        containsInAnyOrder(expectedList.toArray()));
  }

  /** ################# changeReadFlag ############### */
  //  @Test
  //  @DisplayName("ORGANIZATIONS - OK - changeReadFlag")
  //  void testOrganization_changeReadFlag_Ok() {
  //    String flowName = TestUtil.getDynamicFlowName();
  //    TestUtil.pspSunnyDay(flowName);
  //
  //    String url = CHANGE_READ_FLAG_URL.formatted(flowName, 1, PSP_CODE);
  //
  //    GenericResponse res =
  //        given()
  //            .header(HEADER)
  //            .when()
  //            .put(url)
  //            .then()
  //            .statusCode(200)
  //            .extract()
  //            .as(GenericResponse.class);
  //    assertThat(res.getMessage(), equalTo(String.format("Fdr [%s] internal read", flowName)));
  //  }
  //
  //  @Test
  //  @DisplayName("ORGANIZATIONS - KO FDR-0701 - changeReadFlag reporting flow not found")
  //  void testOrganization_changeReadFlag_KO_FDR0701() {
  //    String flowName = TestUtil.getDynamicFlowName();
  //    TestUtil.pspSunnyDay(flowName);
  //    String flowNameWrong = TestUtil.getDynamicFlowName();
  //
  //    String url = CHANGE_READ_FLAG_URL.formatted(flowNameWrong, 1, PSP_CODE);
  //
  //    ErrorResponse res =
  //        given()
  //            .header(HEADER)
  //            .when()
  //            .put(url)
  //            .then()
  //            .statusCode(404)
  //            .extract()
  //            .as(ErrorResponse.class);
  //    assertThat(
  //        res.getAppErrorCode(),
  //        equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND.errorCode()));
  //    assertThat(
  //        res.getErrors(),
  //        hasItem(
  //            hasProperty(
  //                "message",
  //                equalTo(String.format("Fdr [%s] not found", flowNameWrong)))));
  //  }
}
