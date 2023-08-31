package it.gov.pagopa.fdr.rest.support;

import static io.restassured.RestAssured.given;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.EC_CODE;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.HEADER;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.PSP_CODE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.rest.model.FdrByPspIdIuvIurBase;
import it.gov.pagopa.fdr.rest.support.response.FdrByPspIdIuvIurResponse;
import it.gov.pagopa.fdr.test.util.AzuriteResource;
import it.gov.pagopa.fdr.test.util.MongoResource;
import it.gov.pagopa.fdr.test.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@QuarkusTestResource(MockServerTestResource.class)
@QuarkusTestResource(MongoResource.class)
@QuarkusTestResource(AzuriteResource.class)
class SupportTest {
  @Nested
  @TestMethodOrder(OrderAnnotation.class)
  class getByPspIdIuvIuv {
    private static final String GET_ALL_FDR_BY_PSP_ID_IUV=
        "/internal/psps/"+ "%s"+"/iuv/"+"%s";
    private static final String GET_ALL_FDR_BY_PSP_ID_IUR=
        "/internal/psps/"+ "%s"+"/iur/"+"%s";

    private static String flowName;

    /** ############### getFdrByPspIdIuv ################ */
    @Test
    @Order(1)
    @DisplayName("SUPPORT - OK - getFdrByPspIdIuv")
    void testSupport_getFdrByPspIdIuv_Ok() {
      flowName = TestUtil.getDynamicFlowName();
      TestUtil.pspSunnyDay(flowName);
      String url = GET_ALL_FDR_BY_PSP_ID_IUV.formatted(PSP_CODE, "a");
      FdrByPspIdIuvIurResponse res =
          given()
              .header(HEADER)
              .when()
              .get(url)
              .then()
              .statusCode(200)
              .extract()
              .as(FdrByPspIdIuvIurResponse.class);
      assertThat(res.getCount(), greaterThan(0L));

      FdrByPspIdIuvIurBase data = res.getData().get(0);

      assertThat(data.getPspId(), equalTo(PSP_CODE));
      assertThat(data.getOrganizationId(), equalTo(EC_CODE));
      assertThat(data.getFdr(), equalTo(flowName));
      assertThat(data.getRevision(), equalTo(1L));
      assertThat(data.getCreated(), notNullValue());
    }
    @Test
    @Order(2)
    @DisplayName("SUPPORT - OK - getFdrByPspIdIuv - NO RESULTS")
    void testSupport_getFdrByPspIdIuv_Ok_No_Results() {
      String url = GET_ALL_FDR_BY_PSP_ID_IUV.formatted(PSP_CODE, "NO_RESULTS_IUV");
      FdrByPspIdIuvIurResponse res =
          given()
              .header(HEADER)
              .when()
              .get(url)
              .then()
              .statusCode(200)
              .extract()
              .as(FdrByPspIdIuvIurResponse.class);
      assertThat(res.getCount(), equalTo(0L));
    }

    /** ############### getFdrByPspIdIur ################ */
    @Test
    @Order(3)
    @DisplayName("SUPPORT - OK - getFdrByPspIdIur")
    void testSupport_getFdrByPspIdIur_Ok() {
      String url = GET_ALL_FDR_BY_PSP_ID_IUR.formatted(PSP_CODE, "abcdefg");
      FdrByPspIdIuvIurResponse res =
          given()
              .header(HEADER)
              .when()
              .get(url)
              .then()
              .statusCode(200)
              .extract()
              .as(FdrByPspIdIuvIurResponse.class);
      assertThat(res.getCount(), greaterThan(0L));

      FdrByPspIdIuvIurBase data = res.getData().get(0);

      assertThat(data.getPspId(), equalTo(PSP_CODE));
      assertThat(data.getOrganizationId(), equalTo(EC_CODE));
      assertThat(data.getFdr(), equalTo(flowName));
      assertThat(data.getRevision(), equalTo(1L));
      assertThat(data.getCreated(), notNullValue());
    }
    @Test
    @Order(4)
    @DisplayName("SUPPORT - OK - getFdrByPspIdIur - NO RESULTS")
    void testSupport_getFdrByPspIdIur_Ok_No_Results() {
      String url = GET_ALL_FDR_BY_PSP_ID_IUR.formatted(PSP_CODE, "NO_RESULTS_IUR");
      FdrByPspIdIuvIurResponse res =
          given()
              .header(HEADER)
              .when()
              .get(url)
              .then()
              .statusCode(200)
              .extract()
              .as(FdrByPspIdIuvIurResponse.class);
      assertThat(res.getCount(), equalTo(0L));
    }
  }
}