package it.gov.pagopa.fdr.rest.info;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class InfoResourceTest {

  String name = "pagopafdr";

  @Test
  public void testInfoEndpoint2() {
    given().when().get("/info").then().statusCode(200).body("name", equalTo(name));
  }
}
