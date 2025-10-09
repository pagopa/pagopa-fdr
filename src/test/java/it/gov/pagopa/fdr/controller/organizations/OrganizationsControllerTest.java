package it.gov.pagopa.fdr.controller.organizations;

import static io.restassured.RestAssured.given;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.*;
import static it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum.PSP_UNKNOWN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.controller.model.error.ErrorResponse;
import it.gov.pagopa.fdr.controller.model.flow.enums.ReportingFlowStatusEnum;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowResponse;
import it.gov.pagopa.fdr.controller.model.payment.Payment;
import it.gov.pagopa.fdr.controller.model.payment.enums.PaymentStatusEnum;
import it.gov.pagopa.fdr.controller.model.payment.response.PaginatedPaymentsResponse;
import it.gov.pagopa.fdr.test.util.AzuriteResource;
import it.gov.pagopa.fdr.test.util.PostgresResource;
import it.gov.pagopa.fdr.test.util.TestUtil;
import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(MockServerTestResource.class)
@QuarkusTestResource(PostgresResource.class)
@QuarkusTestResource(AzuriteResource.class)
class OrganizationsControllerTest {

  /** ############### getAllPublishedFlow ################ */
  @Test
  @DisplayName("ORGANIZATIONS - OK - getAllPublishedFlow")
  void testOrganization_getAllPublishedFlow_Ok() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName, FLOW_DATE);
    String url = ORGANIZATIONS_GET_ALL_PUBLISHED_FLOW_URL.formatted(EC_CODE, PSP_CODE);
    PaginatedFlowsResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(PaginatedFlowsResponse.class);
    assertThat(res.getCount(), greaterThan(0L));
    assertThat(
        res.getData(),
        hasItem(
            anyOf(
                hasProperty("name", equalTo(flowName)), hasProperty("pspId", equalTo(PSP_CODE)))));
  }

  @Test
  @DisplayName("ORGANIZATIONS - OK - getAllPublishedFlow with published date filter")
  void testOrganization_getAllPublishedFlow_with_published_date_filter_Ok() {
    Instant now = Instant.now();
    ZonedDateTime nowUtc = now.atZone(ZoneOffset.UTC);
    ZonedDateTime limitZoned = nowUtc.minusDays(25);
    Instant lastMonthDate = limitZoned.toInstant();
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName, FLOW_DATE);
    String url =
        ORGANIZATIONS_GET_ALL_PUBLISHED_FLOW_URL_WITH_PUBLISHED_FILTER.formatted(
            EC_CODE, PSP_CODE, lastMonthDate);
    PaginatedFlowsResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(PaginatedFlowsResponse.class);
    assertThat(res.getCount(), greaterThan(0L));
    assertThat(
        res.getData(),
        hasItem(
            anyOf(
                hasProperty("name", equalTo(flowName)),
                hasProperty("pspId", equalTo(PSP_CODE)),
                hasProperty("published", greaterThan(PUBLISHED_DATE)))));
  }

  @Test
  @DisplayName("ORGANIZATIONS - OK - getAllPublishedFlow with flow date filter")
  void testOrganization_getAllPublishedFlow_with_flow_date_filter_Ok() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName, FLOW_DATE);
    String url = ORGANIZATIONS_GET_ALL_PUBLISHED_FLOW_URL_WITH_FLOW_DATE_FILTER.formatted(EC_CODE, PSP_CODE, FLOW_DATE_PARAMETER);
    PaginatedFlowsResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(PaginatedFlowsResponse.class);
    assertThat(res.getCount(), equalTo(0L));
  }

  @Test
  @DisplayName("ORGANIZATIONS - OK - getAllPublishedFlow with wrong flow date filter")
  void testOrganization_getAllPublishedFlow_with_flow_date_filter_Ko() {
    String flowName = TestUtil.getDynamicFlowName();

    TestUtil.pspSunnyDay(flowName);

    String url =
        ORGANIZATIONS_GET_ALL_PUBLISHED_FLOW_URL_WITH_FLOW_DATE_FILTER.formatted(
            EC_CODE,
            PSP_CODE,
            Instant.now().atZone(ZoneOffset.UTC).minus(2, ChronoUnit.MONTHS).toString());

    given().header(HEADER).when().get(url).then().statusCode(400).extract().as(ErrorResponse.class);
  }

  @Test
  @DisplayName("ORGANIZATIONS - OK - getAllPublishedFlow no results")
  void testOrganization_getAllPublishedFlow_OkNoResults() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName, FLOW_DATE);
    String url = ORGANIZATIONS_GET_ALL_PUBLISHED_FLOW_URL.formatted(EC_CODE, PSP_CODE_2);
    PaginatedFlowsResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(PaginatedFlowsResponse.class);
    assertThat(res.getCount(), equalTo(0L));
  }

  @Test
  @DisplayName("ORGANIZATIONS - OK - getAllPublishedFlow with published date filter no results")
  void testOrganization_getAllPublishedFlow_with_published_date_filter_OkNoResults() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName, FLOW_DATE);
    String url =
        ORGANIZATIONS_GET_ALL_PUBLISHED_FLOW_URL_WITH_PUBLISHED_FILTER.formatted(
            EC_CODE, PSP_CODE, PUBLISHED_DATE_FUTURE);
    PaginatedFlowsResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(PaginatedFlowsResponse.class);
    assertThat(res.getCount(), equalTo(0L));
  }

  @Test
  @DisplayName("ORGANIZATIONS - OK - getAllPublishedFlow with flow date filter no results")
  void testOrganization_getAllPublishedFlow_with_flow_date_filter_OkNoResults() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName, FLOW_DATE);
    String url =
        ORGANIZATIONS_GET_ALL_PUBLISHED_FLOW_URL_WITH_FLOW_DATE_FILTER.formatted(
            EC_CODE, PSP_CODE, FLOW_DATE_FUTURE);
    PaginatedFlowsResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(PaginatedFlowsResponse.class);
    assertThat(res.getCount(), equalTo(0L));
  }

  @Test
  @DisplayName("ORGANIZATIONS - KO FDR-0708 - psp unknown")
  void testOrganization_getAllPublishedFlow_KO_FDR0708() {
    String pspUnknown = "PSP_UNKNOWN";
    String url = ORGANIZATIONS_GET_ALL_PUBLISHED_FLOW_URL.formatted(EC_CODE, pspUnknown);
    ErrorResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(400)
            .extract()
            .as(ErrorResponse.class);
    assertThat(res.getHttpStatusDescription(), equalTo("Bad Request"));
    assertThat(res.getAppErrorCode(), equalTo(PSP_UNKNOWN.errorCode()));
    assertThat(res.getErrors(), hasSize(1));
    assertThat(
        res.getErrors(),
        hasItem(
            hasProperty(
                "message",
                equalTo(String.format("PSP with ID [%s] is invalid or unknown.", pspUnknown)))));
  }

  @Test
  @DisplayName("ORGANIZATIONS - KO FDR-0709 - psp not enabled")
  void testOrganization_getAllPublishedFlow_KO_FDR0709() {
    String url = ORGANIZATIONS_GET_ALL_PUBLISHED_FLOW_URL.formatted(EC_CODE, PSP_CODE_NOT_ENABLED);

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
                "message",
                equalTo("PSP with ID [%s] is not enabled.".formatted(PSP_CODE_NOT_ENABLED)))));
  }

  @Test
  @DisplayName("ORGANIZATIONS - KO FDR-0716 - creditor institution unknown")
  void testOrganization_getAllPublishedFlow_KO_FDR0716() {
    String ecUnknown = "EC_UNKNOWN";
    String url = ORGANIZATIONS_GET_ALL_PUBLISHED_FLOW_URL.formatted(ecUnknown, PSP_CODE);

    ErrorResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(400)
            .extract()
            .as(ErrorResponse.class);
    assertThat(res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.EC_UNKNOWN.errorCode()));
    assertThat(
        res.getErrors(),
        hasItem(
            hasProperty(
                "message",
                equalTo(
                    "Creditor institution with ID [%s] is invalid or unknown."
                        .formatted(ecUnknown)))));
  }

  @Test
  @DisplayName("ORGANIZATIONS - KO FDR-0717 - creditor institution not enabled")
  void testOrganization_getAllPublishedFlow_KO_FDR0717() {
    String url = ORGANIZATIONS_GET_ALL_PUBLISHED_FLOW_URL.formatted(EC_CODE_NOT_ENABLED, PSP_CODE);

    ErrorResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(400)
            .extract()
            .as(ErrorResponse.class);
    assertThat(res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.EC_NOT_ENABLED.errorCode()));
    assertThat(
        res.getErrors(),
        hasItem(
            hasProperty(
                "message",
                equalTo(
                    "Creditor institution with ID [%s] is not enabled."
                        .formatted(EC_CODE_NOT_ENABLED)))));
  }

  /** ################# getReportingFlow ############### */
  @Test
  @DisplayName("ORGANIZATIONS - OK - reporting flow retrieval")
  void testOrganization_getReportingFlow_Ok() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName, FLOW_DATE);
    String url = ORGANIZATIONS_GET_REPORTING_FLOW_URL.formatted(EC_CODE, flowName, 1L, PSP_CODE);
    SingleFlowResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(SingleFlowResponse.class);
    assertThat(res.getFdr(), equalTo(flowName));
    assertThat(res.getReceiver().getOrganizationId(), equalTo(EC_CODE));
    assertThat(res.getSender().getPspId(), equalTo(PSP_CODE));
    assertThat(res.getStatus(), equalTo(ReportingFlowStatusEnum.PUBLISHED));
    assertThat(res.getComputedTotPayments(), equalTo(5L));
  }

  @Test
  @DisplayName("ORGANIZATIONS - OK -  retrieval of a published revision 2 reporting flow")
  void testOrganization_getReportingFlow_revision_2_OK() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName, FLOW_DATE);
    TestUtil.pspSunnyDay(flowName, FLOW_DATE_FUTURE);

    String url = ORGANIZATIONS_GET_REPORTING_FLOW_URL.formatted(EC_CODE, flowName, 2L, PSP_CODE);
    SingleFlowResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(SingleFlowResponse.class);
    assertThat(res.getFdr(), equalTo(flowName));
    assertThat(res.getRevision(), equalTo(2L));
    assertThat(res.getStatus(), equalTo(ReportingFlowStatusEnum.PUBLISHED));
  }

  @Test
  @DisplayName("ORGANIZATIONS - KO FDR-0701 - getReportingFlow reporting flow not found")
  void testOrganization_getReportingFlow_KO_FDR0701() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName, FLOW_DATE);

    String flowNameWrong = TestUtil.getDynamicFlowName();
    String url =
        ORGANIZATIONS_GET_REPORTING_FLOW_URL.formatted(EC_CODE, flowNameWrong, 1L, PSP_CODE);

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
            hasProperty(
                "message", equalTo(String.format("Flow with ID [%s] not found.", flowNameWrong)))));
  }

  /** ################# getReportingFlowPayments ############### */
  @Test
  @DisplayName("ORGANIZATIONS - OK -  payments retrieval from a published flow")
  void testOrganization_getReportingFlowPayments_Ok() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName, FLOW_DATE);

    String url =
        ORGANIZATIONS_GET_REPORTING_FLOW_PAYMENTS_URL.formatted(EC_CODE, flowName, 1L, PSP_CODE);

    PaginatedPaymentsResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(PaginatedPaymentsResponse.class);
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

  @Test
  @DisplayName(
      "ORGANIZATIONS - OK - payments retrieval from a published flow using custom pagination")
  void testOrganization_getReportingFlowPayments_pagination_Ok() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName, FLOW_DATE);

    String url =
        (ORGANIZATIONS_GET_REPORTING_FLOW_PAYMENTS_URL + "?page=2&size=1")
            .formatted(EC_CODE, flowName, 1L, PSP_CODE);
    PaginatedPaymentsResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(PaginatedPaymentsResponse.class);
    List<Payment> data = res.getData();

    assertThat(res.getMetadata().getPageSize(), equalTo(1));
    assertThat(res.getMetadata().getPageNumber(), equalTo(2));
    assertThat(res.getCount(), equalTo(5L));
    assertThat(
        data.stream().map(o -> o.getPayStatus().name()).toList(),
        equalTo(List.of(PaymentStatusEnum.REVOKED.name())));
    assertThat(data.stream().map(Payment::getIndex).toList(), equalTo(List.of(101L)));
  }
}
