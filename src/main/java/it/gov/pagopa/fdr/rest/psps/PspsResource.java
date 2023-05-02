package it.gov.pagopa.fdr.rest.psps;

import it.gov.pagopa.fdr.rest.psps.mapper.PspsResourceServiceMapper;
import it.gov.pagopa.fdr.rest.psps.request.AddPaymentRequest;
import it.gov.pagopa.fdr.rest.psps.request.CreateFlowRequest;
import it.gov.pagopa.fdr.rest.psps.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.rest.psps.validation.PspsValidationService;
import it.gov.pagopa.fdr.service.psps.PspsService;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

@Tag(name = "PSP", description = "Psp operations")
@Path("/psps/{psps}/flows")
@Consumes("application/json")
@Produces("application/json")
public class PspsResource {

  @Inject Logger log;

  @Inject PspsValidationService validator;

  @Inject PspsResourceServiceMapper mapper;

  @Inject PspsService service;

  @Operation(summary = "Create reporting flow", description = "Create new reporting flow")
  @RequestBody(content = @Content(schema = @Schema(implementation = CreateFlowRequest.class)))
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/ValidationBadRequest"),
        @APIResponse(ref = "#/components/responses/AppException400"),
        @APIResponse(ref = "#/components/responses/AppException404"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Response.class)))
      })
  @POST
  public Response createFlow(
      @PathParam("psps") String psps, @NotNull @Valid CreateFlowRequest createFlowRequest) {

    log.infof("Create reporting flow [%s]", createFlowRequest.getReportingFlowName());

    // validation
    validator.validateCreateFlow(psps, createFlowRequest);

    // save on DB
    service.save(mapper.toReportingFlowDto(createFlowRequest));

    return Response.ok().build();
  }

  @Operation(
      summary = "Add payments to reporting flow",
      description = "Add payments to reporting flow")
  @RequestBody(content = @Content(schema = @Schema(implementation = AddPaymentRequest.class)))
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/ValidationBadRequest"),
        @APIResponse(ref = "#/components/responses/AppException400"),
        @APIResponse(ref = "#/components/responses/AppException404"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Response.class)))
      })
  @PUT
  @Path("/{fdr}/payments")
  public Response addPaymentToFlow(
      @PathParam("psps") String psps,
      @PathParam("fdr") String fdr,
      @NotNull @Valid AddPaymentRequest addPaymentRequest) {

    log.infof("Add payment to reporting flow [%s]", fdr);

    // validation
    validator.validateAddPayment(psps, fdr, addPaymentRequest);

    // save on DB
    service.addPayment(psps, fdr, mapper.toAddPaymentDto(addPaymentRequest));

    return Response.ok().build();
  }

  @Operation(
      summary = "Delete payments to reporting flow",
      description = "Delete payments to reporting flow")
  @RequestBody(content = @Content(schema = @Schema(implementation = DeletePaymentRequest.class)))
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/ValidationBadRequest"),
        @APIResponse(ref = "#/components/responses/AppException400"),
        @APIResponse(ref = "#/components/responses/AppException404"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Response.class)))
      })
  @DELETE
  @Path("/{fdr}/payments")
  public Response deletePaymentToReportingFlow(
      @PathParam("psps") String psps,
      @PathParam("fdr") String fdr,
      @NotNull @Valid DeletePaymentRequest deletePaymentRequest) {

    log.infof("Delete payment to reporting flow [%s]", fdr);

    // validation
    validator.validateDeletePayment(psps, fdr, deletePaymentRequest);

    // save on DB
    service.deletePayment(psps, fdr, mapper.toDeletePaymentDto(deletePaymentRequest));

    return Response.ok().build();
  }

  @Operation(summary = "Publish reporting flow", description = "Publish reporting flow")
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/ValidationBadRequest"),
        @APIResponse(ref = "#/components/responses/AppException400"),
        @APIResponse(ref = "#/components/responses/AppException404"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Response.class)))
      })
  @POST
  @Path("/{fdr}/publish")
  public Response publishReportingFlow(
      @PathParam("psps") String psps, @PathParam("fdr") String fdr) {

    log.infof("Publish reporting flow [%s]", fdr);

    // validation
    validator.validatePublish(psps, fdr);

    // save on DB
    service.publishByReportingFlowName(psps, fdr);

    return Response.ok().build();
  }

  @Operation(summary = "Delete reporting flow", description = "Delete reporting flow")
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/ValidationBadRequest"),
        @APIResponse(ref = "#/components/responses/AppException400"),
        @APIResponse(ref = "#/components/responses/AppException404"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Response.class)))
      })
  @DELETE
  @Path("/{fdr}")
  public Response deleteReportingFlow(
      @PathParam("psps") String psps, @PathParam("fdr") String fdr) {

    log.infof("Delete reporting flow [%s]", fdr);

    // validation
    validator.validateDelete(psps, fdr);

    // save on DB
    service.deleteByReportingFlowName(psps, fdr);

    return Response.ok().build();
  }
}
