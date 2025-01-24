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
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/internal/psps/{" + ControllerConstants.PARAMETER_PSP + "}/")
@Consumes("application/json")
@Produces("application/json")
@Tag(name = "Support", description = "Support operations")
public interface ISupportController {

  @GET
  @Path("iuv/{" + ControllerConstants.PARAMETER_IUV + "}/")
  @Operation(
      operationId = "ISupportController.getByIuv",
      summary = "Get all payments by psp id and iuv",
      description = "Get all payments by psp id and iuv")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "400 (Syntactic error)",
            description = "Bad Request",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ErrorResponse.class),
                    example = ControllerConstants.OPENAPI_BADREQUESTFIELD_EXAMPLE)),
        @APIResponse(
            responseCode = "400 (Semantic error)",
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
                    example = ControllerConstants.OPENAPI_INTERNALSERVERERROR_EXAMPLE)),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema =
                        @Schema(implementation = PaginatedFlowsBySenderAndReceiverResponse.class)))
      })
  PaginatedFlowsBySenderAndReceiverResponse getByIuv(
      @PathParam(ControllerConstants.PARAMETER_PSP) @Pattern(regexp = "^(.{1,35})$") String pspId,
      @PathParam(ControllerConstants.PARAMETER_IUV) @Pattern(regexp = "^(.{1,35})$") String iuv,
      @QueryParam(ControllerConstants.PARAMETER_CREATED_FROM) Instant createdFrom,
      @QueryParam(ControllerConstants.PARAMETER_CREATED_TO) Instant createdTo,
      @QueryParam(ControllerConstants.PARAMETER_PAGE_INDEX)
          @DefaultValue(ControllerConstants.PARAMETER_PAGE_INDEX_DEFAULT)
          @Min(value = 1)
          long pageNumber,
      @QueryParam(ControllerConstants.PARAMETER_PAGE_SIZE)
          @DefaultValue(ControllerConstants.PARAMETER_PAGE_SIZE_DEFAULT)
          @Min(value = 1)
          long pageSize);

  @GET
  @Path("iur/{" + ControllerConstants.PARAMETER_IUR + "}/")
  @Operation(
      operationId = "ISupportController.getByIur",
      summary = "Get all payments by psp id and iur",
      description = "Get all payments by psp id and iur")
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/AppException400"),
        @APIResponse(ref = "#/components/responses/AppException404"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema =
                        @Schema(implementation = PaginatedFlowsBySenderAndReceiverResponse.class)))
      })
  PaginatedFlowsBySenderAndReceiverResponse getByIur(
      @PathParam(ControllerConstants.PARAMETER_PSP) @Pattern(regexp = "^(.{1,35})$") String pspId,
      @PathParam(ControllerConstants.PARAMETER_IUR) @Pattern(regexp = "^(.{1,35})$") String iur,
      @QueryParam(ControllerConstants.PARAMETER_CREATED_FROM) Instant createdFrom,
      @QueryParam(ControllerConstants.PARAMETER_CREATED_TO) Instant createdTo,
      @QueryParam(ControllerConstants.PARAMETER_PAGE_INDEX)
          @DefaultValue(ControllerConstants.PARAMETER_PAGE_INDEX_DEFAULT)
          @Min(value = 1)
          long pageNumber,
      @QueryParam(ControllerConstants.PARAMETER_PAGE_SIZE)
          @DefaultValue(ControllerConstants.PARAMETER_PAGE_SIZE_DEFAULT)
          @Min(value = 1)
          long pageSize);
}
