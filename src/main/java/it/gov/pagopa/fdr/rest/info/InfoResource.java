package it.gov.pagopa.fdr.rest.info;

import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.rest.info.response.InfoResponse;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import java.util.Arrays;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

@Path("/info")
@Tag(name = "Info", description = "Info operations")
public class InfoResource {

  @Inject Logger log;

  @ConfigProperty(name = "app.name", defaultValue = "app")
  String name;

  @ConfigProperty(name = "app.version", defaultValue = "0.0.0")
  String version;

  @ConfigProperty(name = "app.environment", defaultValue = "local")
  String environment;

  @Operation(summary = "Get info of FDR")
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/BadRequest"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = InfoResponse.class)))
      })
  @Produces(MediaType.APPLICATION_JSON)
  @GET
  public InfoResponse hello() {
    log.infof("Info environment: [%s] - name: [%s] - version: [%s]", environment, name, version);

    return InfoResponse.builder()
        .name(name)
        .version(version)
        .environment(environment)
        .description(AppMessageUtil.getMessage("app.description"))
        .errorCodes(
            Arrays.stream(AppErrorCodeMessageEnum.values())
                .map(
                    errorCode ->
                        InfoResponse.ErrorCode.builder()
                            .code(errorCode.errorCode())
                            .description(errorCode.message())
                            .statusCode(errorCode.httpStatus().getStatusCode())
                            .build())
                .toList())
        .build();
  }
}
