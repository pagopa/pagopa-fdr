package it.gov.pagopa.fdr.rest.psps;

import it.gov.pagopa.fdr.rest.model.GenericResponse;
import it.gov.pagopa.fdr.rest.psps.request.AddPaymentRequest;
import it.gov.pagopa.fdr.rest.psps.request.CreateRequest;
import it.gov.pagopa.fdr.rest.psps.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.service.re.model.FdrActionEnum;
import it.gov.pagopa.fdr.util.AppConstant;
import it.gov.pagopa.fdr.util.Re;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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

@Tag(name = "Internal PSP", description = "PSP operations")
@Path("/internal/psps/{" + AppConstant.PSP + "}/fdrs/{" + AppConstant.FDR + "}")
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
  @Re(action = FdrActionEnum.INTERNAL_CREATE_FLOW)
  public RestResponse<GenericResponse> internalCreate(
      @PathParam(AppConstant.PSP) String pspId,
      @PathParam(AppConstant.FDR) @Pattern(regexp = "[a-zA-Z0-9\\-_]{1,35}") String fdr,
      @NotNull @Valid CreateRequest createRequest) {

    return create(pspId, fdr, createRequest);
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
  @Path("/payments/add")
  @Re(action = FdrActionEnum.INTERNAL_ADD_PAYMENT)
  public GenericResponse internalAddPayment(
      @PathParam(AppConstant.PSP) String pspId,
      @PathParam(AppConstant.FDR) String fdr,
      @NotNull @Valid AddPaymentRequest addPaymentRequest) {
    return addPayment(pspId, fdr, addPaymentRequest);
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
  @Path("/payments/del")
  @Re(action = FdrActionEnum.INTERNAL_DELETE_PAYMENT)
  public GenericResponse internalDeletePayment(
      @PathParam(AppConstant.PSP) String pspId,
      @PathParam(AppConstant.FDR) String fdr,
      @NotNull @Valid DeletePaymentRequest deletePaymentRequest) {
    return deletePayment(pspId, fdr, deletePaymentRequest);
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
  @Path("/publish")
  @Re(action = FdrActionEnum.INTERNAL_PUBLISH)
  public GenericResponse internalPublish(
      @PathParam(AppConstant.PSP) String pspId, @PathParam(AppConstant.FDR) String fdr) {
    return publish(pspId, fdr, true);
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
  @Re(action = FdrActionEnum.INTERNAL_DELETE_FLOW)
  public GenericResponse internalDelete(
      @PathParam(AppConstant.PSP) String pspId, @PathParam(AppConstant.FDR) String fdr) {
    return delete(pspId, fdr);
  }
}
