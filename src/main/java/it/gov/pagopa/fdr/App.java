package it.gov.pagopa.fdr;

import it.gov.pagopa.fdr.rest.model.ErrorResponse;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@OpenAPIDefinition(
    components =
        @Components(
            responses = {
              @APIResponse(
                  name = "InternalServerError",
                  responseCode = "500",
                  description = "Internal Server Error",
                  content =
                      @Content(
                          mediaType = MediaType.APPLICATION_JSON,
                          schema = @Schema(implementation = ErrorResponse.class),
                          example =
                              "{\n"
                                  + "    \"errorId\": \"2f4dfc8f-4512-4549-ba9c-0c2156144a1e\",\n"
                                  + "    \"httpStatusCode\": 500,\n"
                                  + "    \"httpStatusDescription\": \"Internal Server Error\",\n"
                                  + "    \"appErrorCode\": \"FDR-0500\",\n"
                                  + "    \"errors\": [\n"
                                  + "        {\n"
                                  + "            \"message\": \"An unexpected error has occurred."
                                  + " Please contact support.\"\n"
                                  + "        }\n"
                                  + "    ]\n"
                                  + "}")),
              @APIResponse(
                  name = "BadRequest",
                  responseCode = "400",
                  description = "Bad Request",
                  content =
                      @Content(
                          mediaType = MediaType.APPLICATION_JSON,
                          schema = @Schema(implementation = ErrorResponse.class)))
            }),
    info = @Info(title = "FDR - Flussi di Rendicontazione", version = "0.0.0-SNAPSHOT"))
public class App extends Application {}
