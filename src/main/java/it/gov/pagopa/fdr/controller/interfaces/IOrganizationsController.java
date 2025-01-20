package it.gov.pagopa.fdr.controller.interfaces;

import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowResponse;
import it.gov.pagopa.fdr.controller.model.payment.response.PaginatedPaymentsResponse;
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

@Path(ControllerConstants.URL_CONTROLLER_ORGANIZATIONS)
@Consumes("application/json")
@Produces("application/json")
@Tag(name = "Organizations", description = "Organizations operations")
public interface IOrganizationsController {

  @GET
  @Operation(
      operationId = "getAllPublishedFlows",
      summary = "Get all fdr published",
      description = "Get all fdr published")
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
                    schema = @Schema(implementation = PaginatedFlowsResponse.class)))
      })
  PaginatedFlowsResponse getAllPublishedFlows(
      @PathParam(ControllerConstants.PARAMETER_ORGANIZATION) @Pattern(regexp = "^(.{1,35})$")
          String organizationId,
      @QueryParam(ControllerConstants.PARAMETER_PSP) @Pattern(regexp = "^(.{1,35})$") String pspId,
      @QueryParam(ControllerConstants.PARAMETER_PUBLISHED_GREATER_THAN) Instant publishedGt,
      @QueryParam(ControllerConstants.PARAMETER_PAGE_INDEX)
          @DefaultValue(ControllerConstants.PARAMETER_PAGE_INDEX_DEFAULT)
          @Min(value = 1)
          long pageNumber,
      @QueryParam(ControllerConstants.PARAMETER_PAGE_SIZE)
          @DefaultValue(ControllerConstants.PARAMETER_PAGE_SIZE_DEFAULT)
          @Min(value = 1)
          long pageSize);

  @GET
  @Path(ControllerConstants.URL_API_GET_SINGLE_FLOW)
  @Operation(operationId = "getSinglePublishedFlow", summary = "Get fdr", description = "Get fdr")
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
                    schema = @Schema(implementation = SingleFlowResponse.class)))
      })
  SingleFlowResponse getSinglePublishedFlow(
      @PathParam(ControllerConstants.PARAMETER_ORGANIZATION) String organizationId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String flowName,
      @PathParam(ControllerConstants.PARAMETER_REVISION) Long revision,
      @PathParam(ControllerConstants.PARAMETER_PSP) String psp);

  @GET
  @Path(ControllerConstants.URL_API_GET_FLOW_PAYMENTS)
  @Operation(
      operationId = "getPaymentsFromPublishedFlow",
      summary = "Get payments of fdr",
      description = "Get payments of fdr")
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
                    schema = @Schema(implementation = PaginatedPaymentsResponse.class)))
      })
  PaginatedPaymentsResponse getPaymentsFromPublishedFlow(
      @PathParam(ControllerConstants.PARAMETER_ORGANIZATION) String organizationId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String flowName,
      @PathParam(ControllerConstants.PARAMETER_REVISION) Long revision,
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @QueryParam(ControllerConstants.PARAMETER_PAGE_INDEX)
          @DefaultValue(ControllerConstants.PARAMETER_PAGE_INDEX_DEFAULT)
          @Min(value = 1)
          long pageNumber,
      @QueryParam(ControllerConstants.PARAMETER_PAGE_SIZE)
          @DefaultValue(ControllerConstants.PARAMETER_PAGE_SIZE_DEFAULT)
          @Min(value = 1)
          long pageSize);
}
