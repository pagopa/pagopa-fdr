package it.gov.pagopa.fdr.controller.interfaces.controller;

import it.gov.pagopa.fdr.controller.model.error.ErrorResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsBySenderAndReceiverResponse;
import it.gov.pagopa.fdr.util.constant.ControllerConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.time.Instant;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/internal/psps/{" + ControllerConstants.PARAMETER_PSP + "}/")
@Consumes("application/json")
@Produces("application/json")
@Tag(
    name = "Support",
    description = "APIs for technical support, used for troubleshooting operations")
public interface ISupportController {

  @GET
  @Path("iuv/{" + ControllerConstants.PARAMETER_IUV + "}/")
  @Operation(
      operationId = "ISupportController_getByIuv",
      summary = "Get all flows related to PSP, only if contains a payment with specific IUV code",
      description =
          """
This API permits to search all the flows that contains a payment with specific IUV code
(Identificativo Univoco Versamento) in relation to a PSP.<br>
The result of the query is paginated and contains all the metadata needed for pagination purposes.<br>
This API is used for internal purpose in order to perform a deep-search for dedicated troubleshooting.
""")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema =
                        @Schema(implementation = PaginatedFlowsBySenderAndReceiverResponse.class))),
        @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ErrorResponse.class),
                    example = ControllerConstants.OPENAPI_BADREQUEST_EXAMPLE)),
        @APIResponse(
            responseCode = "404",
            description = "Not Found",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ErrorResponse.class),
                    example = ControllerConstants.OPENAPI_NOTFOUND_EXAMPLE)),
        @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ErrorResponse.class),
                    example = ControllerConstants.OPENAPI_INTERNALSERVERERROR_EXAMPLE))
      })
  PaginatedFlowsBySenderAndReceiverResponse getByIuv(
      @PathParam(ControllerConstants.PARAMETER_PSP)
          @Pattern(regexp = "^(.{1,35})$")
          @Parameter(
              description = "The PSP identifier, used as a search filter",
              example = "88888888888")
          String pspId,
      @PathParam(ControllerConstants.PARAMETER_IUV)
          @Pattern(regexp = "^(.{1,35})$")
          @Parameter(
              description = "The payment's IUV code, used as a search filter",
              example = "17854456582215")
          String iuv,
      @QueryParam(ControllerConstants.PARAMETER_CREATED_FROM)
          @Parameter(
              description = "The lower limit of the date related to the flow creation date",
              example = "2025-01-01T12:00:00.00000Z")
          Instant createdFrom,
      @QueryParam(ControllerConstants.PARAMETER_CREATED_TO)
          @Parameter(
              description = "The upper limit of the date related to the flow creation date",
              example = "2025-01-31T12:00:00.00000Z")
          Instant createdTo,
      @QueryParam(ControllerConstants.PARAMETER_PAGE_INDEX)
          @DefaultValue(ControllerConstants.PARAMETER_PAGE_INDEX_DEFAULT)
          @Min(value = 1)
          @Parameter(description = "The index of the page to be shown in the result", example = "1")
          long pageNumber,
      @QueryParam(ControllerConstants.PARAMETER_PAGE_SIZE)
          @DefaultValue(ControllerConstants.PARAMETER_PAGE_SIZE_DEFAULT)
          @Min(value = 1)
          @Parameter(
              description = "The number of the elements of the page to be shown in the result",
              example = "50")
          long pageSize);

  @GET
  @Path("iur/{" + ControllerConstants.PARAMETER_IUR + "}/")
  @Operation(
      operationId = "ISupportController_getByIur",
      summary = "Get all flows related to PSP, only if contains a payment with specific IUR code",
      description =
          """
This API permits to search all the flows that contains a payment with specific IUR code
(Identificativo Univoco Riscossione) in relation to a PSP.<br>
The result of the query is paginated and contains all the metadata needed for pagination purposes.<br>
This API is used for internal purpose in order to perform a deep-search for dedicated troubleshooting.
""")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema =
                        @Schema(implementation = PaginatedFlowsBySenderAndReceiverResponse.class))),
        @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ErrorResponse.class),
                    example = ControllerConstants.OPENAPI_BADREQUEST_EXAMPLE)),
        @APIResponse(
            responseCode = "404",
            description = "Not Found",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ErrorResponse.class),
                    example = ControllerConstants.OPENAPI_NOTFOUND_EXAMPLE)),
        @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ErrorResponse.class),
                    example = ControllerConstants.OPENAPI_INTERNALSERVERERROR_EXAMPLE))
      })
  PaginatedFlowsBySenderAndReceiverResponse getByIur(
      @PathParam(ControllerConstants.PARAMETER_PSP)
          @Pattern(regexp = "^(.{1,35})$")
          @Parameter(
              description = "The PSP identifier, used as a search filter",
              example = "88888888888")
          String pspId,
      @PathParam(ControllerConstants.PARAMETER_IUR)
          @Pattern(regexp = "^(.{1,35})$")
          @Parameter(
              description = "The payment's IUR code, used as a search filter",
              example = "17854456582215")
          String iur,
      @QueryParam(ControllerConstants.PARAMETER_CREATED_FROM)
          @Parameter(
              description = "The lower limit of the date related to the flow creation date",
              example = "2025-01-01T12:00:00.00000Z")
          Instant createdFrom,
      @QueryParam(ControllerConstants.PARAMETER_CREATED_TO)
          @Parameter(
              description = "The upper limit of the date related to the flow creation date",
              example = "2025-01-31T12:00:00.00000Z")
          Instant createdTo,
      @QueryParam(ControllerConstants.PARAMETER_PAGE_INDEX)
          @DefaultValue(ControllerConstants.PARAMETER_PAGE_INDEX_DEFAULT)
          @Min(value = 1)
          @Parameter(description = "The index of the page to be shown in the result", example = "1")
          long pageNumber,
      @QueryParam(ControllerConstants.PARAMETER_PAGE_SIZE)
          @DefaultValue(ControllerConstants.PARAMETER_PAGE_SIZE_DEFAULT)
          @Min(value = 1)
          @Parameter(
              description = "The number of the elements of the page to be shown in the result",
              example = "50")
          long pageSize);
}
