package it.gov.pagopa.fdr;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
@QuarkusTest
public class InfoResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/info")
          .then()
             .statusCode(200)
             .body("name", containsString("FDR"));
    }

}