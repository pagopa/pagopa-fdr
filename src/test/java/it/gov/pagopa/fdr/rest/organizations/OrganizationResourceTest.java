package it.gov.pagopa.fdr.rest.organizations;

import static io.restassured.RestAssured.given;
import static it.gov.pagopa.fdr.ConstantsTest.pspCode;
import static it.gov.pagopa.fdr.ConstantsTest.reportingFlowName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.Header;
import it.gov.pagopa.fdr.rest.BaseResourceTest;
import it.gov.pagopa.fdr.rest.organizations.response.GetAllResponse;
import it.gov.pagopa.fdr.util.MongoResource;
import it.gov.pagopa.fdr.util.TestUtil;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(MockServerTestResource.class)
@QuarkusTestResource(MongoResource.class)
public class OrganizationResourceTest extends BaseResourceTest {

  @Inject TestUtil testUtil;

  private static final String pspCodeNotEnabled = "pspNotEnabled";
  private static final String brokerCode = "intTest";
  private static final String channelCode = "canaleTest";
  private static final String ecCode = "12345678900";
  private static final String ecCodeNotEnabled = "00987654321";
  private static final Header header = new Header("Content-Type", "application/json");

  private static final String organizationFindByIdEcUrl = "/organizations/%s/flows?idPsp=%s";
  private static final String organizationfindByReportingFlowNameUrl =
      "/organizations/%s/flows/%s/psps/%s";

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

  /** ############### findByIdEc ################ */
  @Test
  @DisplayName("ORGANIZATIONS findByIdEc Ok")
  public void testOrganization_findByIdEc_Ok() throws JsonProcessingException {
    String flowName = getFlowName();
    pspSunnyDay(flowName);
    String url = organizationFindByIdEcUrl.formatted(ecCode, pspCode);
    String responseFmt = responseAllPublishedFlows.formatted(flowName, pspCode);
    GetAllResponse res = given()
        .header(header)
        .when()
        .get(url)
        .then()
        .statusCode(200)
        .extract()
        .as(GetAllResponse.class);
    assertThat(testUtil.prettyPrint(res), equalTo(testUtil.prettyPrint(responseFmt, GetAllResponse.class)));
//        .body(containsString(responseFmt.replaceAll("[\r\n]+"," ").trim()));
  }

  @Test
  @DisplayName("ORGANIZATIONS findByIdEc no results")
  public void testOrganization_findByIdEc_OkNoResults() throws JsonProcessingException {
    String url = organizationFindByIdEcUrl.formatted(ecCode, pspCode, 10, 10);
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
//    given()
//        .header(header)
//        .when()
//        .get(url)
//        .then()
//        .statusCode(200)
//        .body(containsString(mapper.writeValueAsString(reportingFlowByIdEcNoResults)));
  }

  @Test
  @DisplayName("ORGANIZATIONS findByIdEc KO FDR-0708")
  public void testOrganization_findByIdEc_KO_FDR0708() {
    String pspUnknown = "PSP_UNKNOWN";
    String url = organizationFindByIdEcUrl.formatted(ecCode, pspUnknown, 10, 10);
    String responseFmt =
        """
        {"httpStatusCode":400,"httpStatusDescription":"Bad Request","appErrorCode":"FDR-0708","errors":[{"message":"Psp [PSP_UNKNOWN] unknown"}]}""";
    given().header(header).when().get(url).then().statusCode(400).body(containsString(responseFmt));
  }

  @Test
  @DisplayName("ORGANIZATIONS findByIdEc KO FDR-0709")
  public void testOrganization_findByIdEc_KO_FDR0709() {
    String url = organizationFindByIdEcUrl.formatted(ecCode, pspCodeNotEnabled, 10, 10);
    String responseFmt =
        """
        {"httpStatusCode":400,"httpStatusDescription":"Bad Request","appErrorCode":"FDR-0709","errors":[{"message":"Psp [pspNotEnabled] not enabled"}]}""";

    given().header(header).when().get(url).then().statusCode(400).body(containsString(responseFmt));
  }

  @Test
  @DisplayName("ORGANIZATIONS findByIdEc KO FDR-0716")
  public void testOrganization_findByIdEc_KO_FDR0716() {
    String ecUnknown = "EC_UNKNOWN";
    String url = organizationFindByIdEcUrl.formatted(ecUnknown, pspCode, 10, 10);
    String responseFmt =
        """
        {"httpStatusCode":400,"httpStatusDescription":"Bad Request","appErrorCode":"FDR-0716","errors":[{"message":"Creditor institution [EC_UNKNOWN] unknown"}]}""";

    given().header(header).when().get(url).then().statusCode(400).body(containsString(responseFmt));
  }

  @Test
  @DisplayName("ORGANIZATIONS findByIdEc KO FDR-0717")
  public void testOrganization_findByIdEc_KO_FDR0717() {
    String url = organizationFindByIdEcUrl.formatted(ecCodeNotEnabled, pspCode, 10, 10);
    String responseFmt =
        """
        {"httpStatusCode":400,"httpStatusDescription":"Bad Request","appErrorCode":"FDR-0717","errors":[{"message":"Creditor institution [%s] not enabled"}]}"""
            .formatted(ecCodeNotEnabled);

    given().header(header).when().get(url).then().statusCode(400).body(containsString(responseFmt));
  }

  /** ################# findByReportingFlowName ############### */
  @Test
  @DisplayName("ORGANIZATIONS findByReportingFlowName Ok")
  public void testOrganization_findByReportingFlowName_Ok() throws JsonProcessingException {
    String url =
        organizationfindByReportingFlowNameUrl.formatted(ecCode, reportingFlowName, pspCode);

//    given()
//        .header(header)
//        .when()
//        .get(url)
//        .then()
//        .statusCode(200)
//        .body(containsString(mapper.writeValueAsString(reportingFlowGet)));
  }

}
