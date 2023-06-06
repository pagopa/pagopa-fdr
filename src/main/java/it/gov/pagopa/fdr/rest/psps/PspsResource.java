package it.gov.pagopa.fdr.rest.psps;

import static it.gov.pagopa.fdr.util.MDCKeys.FLOW_NAME;
import static it.gov.pagopa.fdr.util.MDCKeys.PSP_ID;
import static it.gov.pagopa.fdr.util.MDCKeys.TRX_ID;

import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.rest.model.GenericResponse;
import it.gov.pagopa.fdr.rest.psps.mapper.PspsResourceServiceMapper;
import it.gov.pagopa.fdr.rest.psps.request.AddPaymentRequest;
import it.gov.pagopa.fdr.rest.psps.request.CreateFlowRequest;
import it.gov.pagopa.fdr.rest.psps.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.rest.psps.validation.PspsValidationService;
import it.gov.pagopa.fdr.service.psps.PspsService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;
import org.slf4j.MDC;

@Tag(name = "PSP", description = "Psp operations")
@Path("/psps/{psp}/flows")
@Consumes("application/json")
@Produces("application/json")
public class PspsResource {

  @Inject Logger log;

  @Inject PspsValidationService validator;

  @Inject PspsResourceServiceMapper mapper;

  @Inject PspsService service;

  @Inject Config config;

  @Operation(summary = "Create reporting flow", description = "Create new reporting flow")
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
  @POST
  public RestResponse<GenericResponse> createFlow(
      @PathParam("psp") String psp, @NotNull @Valid CreateFlowRequest createFlowRequest) {
    MDC.put(TRX_ID, UUID.randomUUID().toString());

    String flowName = createFlowRequest.getReportingFlowName();

    MDC.put(FLOW_NAME, flowName);
    MDC.put(PSP_ID, psp);

    log.infof("Create reporting flow [%s]", flowName);

    ConfigDataV1 configData = config.getClonedCache();
    // validation
    validator.validateCreateFlow(psp, createFlowRequest, configData);

    // save on DB
    service.save(mapper.toReportingFlowDto(createFlowRequest));

    MDC.clear();
    return RestResponse.status(
        Status.CREATED,
        GenericResponse.builder().message(String.format("Flow [%s] saved", flowName)).build());
  }

  @Operation(
      summary = "Add payments to reporting flow",
      description = "Add payments to reporting flow")
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
  @Path("/{fdr}/payments/add")
  public GenericResponse addPaymentToFlow(
      @PathParam("psp") String psp,
      @PathParam("fdr") String fdr,
      @NotNull @Valid AddPaymentRequest addPaymentRequest) {
    MDC.put(TRX_ID, UUID.randomUUID().toString());
    MDC.put(FLOW_NAME, fdr);
    MDC.put(PSP_ID, psp);

    log.infof("Add payment to reporting flow [%s]", fdr);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    validator.validateAddPayment(psp, fdr, configData);

    // save on DB
    service.addPayment(psp, fdr, mapper.toAddPaymentDto(addPaymentRequest));

    MDC.clear();
    return GenericResponse.builder().message(String.format("Flow [%s] payment added", fdr)).build();
  }

  @Operation(
      summary = "Delete payments to reporting flow",
      description = "Delete payments to reporting flow")
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
  @Path("/{fdr}/payments/del")
  public GenericResponse deletePaymentToReportingFlow(
      @PathParam("psp") String psp,
      @PathParam("fdr") String fdr,
      @NotNull @Valid DeletePaymentRequest deletePaymentRequest) {
    MDC.put(TRX_ID, UUID.randomUUID().toString());
    MDC.put(FLOW_NAME, fdr);
    MDC.put(PSP_ID, psp);

    log.infof("Delete payment to reporting flow [%s]", fdr);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    validator.validateDeletePayment(psp, fdr, configData);

    // save on DB
    service.deletePayment(psp, fdr, mapper.toDeletePaymentDto(deletePaymentRequest));

    MDC.clear();
    return GenericResponse.builder()
        .message(String.format("Flow [%s] payment deleted", fdr))
        .build();
  }

  @Operation(summary = "Publish reporting flow", description = "Publish reporting flow")
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
  @Path("/{fdr}/publish")
  public GenericResponse publishReportingFlow(
      @PathParam("psp") String psp, @PathParam("fdr") String fdr) {
    MDC.put(TRX_ID, UUID.randomUUID().toString());
    MDC.put(FLOW_NAME, fdr);
    MDC.put(PSP_ID, psp);
    log.infof("Publish reporting flow [%s]", fdr);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    validator.validatePublish(psp, fdr, configData);

    // save on DB
    service.publishByReportingFlowName(psp, fdr);

    return GenericResponse.builder().message(String.format("Flow [%s] published", fdr)).build();
  }

  @Operation(summary = "Delete reporting flow", description = "Delete reporting flow")
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
  @Path("/{fdr}")
  public GenericResponse deleteReportingFlow(
      @PathParam("psp") String psp, @PathParam("fdr") String fdr) {
    MDC.put(TRX_ID, UUID.randomUUID().toString());
    MDC.put(FLOW_NAME, fdr);
    MDC.put(PSP_ID, psp);

    log.infof("Delete reporting flow [%s]", fdr);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    validator.validateDelete(psp, fdr, configData);

    // save on DB
    service.deleteByReportingFlowName(psp, fdr);

    String responseMessage = String.format("Flow [%s] deleted", fdr);
    log.infof(responseMessage);
    MDC.clear();
    return GenericResponse.builder().message(responseMessage).build();
  }
}
