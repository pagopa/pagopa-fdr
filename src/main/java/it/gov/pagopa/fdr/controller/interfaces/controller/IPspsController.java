package it.gov.pagopa.fdr.controller.interfaces.controller;

import it.gov.pagopa.fdr.controller.model.common.response.GenericResponse;
import it.gov.pagopa.fdr.controller.model.error.ErrorResponse;
import it.gov.pagopa.fdr.controller.model.flow.request.CreateFlowRequest;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsCreatedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsPublishedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowCreatedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowResponse;
import it.gov.pagopa.fdr.controller.model.payment.request.AddPaymentRequest;
import it.gov.pagopa.fdr.controller.model.payment.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.controller.model.payment.response.PaginatedPaymentsResponse;
import it.gov.pagopa.fdr.util.constant.ControllerConstants;
import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.openapi.APIAppErrorMetadata;
import it.gov.pagopa.fdr.util.openapi.APITableMetadata;
import it.gov.pagopa.fdr.util.openapi.APITableMetadata.APISecurityMode;
import it.gov.pagopa.fdr.util.openapi.APITableMetadata.APISynchronism;
import it.gov.pagopa.fdr.util.openapi.APITableMetadata.ReadWrite;
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
  @Operation(
      operationId = "IPspsController.createEmptyFlow",
      summary = "Create fdr",
      description = "Create fdr")
  @RequestBody(content = @Content(schema = @Schema(implementation = CreateFlowRequest.class)))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "201",
            description = "Created",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GenericResponse.class))),
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
  @APITableMetadata(
      internal = false,
      synchronism = APISynchronism.SYNC,
      authorization = APISecurityMode.AUTHORIZER,
      authentication = APISecurityMode.APIKEY,
      idempotency = false,
      readWriteIntense = ReadWrite.WRITE)
  @APIAppErrorMetadata(
      errors = {
        AppErrorCodeMessageEnum.PSP_UNKNOWN,
        AppErrorCodeMessageEnum.PSP_NOT_ENABLED,
        AppErrorCodeMessageEnum.REPORTING_FLOW_ALREADY_EXIST,
        AppErrorCodeMessageEnum.BROKER_UNKNOWN,
        AppErrorCodeMessageEnum.BROKER_NOT_ENABLED,
        AppErrorCodeMessageEnum.CHANNEL_UNKNOWN,
        AppErrorCodeMessageEnum.CHANNEL_NOT_ENABLED,
        AppErrorCodeMessageEnum.CHANNEL_BROKER_WRONG_CONFIG,
        AppErrorCodeMessageEnum.CHANNEL_PSP_WRONG_CONFIG,
        AppErrorCodeMessageEnum.EC_UNKNOWN,
        AppErrorCodeMessageEnum.EC_NOT_ENABLED,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_DATE_WRONG_FORMAT,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_PSP_WRONG_FORMAT,
        AppErrorCodeMessageEnum.REPORTING_FLOW_PSP_ID_NOT_MATCH,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_NOT_MATCH
      })
  RestResponse<GenericResponse> createEmptyFlow(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) @Pattern(regexp = "[a-zA-Z0-9\\-_]{1,35}")
          String flowName,
      @NotNull @Valid CreateFlowRequest request);

  @PUT
  @Path(ControllerConstants.URL_API_ADD_PAYMENT_IN_FLOW)
  @Operation(
      operationId = "IPspsController.addPaymentToExistingFlow",
      summary = "Add payments to fdr",
      description = "Add payments to fdr")
  @RequestBody(content = @Content(schema = @Schema(implementation = AddPaymentRequest.class)))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GenericResponse.class))),
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
  @APITableMetadata(
      internal = false,
      synchronism = APISynchronism.SYNC,
      authorization = APISecurityMode.AUTHORIZER,
      authentication = APISecurityMode.APIKEY,
      idempotency = false,
      readWriteIntense = ReadWrite.WRITE)
  GenericResponse addPaymentToExistingFlow(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String flowName,
      @NotNull @Valid AddPaymentRequest request);

  @PUT
  @Path(ControllerConstants.URL_API_DELETE_PAYMENT_IN_FLOW)
  @Operation(
      operationId = "IPspsController.deletePaymentFromExistingFlow",
      summary = "Delete payments to fdr",
      description = "Delete payments to fdr")
  @RequestBody(content = @Content(schema = @Schema(implementation = DeletePaymentRequest.class)))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GenericResponse.class))),
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
  @APITableMetadata(
      internal = false,
      synchronism = APISynchronism.SYNC,
      authorization = APISecurityMode.AUTHORIZER,
      authentication = APISecurityMode.APIKEY,
      idempotency = false,
      readWriteIntense = ReadWrite.WRITE)
  GenericResponse deletePaymentFromExistingFlow(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String flowName,
      @NotNull @Valid DeletePaymentRequest request);

  @POST
  @Path(ControllerConstants.URL_API_PUBLISH_FLOW)
  @Operation(
      operationId = "IPspsController.publishFlow",
      summary = "Publish fdr",
      description = "Publish fdr")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GenericResponse.class))),
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
  @APITableMetadata(
      internal = false,
      synchronism = APISynchronism.SYNC,
      authorization = APISecurityMode.AUTHORIZER,
      authentication = APISecurityMode.APIKEY,
      idempotency = false,
      readWriteIntense = ReadWrite.WRITE)
  GenericResponse publishFlow(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String flowName);

  @DELETE
  @Path(ControllerConstants.URL_API_DELETE_FLOW)
  @Operation(
      operationId = "IPspsController.deleteExistingFlow",
      summary = "Delete fdr",
      description = "Delete fdr")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GenericResponse.class))),
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
  @APITableMetadata(
      internal = false,
      synchronism = APISynchronism.SYNC,
      authorization = APISecurityMode.AUTHORIZER,
      authentication = APISecurityMode.APIKEY,
      idempotency = false,
      readWriteIntense = ReadWrite.BOTH)
  GenericResponse deleteExistingFlow(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String flowName);

  @GET
  @Path(ControllerConstants.URL_API_GET_ALL_NOT_PUBLISHED_FLOWS)
  @Operation(
      operationId = "IPspsController.getAllFlowsNotInPublishedStatus",
      summary = "Get all fdr created",
      description = "Get all fdr created")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = PaginatedFlowsCreatedResponse.class))),
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
  @APITableMetadata(
      internal = false,
      synchronism = APISynchronism.SYNC,
      authorization = APISecurityMode.AUTHORIZER,
      authentication = APISecurityMode.APIKEY,
      readWriteIntense = ReadWrite.READ,
      cacheable = true)
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
      operationId = "IPspsController.getSingleFlowNotInPublishedStatus",
      summary = "Get created fdr",
      description = "Get created fdr")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SingleFlowCreatedResponse.class))),
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
  @APITableMetadata(
      internal = false,
      synchronism = APISynchronism.SYNC,
      authorization = APISecurityMode.AUTHORIZER,
      authentication = APISecurityMode.APIKEY,
      readWriteIntense = ReadWrite.READ,
      cacheable = true)
  SingleFlowCreatedResponse getSingleFlowNotInPublishedStatus(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String flowName,
      @PathParam(ControllerConstants.PARAMETER_ORGANIZATION) String organizationId);

  @GET
  @Path(ControllerConstants.URL_API_GET_PAYMENTS_FOR_NOT_PUBLISHED_FLOW)
  @Operation(
      operationId = "IPspsController.getPaymentsForFlowNotInPublishedStatus",
      summary = "Get created payments of fdr",
      description = "Get created payments of fdr")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = PaginatedPaymentsResponse.class))),
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
  @APITableMetadata(
      internal = false,
      synchronism = APISynchronism.SYNC,
      authorization = APISecurityMode.AUTHORIZER,
      authentication = APISecurityMode.APIKEY,
      readWriteIntense = ReadWrite.READ,
      cacheable = true)
  PaginatedPaymentsResponse getPaymentsForFlowNotInPublishedStatus(
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
      operationId = "IPspsController.getAllFlowsInPublishedStatus",
      summary = "Get all fdr published",
      description = "Get all fdr published")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = PaginatedFlowsPublishedResponse.class))),
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
  @APITableMetadata(
      internal = false,
      synchronism = APISynchronism.SYNC,
      authorization = APISecurityMode.AUTHORIZER,
      authentication = APISecurityMode.APIKEY,
      readWriteIntense = ReadWrite.READ,
      cacheable = true)
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
      operationId = "IPspsController.getSingleFlowInPublishedStatus",
      summary = "Get fdr Published",
      description = "Get fdr Published")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SingleFlowResponse.class))),
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
  @APITableMetadata(
      internal = false,
      synchronism = APISynchronism.SYNC,
      authorization = APISecurityMode.AUTHORIZER,
      authentication = APISecurityMode.APIKEY,
      readWriteIntense = ReadWrite.READ,
      cacheable = true)
  SingleFlowResponse getSingleFlowInPublishedStatus(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String flowName,
      @PathParam(ControllerConstants.PARAMETER_REVISION) Long revision,
      @PathParam(ControllerConstants.PARAMETER_ORGANIZATION) String organizationId);

  @GET
  @Path(ControllerConstants.URL_API_GET_PAYMENTS_FOR_PUBLISHED_FLOW)
  @Operation(
      operationId = "IPspsController.getPaymentsForFlowInPublishedStatus",
      summary = "Get payments of fdr Published",
      description = "Get payments of fdr Published")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = PaginatedPaymentsResponse.class))),
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
  @APITableMetadata(
      internal = false,
      synchronism = APISynchronism.SYNC,
      authorization = APISecurityMode.AUTHORIZER,
      authentication = APISecurityMode.APIKEY,
      readWriteIntense = ReadWrite.READ,
      cacheable = true)
  PaginatedPaymentsResponse getPaymentsForFlowInPublishedStatus(
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
