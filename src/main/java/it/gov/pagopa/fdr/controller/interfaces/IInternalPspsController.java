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

@Path(ControllerConstants.URL_CONTROLLER_INTERNAL_PSPS)
@Consumes("application/json")
@Produces("application/json")
@Tag(name = "Internal PSP", description = "PSP operations")
public interface IInternalPspsController {

  @POST
  @Path(ControllerConstants.URL_API_CREATE_EMPTY_FLOW)
  @Operation(
      operationId = "createEmptyFlowForInternalUse",
      summary = "Create fdr",
      description = "Create fdr")
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
  RestResponse<GenericResponse> createEmptyFlowForInternalUse(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) @Pattern(regexp = "[a-zA-Z0-9\\-_]{1,35}")
          String flowName,
      @NotNull @Valid CreateFlowRequest request);

  @PUT
  @Path(ControllerConstants.URL_API_ADD_PAYMENT_IN_FLOW)
  @Operation(
      operationId = "addPaymentToExistingFlowForInternalUse",
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
  GenericResponse addPaymentToExistingFlowForInternalUse(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String flowName,
      @NotNull @Valid AddPaymentRequest request);

  @PUT
  @Path(ControllerConstants.URL_API_DELETE_PAYMENT_IN_FLOW)
  @Operation(
      operationId = "deletePaymentFromExistingFlowForInternalUse",
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
  GenericResponse deletePaymentFromExistingFlowForInternalUse(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String flowName,
      @NotNull @Valid DeletePaymentRequest request);

  @POST
  @Path(ControllerConstants.URL_API_PUBLISH_FLOW)
  @Operation(
      operationId = "publishFlowForInternalUse",
      summary = "Publish fdr",
      description = "Publish fdr")
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
  GenericResponse publishFlowForInternalUse(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String flowName);

  @DELETE
  @Path(ControllerConstants.URL_API_DELETE_FLOW)
  @Operation(
      operationId = "deleteExistingFlowForInternalUse",
      summary = "Delete fdr",
      description = "Delete fdr")
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
  GenericResponse deleteExistingFlowForInternalUse(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String flowName);

  @GET
  @Path(ControllerConstants.URL_API_GET_ALL_NOT_PUBLISHED_FLOWS)
  @Operation(
      operationId = "getAllFlowsNotInPublishedStatusForInternalUse",
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
  PaginatedFlowsCreatedResponse getAllFlowsNotInPublishedStatusForInternalUse(
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
      operationId = "getSingleFlowNotInPublishedStatusForInternalUse",
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
  SingleFlowCreatedResponse getSingleFlowNotInPublishedStatusForInternalUse(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String flowName,
      @PathParam(ControllerConstants.PARAMETER_ORGANIZATION) String organizationId);

  @GET
  @Path(ControllerConstants.URL_API_GET_PAYMENTS_FOR_NOT_PUBLISHED_FLOW)
  @Operation(
      operationId = "getPaymentsForFlowNotInPublishedStatusForInternalUse",
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
  PaginatedPaymentsResponse getPaymentsForFlowNotInPublishedStatusForInternalUse(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String flowName,
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
      operationId = "getAllFlowsInPublishedStatusForInternalUse",
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
  PaginatedFlowsPublishedResponse getAllFlowsInPublishedStatusForInternalUse(
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
      operationId = "getSingleFlowInPublishedStatusForInternalUse",
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
  SingleFlowResponse getSingleFlowInPublishedStatusForInternalUse(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String flowName,
      @PathParam(ControllerConstants.PARAMETER_REVISION) Long revision,
      @PathParam(ControllerConstants.PARAMETER_ORGANIZATION) String organizationId);

  @GET
  @Path(ControllerConstants.URL_API_GET_PAYMENTS_FOR_PUBLISHED_FLOW)
  @Operation(
      operationId = "getPaymentsForFlowInPublishedStatusForInternalUse",
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
  PaginatedPaymentsResponse getPaymentsForFlowInPublishedStatusForInternalUse(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String flowName,
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
