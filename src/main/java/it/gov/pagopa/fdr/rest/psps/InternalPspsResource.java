package it.gov.pagopa.fdr.rest.psps;

import it.gov.pagopa.fdr.rest.model.GenericResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetPaymentResponse;
import it.gov.pagopa.fdr.rest.psps.request.AddPaymentRequest;
import it.gov.pagopa.fdr.rest.psps.request.CreateRequest;
import it.gov.pagopa.fdr.rest.psps.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.rest.psps.response.GetAllCreatedResponse;
import it.gov.pagopa.fdr.rest.psps.response.GetCreatedResponse;
import it.gov.pagopa.fdr.service.re.model.FdrActionEnum;
import it.gov.pagopa.fdr.util.AppConstant;
import it.gov.pagopa.fdr.util.Re;
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

@Tag(name = "Internal PSP", description = "PSP operations")
@Path("/internal/psps/{" + AppConstant.PSP + "}")
@Consumes("application/json")
@Produces("application/json")
public class InternalPspsResource extends BasePspResource {

  @Operation(operationId = "internalCreate", summary = "Create fdr", description = "Create fdr")
  @RequestBody(content = @Content(schema = @Schema(implementation = CreateRequest.class)))
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
  @POST
  @Path("/fdrs/{" + AppConstant.FDR + "}")
  @Re(action = FdrActionEnum.INTERNAL_CREATE_FLOW)
  public RestResponse<GenericResponse> internalCreate(
      @PathParam(AppConstant.PSP) String pspId,
      @PathParam(AppConstant.FDR) @Pattern(regexp = "[a-zA-Z0-9\\-_]{1,35}") String fdr,
      @NotNull @Valid CreateRequest createRequest) {

    return baseCreate(pspId, fdr, createRequest);
  }

  @Operation(
      operationId = "internalAddPayment",
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
  @PUT
  @Path("/fdrs/{" + AppConstant.FDR + "}/payments/add")
  @Re(action = FdrActionEnum.INTERNAL_ADD_PAYMENT)
  public GenericResponse internalAddPayment(
      @PathParam(AppConstant.PSP) String pspId,
      @PathParam(AppConstant.FDR) String fdr,
      @NotNull @Valid AddPaymentRequest addPaymentRequest) {
    return baseAddPayment(pspId, fdr, addPaymentRequest);
  }

  @Operation(
      operationId = "internalDeletePayment",
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
  @PUT
  @Path("/fdrs/{" + AppConstant.FDR + "}/payments/del")
  @Re(action = FdrActionEnum.INTERNAL_DELETE_PAYMENT)
  public GenericResponse internalDeletePayment(
      @PathParam(AppConstant.PSP) String pspId,
      @PathParam(AppConstant.FDR) String fdr,
      @NotNull @Valid DeletePaymentRequest deletePaymentRequest) {
    return baseDeletePayment(pspId, fdr, deletePaymentRequest);
  }

  @Operation(operationId = "internalPublish", summary = "Publish fdr", description = "Publish fdr")
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
  @POST
  @Path("/fdrs/{" + AppConstant.FDR + "}/publish")
  @Re(action = FdrActionEnum.INTERNAL_PUBLISH)
  public GenericResponse internalPublish(
      @PathParam(AppConstant.PSP) String pspId, @PathParam(AppConstant.FDR) String fdr) {
    return basePublish(pspId, fdr, true);
  }

  @Operation(operationId = "internalDelete", summary = "Delete fdr", description = "Delete fdr")
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
  @DELETE
  @Path("/fdrs/{" + AppConstant.FDR + "}")
  @Re(action = FdrActionEnum.INTERNAL_DELETE_FLOW)
  public GenericResponse internalDelete(
      @PathParam(AppConstant.PSP) String pspId, @PathParam(AppConstant.FDR) String fdr) {
    return baseDelete(pspId, fdr);
  }

  @Operation(
      operationId = "internalGetAllCreated",
      summary = "Get all fdr inserted",
      description = "Get all fdr inserted")
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
                    schema = @Schema(implementation = GetAllCreatedResponse.class)))
      })
  @GET
  @Re(action = FdrActionEnum.INTERNAL_GET_ALL_CREATED_FDR)
  public GetAllCreatedResponse internalGetAllCreated(
      @PathParam(AppConstant.PSP) String pspId,
      @QueryParam(AppConstant.CREATED_GREATER_THAN) Instant createdGt,
      @QueryParam(AppConstant.PAGE) @DefaultValue(AppConstant.PAGE_DEAFULT) @Min(value = 1)
          long pageNumber,
      @QueryParam(AppConstant.SIZE) @DefaultValue(AppConstant.SIZE_DEFAULT) @Min(value = 1)
          long pageSize) {
    return baseGetAllCreated(pspId, createdGt, pageNumber, pageSize);
  }

  @Operation(
      operationId = "internalGetCreated",
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
                    schema = @Schema(implementation = GetCreatedResponse.class)))
      })
  @GET
  @Path("/fdrs/{" + AppConstant.FDR + "}")
  @Re(action = FdrActionEnum.INTERNAL_GET_CREATED_FDR)
  public GetCreatedResponse internalGetCreated(
      @PathParam(AppConstant.PSP) String psp, @PathParam(AppConstant.FDR) String fdr) {
    return baseGetCreated(fdr, psp);
  }

  @Operation(
      operationId = "internalGetCreatedPayment",
      summary = "Get internal created payments of fdr",
      description = "Get internal created payments of fdr")
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
                    schema = @Schema(implementation = GetPaymentResponse.class)))
      })
  @GET
  @Path("/fdrs/{" + AppConstant.FDR + "}/payments")
  @Re(action = FdrActionEnum.INTERNAL_GET_CREATED_FDR_PAYMENT)
  public GetPaymentResponse internalGetCreatedPayment(
      @PathParam(AppConstant.PSP) String psp,
      @PathParam(AppConstant.FDR) String fdr,
      @QueryParam(AppConstant.PAGE) @DefaultValue(AppConstant.PAGE_DEAFULT) @Min(value = 1)
          long pageNumber,
      @QueryParam(AppConstant.SIZE) @DefaultValue(AppConstant.SIZE_DEFAULT) @Min(value = 1)
          long pageSize) {
    return baseGetCreatedFdrPayment(fdr, psp, pageNumber, pageSize);
  }
}
