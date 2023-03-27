package it.gov.pagopa.fdr;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

import io.quarkus.test.junit.QuarkusTest;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class FruitResourceTest {

  //    @Test
  //    public void testList() {
  //        given()
  //                .when().get("/fruits")
  //                .then()
  //                .statusCode(200)
  //                .body("$.size()", is(2),
  //                        "name", containsInAnyOrder("Apple", "Pineapple"),
  //                        "description", containsInAnyOrder("Winter fruit", "Tropical fruit"));
  //    }
  //
  @Test
  public void testAdd() {
    given()
        .body("{\"name\": \"Pear\", \"description\": \"Winter fruit\"}")
        .header("Content-Type", MediaType.APPLICATION_JSON)
        .when()
        .post("/fruits")
        .then()
        .statusCode(400);
  }
}
