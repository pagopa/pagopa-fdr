package it.gov.pagopa.fdr;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class InfoResponseResourceTest {

  @ConfigProperty(name = "app.name", defaultValue = "app")
  String name;

  @Test
  public void testInfoEndpoint() {
    given().when().get("/info").then().statusCode(200).body("name", equalTo(name));
  }
}
