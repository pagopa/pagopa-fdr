package it.gov.pagopa.fdr;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class InfoResponseResourceTest {

  @Test
  public void testInfoEndpoint() {
    given().when().get("/api/info").then().statusCode(200).body("name", equalTo("pagopa-fdr"));
  }
}
