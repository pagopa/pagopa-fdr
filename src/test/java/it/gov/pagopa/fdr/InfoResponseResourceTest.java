package it.gov.pagopa.fdr;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class InfoResponseResourceTest {

  @Test
  public void testHelloEndpoint() {
    given().when().get("/info").then().statusCode(200).body("name", containsString("app"));
  }
}
