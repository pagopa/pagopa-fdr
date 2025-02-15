package it.gov.pagopa.fdr.controller.info;

import static io.restassured.RestAssured.given;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.APP_NAME;
import static org.hamcrest.CoreMatchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
class InfoControllerTest {

  @Test
  @DisplayName("InfoController endpoint")
  void testInfoEndpoint2() {
    given().when().get("/info").then().statusCode(200).body("name", equalTo(APP_NAME));
  }
}
