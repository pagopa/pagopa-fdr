package it.gov.pagopa.fdr;

import io.quarkus.test.junit.QuarkusTest;

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
  //    @Test
  //    public void testAdd() {
  //        given()
  //                .body("{\"name\": \"Pear\", \"description\": \"Winter fruit\"}")
  //                .header("Content-Type", MediaType.APPLICATION_JSON)
  //                .when()
  //                .post("/fruits")
  //                .then()
  //                .statusCode(200)
  //                .body("$.size()", is(3),
  //                        "name", containsInAnyOrder("Apple", "Pineapple", "Pear"),
  //                        "description", containsInAnyOrder("Winter fruit", "Tropical fruit",
  // "Winter fruit"));
  //
  //        given()
  //                .body("{\"name\": \"Pear\", \"description\": \"Winter fruit\"}")
  //                .header("Content-Type", MediaType.APPLICATION_JSON)
  //                .when()
  //                .delete("/fruits")
  //                .then()
  //                .statusCode(200)
  //                .body("$.size()", is(2),
  //                        "name", containsInAnyOrder("Apple", "Pineapple"),
  //                        "description", containsInAnyOrder("Winter fruit", "Tropical fruit"));
  //    }
}
