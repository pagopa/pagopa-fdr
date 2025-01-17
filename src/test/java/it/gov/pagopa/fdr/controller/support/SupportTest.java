package it.gov.pagopa.fdr.controller.support;

import static io.restassured.RestAssured.given;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.HEADER;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.PSP_CODE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;

import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsBySenderAndReceiverResponse;
import it.gov.pagopa.fdr.test.util.AzuriteResource;
import it.gov.pagopa.fdr.test.util.MongoResource;
import it.gov.pagopa.fdr.test.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(MockServerTestResource.class)
@QuarkusTestResource(MongoResource.class)
@QuarkusTestResource(AzuriteResource.class)
class SupportTest {

  private static final String GET_ALL_FDR_BY_PSP_ID_IUV = "/internal/psps/" + "%s" + "/iuv/" + "%s";
  private static final String GET_ALL_FDR_BY_PSP_ID_IUR = "/internal/psps/" + "%s" + "/iur/" + "%s";

  private static String flowName;

  /** ############### getFdrByPspIdIuv ################ */
  @Test
  @DisplayName("SUPPORT - OK - getFdrByPspIdIuv")
  void testSupport_getFdrByPspIdIuv_Ok() {
    flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName);
    String url = GET_ALL_FDR_BY_PSP_ID_IUV.formatted(PSP_CODE, "a");
    PaginatedFlowsBySenderAndReceiverResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(PaginatedFlowsBySenderAndReceiverResponse.class);
    assertThat(res.getCount(), greaterThan(0L));
    assertThat(
        res.getData(),
        hasItem(
            anyOf(
                hasProperty("name", equalTo(flowName)), hasProperty("pspId", equalTo(PSP_CODE)))));
  }

  @Test
  @DisplayName("SUPPORT - OK - getFdrByPspIdIuv - NO RESULTS")
  void testSupport_getFdrByPspIdIuv_Ok_No_Results() {
    String url = GET_ALL_FDR_BY_PSP_ID_IUV.formatted(PSP_CODE, "NO_RESULTS_IUV");
    PaginatedFlowsBySenderAndReceiverResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(PaginatedFlowsBySenderAndReceiverResponse.class);
    assertThat(res.getCount(), equalTo(0L));
  }

  /** ############### getFdrByPspIdIur ################ */
  @Test
  @DisplayName("SUPPORT - OK - getFdrByPspIdIur")
  void testSupport_getFdrByPspIdIur_Ok() {
    flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName);
    String url = GET_ALL_FDR_BY_PSP_ID_IUR.formatted(PSP_CODE, "abcdefg");
    PaginatedFlowsBySenderAndReceiverResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(PaginatedFlowsBySenderAndReceiverResponse.class);
    assertThat(res.getCount(), greaterThan(0L));
    assertThat(
        res.getData(),
        hasItem(
            anyOf(
                hasProperty("name", equalTo(flowName)), hasProperty("pspId", equalTo(PSP_CODE)))));
  }

  @Test
  @DisplayName("SUPPORT - OK - getFdrByPspIdIur - NO RESULTS")
  void testSupport_getFdrByPspIdIur_Ok_No_Results() {
    String url = GET_ALL_FDR_BY_PSP_ID_IUR.formatted(PSP_CODE, "NO_RESULTS_IUR");
    PaginatedFlowsBySenderAndReceiverResponse res =
        given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(PaginatedFlowsBySenderAndReceiverResponse.class);
    assertThat(res.getCount(), equalTo(0L));
  }
}
