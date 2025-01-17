package it.gov.pagopa.fdr;


import it.gov.pagopa.fdr.controller.model.error.ErrorResponse;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

@SecurityScheme(
        type = SecuritySchemeType.APIKEY,
        securitySchemeName = "api_key",
        apiKeyName = "Ocp-Apim-Subscription-Key",
        in = SecuritySchemeIn.HEADER)
@OpenAPIDefinition(
        security = {@SecurityRequirement(name = "api_key")},
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
                                                """
                                                        {
                                                          "errorId": "50905466-1881-457b-b42f-fb7b2bfb1610",
                                                          "httpStatusCode": 500,
                                                          "httpStatusDescription": "Internal Server Error",
                                                          "appErrorCode": "FDR-0500",
                                                          "errors": [
                                                            {
                                                              "message": "An unexpected error has occurred. Please contact support."
                                                            }
                                                          ]
                                                        }""")),
                        @APIResponse(
                                name = "AppException400",
                                responseCode = "400",
                                description = "Default app exception for status 400",
                                content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON,
                                        schema = @Schema(implementation = ErrorResponse.class),
                                        examples = {
                                                @ExampleObject(
                                                        name = "Error",
                                                        value =
                                                                """
                                                                        {
                                                                          "httpStatusCode": 400,
                                                                          "httpStatusDescription": "Bad Request",
                                                                          "appErrorCode": "FDR-0702",
                                                                          "errors": [
                                                                            {
                                                                              "message": "Reporting Fdr [<fdr>] is invalid"
                                                                            }
                                                                          ]
                                                                        }"""),
                                                @ExampleObject(
                                                        name = "Errors with path",
                                                        value =
                                                                """
                                                                        {
                                                                          "httpStatusCode": 400,
                                                                          "httpStatusDescription": "Bad Request",
                                                                          "appErrorCode": "FDR-0702",
                                                                          "errors": [
                                                                            {
                                                                              "path": "<detail.path.if-exist>",
                                                                              "message": "<detail.message>"
                                                                            }
                                                                          ]
                                                                        }""")
                                        })),
                        @APIResponse(
                                name = "AppException404",
                                responseCode = "404",
                                description = "Default app exception for status 404",
                                content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON,
                                        schema = @Schema(implementation = ErrorResponse.class),
                                        example =
                                                """
                                                        {
                                                          "httpStatusCode": 404,
                                                          "httpStatusDescription": "Not Found",
                                                          "appErrorCode": "FDR-0701",
                                                          "errors": [
                                                            {
                                                              "message": "Reporting Fdr [<fdr>] not found"
                                                            }
                                                          ]
                                                        }""")),
                }),
        info = @Info(title = "FDR - Flussi di Rendicontazione", version = "0.0.0-SNAPSHOT"))
public class App extends Application {
}
