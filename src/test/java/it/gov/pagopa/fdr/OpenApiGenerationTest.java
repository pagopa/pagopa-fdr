package it.gov.pagopa.fdr;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;

@QuarkusTest
class OpenApiGenerationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void swaggerSpringPlugin() throws Exception {

        String responseString =
                given()
                        .when().get("/openapi.json")
                        .then()
                        .statusCode(200)
                        .contentType("application/json")
                        .extract()
                        .asString();

        Object swagger = objectMapper.readValue(responseString, Object.class);
        String formatted = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(swagger);
        formatted = formatted.replace("placeholder-for-replace", getAppErrorCodes());
        Path basePath = Paths.get("openapi/");
        Files.createDirectories(basePath);
        Files.write(basePath.resolve("openapi.json"), formatted.getBytes());
    }

    private String getAppErrorCodes() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("### APP ERROR CODES ### \\n\\n\\n <details><summary>Details</summary>\\n **NAME** | **HTTP STATUS CODE** | **DESCRIPTION** \\n- | - | - ");
        for (AppErrorCodeMessageEnum errorCode : AppErrorCodeMessageEnum.values()) {
            stringBuilder
                    .append("\\n **")
                    .append(errorCode.errorCode())
                    .append("** | *")
                    .append(errorCode.httpStatus())
                    .append("* | ")
                    .append(errorCode.openAPIDescription());
        }
        stringBuilder.append(" \\n\\n </details> \\n");
        return stringBuilder.toString();
    }
}