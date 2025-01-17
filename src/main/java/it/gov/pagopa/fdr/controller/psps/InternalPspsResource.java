package it.gov.pagopa.fdr.controller.psps;

import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.controller.model.GenericResponse;
import it.gov.pagopa.fdr.controller.organizations.response.GetPaymentResponse;
import it.gov.pagopa.fdr.controller.organizations.response.GetResponse;
import it.gov.pagopa.fdr.controller.psps.mapper.PspsResourceServiceMapper;
import it.gov.pagopa.fdr.controller.psps.request.AddPaymentRequest;
import it.gov.pagopa.fdr.controller.psps.request.CreateRequest;
import it.gov.pagopa.fdr.controller.psps.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.controller.psps.response.GetAllCreatedResponse;
import it.gov.pagopa.fdr.controller.psps.response.GetAllPublishedResponse;
import it.gov.pagopa.fdr.controller.psps.response.GetCreatedResponse;
import it.gov.pagopa.fdr.controller.psps.validation.InternalPspValidationService;
import it.gov.pagopa.fdr.controller.psps.validation.PspsValidationService;
import it.gov.pagopa.fdr.service.psps.PspsService;
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
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;

@Tag(name = "Internal PSP", description = "PSP operations")
@Path("/internal/psps/{" + AppConstant.PSP + "}")
@Consumes("application/json")
@Produces("application/json")
public class InternalPspsResource extends BasePspResource {

  protected InternalPspsResource(
      Logger log,
      Config config,
      PspsValidationService validator,
      InternalPspValidationService internalValidator,
      PspsResourceServiceMapper mapper,
      PspsService service) {
    super(log, config, validator, internalValidator, mapper, service);
  }

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
  @Path("/created")
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
  @Path("/created/fdrs/{" + AppConstant.FDR + "}/organizations/{" + AppConstant.ORGANIZATION + "}")
  @Re(action = FdrActionEnum.INTERNAL_GET_CREATED_FDR)
  public GetCreatedResponse internalGetCreated(
      @PathParam(AppConstant.PSP) String psp,
      @PathParam(AppConstant.FDR) String fdr,
      @PathParam(AppConstant.ORGANIZATION) String organizationId) {
    return baseGetCreated(fdr, psp, organizationId);
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
  @Path(
      "/created/fdrs/{"
          + AppConstant.FDR
          + "}/organizations/{"
          + AppConstant.ORGANIZATION
          + "}/payments")
  @Re(action = FdrActionEnum.INTERNAL_GET_CREATED_FDR_PAYMENT)
  public GetPaymentResponse internalGetCreatedPayment(
      @PathParam(AppConstant.PSP) String psp,
      @PathParam(AppConstant.FDR) String fdr,
      @PathParam(AppConstant.ORGANIZATION) String organizationId,
      @QueryParam(AppConstant.PAGE) @DefaultValue(AppConstant.PAGE_DEAFULT) @Min(value = 1)
          long pageNumber,
      @QueryParam(AppConstant.SIZE) @DefaultValue(AppConstant.SIZE_DEFAULT) @Min(value = 1)
          long pageSize) {
    return baseGetCreatedFdrPayment(fdr, psp, organizationId, pageNumber, pageSize);
  }

  @Operation(
      operationId = "internalGetAllPublishedByPsp",
      summary = "Get all internal fdr published",
      description = "Get all internal fdr published")
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
                    schema = @Schema(implementation = GetAllPublishedResponse.class)))
      })
  @GET
  @Path("/published")
  @Re(action = FdrActionEnum.INTERNAL_GET_ALL_FDR_PUBLISHED_BY_PSP)
  public GetAllPublishedResponse internalGetAllPublishedByPsp(
      @PathParam(AppConstant.PSP) String idPsp,
      @QueryParam(AppConstant.ORGANIZATION) @Pattern(regexp = "^(.{1,35})$") String organizationId,
      @QueryParam(AppConstant.PUBLISHED_GREATER_THAN) Instant publishedGt,
      @QueryParam(AppConstant.PAGE) @DefaultValue(AppConstant.PAGE_DEAFULT) @Min(value = 1)
          long pageNumber,
      @QueryParam(AppConstant.SIZE) @DefaultValue(AppConstant.SIZE_DEFAULT) @Min(value = 1)
          long pageSize) {
    return baseGetAllPublished(idPsp, organizationId, publishedGt, pageNumber, pageSize);
  }

  @Operation(
      operationId = "internalGetPublishedByPsp",
      summary = "Get internal fdr Published",
      description = "Get internal fdr Published")
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
                    schema = @Schema(implementation = GetResponse.class)))
      })
  @GET
  @Path(
      "/published/fdrs/{"
          + AppConstant.FDR
          + "}/revisions/{"
          + AppConstant.REVISION
          + "}/organizations/{"
          + AppConstant.ORGANIZATION
          + "}")
  @Re(action = FdrActionEnum.INTERNAL_GET_FDR_PUBLISHED_BY_PSP)
  public GetResponse internalGetPublishedByPsp(
      @PathParam(AppConstant.PSP) String psp,
      @PathParam(AppConstant.FDR) String fdr,
      @PathParam(AppConstant.REVISION) Long rev,
      @PathParam(AppConstant.ORGANIZATION) String organizationId) {
    return baseGetPublished(organizationId, fdr, rev, psp);
  }

  @Operation(
      operationId = "internalGetPaymentPublishedByPSp",
      summary = "Get internal payments of fdr Published",
      description = "Get internal payments of fdr Published")
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
  @Path(
      "/published/fdrs/{"
          + AppConstant.FDR
          + "}/revisions/{"
          + AppConstant.REVISION
          + "}/organizations/{"
          + AppConstant.ORGANIZATION
          + "}/payments")
  @Re(action = FdrActionEnum.INTERNAL_GET_FDR_PAYMENT_PUBLISHED_BY_PSP)
  public GetPaymentResponse internalGetPaymentPublishedByPSp(
      @PathParam(AppConstant.PSP) String psp,
      @PathParam(AppConstant.FDR) String fdr,
      @PathParam(AppConstant.REVISION) Long rev,
      @PathParam(AppConstant.ORGANIZATION) String organizationId,
      @QueryParam(AppConstant.PAGE) @DefaultValue(AppConstant.PAGE_DEAFULT) @Min(value = 1)
          long pageNumber,
      @QueryParam(AppConstant.SIZE) @DefaultValue(AppConstant.SIZE_DEFAULT) @Min(value = 1)
          long pageSize) {
    return baseGetFdrPaymentPublished(organizationId, fdr, rev, psp, pageNumber, pageSize);
  }
}
