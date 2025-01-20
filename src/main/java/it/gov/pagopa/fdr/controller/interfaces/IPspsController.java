package it.gov.pagopa.fdr.controller.interfaces;

import it.gov.pagopa.fdr.controller.model.common.response.GenericResponse;
import it.gov.pagopa.fdr.controller.model.flow.request.CreateFlowRequest;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsCreatedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsPublishedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowCreatedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowResponse;
import it.gov.pagopa.fdr.controller.model.payment.request.AddPaymentRequest;
import it.gov.pagopa.fdr.controller.model.payment.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.controller.model.payment.response.PaginatedPaymentsResponse;
import it.gov.pagopa.fdr.util.constant.AppConstant;
import it.gov.pagopa.fdr.util.constant.ControllerConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.time.Instant;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestResponse;

@Path(ControllerConstants.URL_CONTROLLER_PSPS)
@Consumes("application/json")
@Produces("application/json")
@Tag(name = "PSP", description = "PSP operations")
public interface IPspsController {

  @POST
  @Path(ControllerConstants.URL_API_CREATE_EMPTY_FLOW)
  @Operation(operationId = "createEmptyFlow", summary = "Create fdr", description = "Create fdr")
  @RequestBody(content = @Content(schema = @Schema(implementation = CreateFlowRequest.class)))
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/AppException400"),
        @APIResponse(ref = "#/components/responses/AppException404"),
        @APIResponse(
            responseCode = "201",
            description = "Created",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GenericResponse.class)))
      })
  RestResponse<GenericResponse> createEmptyFlow(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) @Pattern(regexp = "[a-zA-Z0-9\\-_]{1,35}")
          String fdrName,
      @NotNull @Valid CreateFlowRequest request);

  @PUT
  @Path(ControllerConstants.URL_API_ADD_PAYMENT_IN_FLOW)
  @Operation(
      operationId = "addPaymentToExistingFlow",
      summary = "Add payments to fdr",
      description = "Add payments to fdr")
  @RequestBody(content = @Content(schema = @Schema(implementation = AddPaymentRequest.class)))
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
                    schema = @Schema(implementation = GenericResponse.class)))
      })
  GenericResponse addPaymentToExistingFlow(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String fdrName,
      @NotNull @Valid AddPaymentRequest request);

  @PUT
  @Path(ControllerConstants.URL_API_DELETE_PAYMENT_IN_FLOW)
  @Operation(
      operationId = "deletePaymentFromExistingFlow",
      summary = "Delete payments to fdr",
      description = "Delete payments to fdr")
  @RequestBody(content = @Content(schema = @Schema(implementation = DeletePaymentRequest.class)))
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
                    schema = @Schema(implementation = GenericResponse.class)))
      })
  GenericResponse deletePaymentFromExistingFlow(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String fdrName,
      @NotNull @Valid DeletePaymentRequest request);

  @POST
  @Path(ControllerConstants.URL_API_PUBLISH_FLOW)
  @Operation(operationId = "publishFlow", summary = "Publish fdr", description = "Publish fdr")
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
                    schema = @Schema(implementation = GenericResponse.class)))
      })
  GenericResponse publishFlow(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String fdrName);

  @DELETE
  @Path(ControllerConstants.URL_API_DELETE_FLOW)
  @Operation(operationId = "deleteExistingFlow", summary = "Delete fdr", description = "Delete fdr")
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
                    schema = @Schema(implementation = GenericResponse.class)))
      })
  GenericResponse deleteExistingFlow(
      @PathParam(AppConstant.PSP) String pspId, @PathParam(AppConstant.FDR) String fdrName);

  @GET
  @Path(ControllerConstants.URL_API_GET_ALL_NOT_PUBLISHED_FLOWS)
  @Operation(
      operationId = "getAllFlowsNotInPublishedStatus",
      summary = "Get all fdr created",
      description = "Get all fdr created")
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
                    schema = @Schema(implementation = PaginatedFlowsCreatedResponse.class)))
      })
  PaginatedFlowsCreatedResponse getAllFlowsNotInPublishedStatus(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @QueryParam(ControllerConstants.PARAMETER_CREATED_GREATER_THAN) Instant createdGt,
      @QueryParam(ControllerConstants.PARAMETER_PAGE_INDEX)
          @DefaultValue(ControllerConstants.PARAMETER_PAGE_INDEX_DEFAULT)
          @Min(value = 1)
          long pageNumber,
      @QueryParam(ControllerConstants.PARAMETER_PAGE_SIZE)
          @DefaultValue(ControllerConstants.PARAMETER_PAGE_SIZE_DEFAULT)
          @Min(value = 1)
          long pageSize);

  @GET
  @Path(ControllerConstants.URL_API_GET_SINGLE_NOT_PUBLISHED_FLOW)
  @Operation(
      operationId = "getSingleFlowNotInPublishedStatus",
      summary = "Get created fdr",
      description = "Get created fdr")
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
                    schema = @Schema(implementation = SingleFlowCreatedResponse.class)))
      })
  SingleFlowCreatedResponse getSingleFlowNotInPublishedStatus(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String fdrName,
      @PathParam(ControllerConstants.PARAMETER_ORGANIZATION) String organizationId);

  @GET
  @Path(ControllerConstants.URL_API_GET_PAYMENTS_FOR_NOT_PUBLISHED_FLOW)
  @Operation(
      operationId = "getPaymentsForFlowNotInPublishedStatus",
      summary = "Get created payments of fdr",
      description = "Get created payments of fdr")
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
  PaginatedPaymentsResponse getPaymentsForFlowNotInPublishedStatus(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String fdrName,
      @PathParam(ControllerConstants.PARAMETER_ORGANIZATION) String organizationId,
      @QueryParam(ControllerConstants.PARAMETER_PAGE_INDEX)
          @DefaultValue(ControllerConstants.PARAMETER_PAGE_INDEX_DEFAULT)
          @Min(value = 1)
          long pageNumber,
      @QueryParam(ControllerConstants.PARAMETER_PAGE_SIZE)
          @DefaultValue(ControllerConstants.PARAMETER_PAGE_SIZE_DEFAULT)
          @Min(value = 1)
          long pageSize);

  @GET
  @Path(ControllerConstants.URL_API_GET_ALL_PUBLISHED_FLOWS)
  @Operation(
      operationId = "getAllFlowsInPublishedStatus",
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
                    schema = @Schema(implementation = PaginatedFlowsPublishedResponse.class)))
      })
  PaginatedFlowsPublishedResponse getAllFlowsInPublishedStatus(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @QueryParam(ControllerConstants.PARAMETER_ORGANIZATION) @Pattern(regexp = "^(.{1,35})$")
          String organizationId,
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
  @Path(ControllerConstants.URL_API_GET_SINGLE_PUBLISHED_FLOW)
  @Operation(
      operationId = "getSingleFlowInPublishedStatus",
      summary = "Get fdr Published",
      description = "Get fdr Published")
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
  SingleFlowResponse getSingleFlowInPublishedStatus(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String fdrName,
      @PathParam(ControllerConstants.PARAMETER_REVISION) Long revision,
      @PathParam(ControllerConstants.PARAMETER_ORGANIZATION) String organizationId);

  @GET
  @Path(ControllerConstants.URL_API_GET_PAYMENTS_FOR_PUBLISHED_FLOW)
  @Operation(
      operationId = "getPaymentsForFlowInPublishedStatus",
      summary = "Get payments of fdr Published",
      description = "Get payments of fdr Published")
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
  PaginatedPaymentsResponse getPaymentsForFlowInPublishedStatus(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String fdrName,
      @PathParam(ControllerConstants.PARAMETER_REVISION) Long revision,
      @PathParam(ControllerConstants.PARAMETER_ORGANIZATION) String organizationId,
      @QueryParam(ControllerConstants.PARAMETER_PAGE_INDEX)
          @DefaultValue(ControllerConstants.PARAMETER_PAGE_INDEX_DEFAULT)
          @Min(value = 1)
          long pageNumber,
      @QueryParam(ControllerConstants.PARAMETER_PAGE_SIZE)
          @DefaultValue(ControllerConstants.PARAMETER_PAGE_SIZE_DEFAULT)
          @Min(value = 1)
          long pageSize);
}
