package it.gov.pagopa.fdr.controller.internal;

import static io.restassured.RestAssured.given;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.APP_NAME;
import static org.hamcrest.CoreMatchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
class InternalInfoControllerTest {

    @Test
    @DisplayName("InternalInfoController endpoint")
    void testInfoEndpoint2() {
        given().when().get("/internal/info").then().statusCode(200).body("name", equalTo(APP_NAME));
    }
}
