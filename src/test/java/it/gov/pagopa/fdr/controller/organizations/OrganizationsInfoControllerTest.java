package it.gov.pagopa.fdr.controller.organizations;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.APP_NAME;
import static org.hamcrest.CoreMatchers.equalTo;

@QuarkusTest
class OrganizationsInfoControllerTest {

    @Test
    @DisplayName("OrganizationsInfoController endpoint")
    void testInfoEndpoint2() {
        given().when().get("/organizations/info").then().statusCode(200).body("name", equalTo(APP_NAME));
    }
}
