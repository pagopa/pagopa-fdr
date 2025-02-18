package it.gov.pagopa.fdr.controller.interfaces.controller;

import it.gov.pagopa.fdr.controller.model.common.response.GenericResponse;
import it.gov.pagopa.fdr.controller.model.error.ErrorResponse;
import it.gov.pagopa.fdr.controller.model.flow.request.CreateFlowRequest;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowCreatedResponse;
import it.gov.pagopa.fdr.controller.model.payment.request.AddPaymentRequest;
import it.gov.pagopa.fdr.controller.model.payment.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.util.constant.ControllerConstants;
import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.openapi.APIAppErrorMetadata;
import it.gov.pagopa.fdr.util.openapi.APITableMetadata;
import it.gov.pagopa.fdr.util.openapi.APITableMetadata.APISecurityMode;
import it.gov.pagopa.fdr.util.openapi.APITableMetadata.APISynchronism;
import it.gov.pagopa.fdr.util.openapi.APITableMetadata.ReadWrite;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
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
@Tag(name = "Internal Operations", description = "APIs for internal operations")
public interface IInternalOperationsController {

  @POST
  @Path(ControllerConstants.URL_API_CREATE_EMPTY_FLOW)
  @Operation(
      operationId = "IInternalOperationsController.createEmptyFlowForInternalUse",
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
      synchronism = APISynchronism.SYNC,
      authorization = APISecurityMode.NONE,
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
  RestResponse<GenericResponse> createEmptyFlowForInternalUse(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) @Pattern(regexp = "[a-zA-Z0-9\\-_]{1,35}")
          String flowName,
      @NotNull @Valid CreateFlowRequest request);

  @PUT
  @Path(ControllerConstants.URL_API_ADD_PAYMENT_IN_FLOW)
  @Operation(
      operationId = "IInternalOperationsController.addPaymentToExistingFlowForInternalUse",
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
      synchronism = APISynchronism.SYNC,
      authorization = APISecurityMode.NONE,
      authentication = APISecurityMode.APIKEY,
      idempotency = false,
      readWriteIntense = ReadWrite.WRITE)
  @APIAppErrorMetadata(
      errors = {
        AppErrorCodeMessageEnum.PSP_UNKNOWN,
        AppErrorCodeMessageEnum.PSP_NOT_ENABLED,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_DATE_WRONG_FORMAT,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_PSP_WRONG_FORMAT,
        AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_SAME_INDEX_IN_SAME_REQUEST,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND,
        AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_DUPLICATE_INDEX,
      })
  GenericResponse addPaymentToExistingFlowForInternalUse(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String flowName,
      @NotNull @Valid AddPaymentRequest request);

  @PUT
  @Path(ControllerConstants.URL_API_DELETE_PAYMENT_IN_FLOW)
  @Operation(
      operationId = "IInternalOperationsController.deletePaymentFromExistingFlowForInternalUse",
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
      synchronism = APISynchronism.SYNC,
      authorization = APISecurityMode.NONE,
      authentication = APISecurityMode.APIKEY,
      idempotency = false,
      readWriteIntense = ReadWrite.WRITE)
  @APIAppErrorMetadata(
      errors = {
        AppErrorCodeMessageEnum.PSP_UNKNOWN,
        AppErrorCodeMessageEnum.PSP_NOT_ENABLED,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_DATE_WRONG_FORMAT,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_PSP_WRONG_FORMAT,
        AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_SAME_INDEX_IN_SAME_REQUEST,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND,
        AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION,
        AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_NO_MATCH_INDEX
      })
  GenericResponse deletePaymentFromExistingFlowForInternalUse(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String flowName,
      @NotNull @Valid DeletePaymentRequest request);

  @POST
  @Path(ControllerConstants.URL_API_PUBLISH_FLOW)
  @Operation(
      operationId = "IInternalOperationsController.publishFlowForInternalUse",
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
      synchronism = APISynchronism.SYNC,
      authorization = APISecurityMode.NONE,
      authentication = APISecurityMode.APIKEY,
      idempotency = false,
      readWriteIntense = ReadWrite.WRITE)
  @APIAppErrorMetadata(
      errors = {
        AppErrorCodeMessageEnum.PSP_UNKNOWN,
        AppErrorCodeMessageEnum.PSP_NOT_ENABLED,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_DATE_WRONG_FORMAT,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_PSP_WRONG_FORMAT,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND,
        AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION,
        AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_TOT_PAYMENT,
        AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_SUM_PAYMENT
      })
  GenericResponse publishFlowForInternalUse(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String flowName);

  @DELETE
  @Path(ControllerConstants.URL_API_DELETE_FLOW)
  @Operation(
      operationId = "IInternalOperationsController.deleteExistingFlowForInternalUse",
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
      synchronism = APISynchronism.SYNC,
      authorization = APISecurityMode.NONE,
      authentication = APISecurityMode.APIKEY,
      idempotency = false,
      readWriteIntense = ReadWrite.BOTH)
  @APIAppErrorMetadata(
      errors = {
        AppErrorCodeMessageEnum.PSP_UNKNOWN,
        AppErrorCodeMessageEnum.PSP_NOT_ENABLED,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_DATE_WRONG_FORMAT,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_PSP_WRONG_FORMAT,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND
      })
  GenericResponse deleteExistingFlowForInternalUse(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String flowName);

  @GET
  @Path(ControllerConstants.URL_API_GET_SINGLE_NOT_PUBLISHED_FLOW)
  @Operation(
      operationId = "IInternalOperationsController.getSingleFlowNotInPublishedStatusForInternalUse",
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
      synchronism = APISynchronism.SYNC,
      authorization = APISecurityMode.NONE,
      authentication = APISecurityMode.APIKEY,
      readWriteIntense = ReadWrite.READ,
      cacheable = true)
  @APIAppErrorMetadata(
      errors = {
        AppErrorCodeMessageEnum.PSP_UNKNOWN,
        AppErrorCodeMessageEnum.PSP_NOT_ENABLED,
        AppErrorCodeMessageEnum.EC_UNKNOWN,
        AppErrorCodeMessageEnum.EC_NOT_ENABLED,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_DATE_WRONG_FORMAT,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_PSP_WRONG_FORMAT,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND,
      })
  SingleFlowCreatedResponse getSingleFlowNotInPublishedStatusForInternalUse(
      @PathParam(ControllerConstants.PARAMETER_PSP) String pspId,
      @PathParam(ControllerConstants.PARAMETER_FDR) String flowName,
      @PathParam(ControllerConstants.PARAMETER_ORGANIZATION) String organizationId);
}
