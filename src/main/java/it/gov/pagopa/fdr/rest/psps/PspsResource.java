package it.gov.pagopa.fdr.rest.psps;

import static it.gov.pagopa.fdr.util.MDCKeys.ACTION;
import static it.gov.pagopa.fdr.util.MDCKeys.EC_ID;
import static it.gov.pagopa.fdr.util.MDCKeys.FLOW_NAME;
import static it.gov.pagopa.fdr.util.MDCKeys.PSP_ID;

import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.rest.model.GenericResponse;
import it.gov.pagopa.fdr.rest.psps.mapper.PspsResourceServiceMapper;
import it.gov.pagopa.fdr.rest.psps.request.AddPaymentRequest;
import it.gov.pagopa.fdr.rest.psps.request.CreateFlowRequest;
import it.gov.pagopa.fdr.rest.psps.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.rest.psps.validation.PspsValidationService;
import it.gov.pagopa.fdr.service.psps.PspsService;
import it.gov.pagopa.fdr.util.AppConstant;
import it.gov.pagopa.fdr.util.AppMessageUtil;
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

  private static final String CREATE_FLOW = "createFlow";
  private static final String ADD_PAYMENT_FLOW = "addPaymentFlow";
  private static final String DELETE_PAYMENT_FLOW = "deletePaymentFlow";
  private static final String PUBLISH_REPORTING_FLOW = "publishReportingFlow";
  private static final String DELETE_REPORTING_FLOW = "deleteReportingFlow";

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
      @PathParam(AppConstant.PATH_PARAM_PSP) String psp,
      @NotNull @Valid CreateFlowRequest createFlowRequest) {
    MDC.put(ACTION, CREATE_FLOW);
    MDC.put(PSP_ID, psp);

    String flowName = createFlowRequest.getReportingFlowName();
    String ecId = createFlowRequest.getReceiver().getEcId();
    MDC.put(EC_ID, ecId);
    MDC.put(FLOW_NAME, flowName);

    log.infof(
        AppMessageUtil.logProcess("%s by psp:[%s] with flowName:[%s], ecId:[%s]"),
        CREATE_FLOW,
        psp,
        flowName,
        ecId);

    ConfigDataV1 configData = config.getClonedCache();
    // validation
    validator.validateCreateFlow(CREATE_FLOW, psp, createFlowRequest, configData);

    // save on DB
    service.save(CREATE_FLOW, mapper.toReportingFlowDto(createFlowRequest));

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
  public GenericResponse addPaymenTFlow(
      @PathParam(AppConstant.PATH_PARAM_PSP) String psp,
      @PathParam(AppConstant.PATH_PARAM_FDR) String fdr,
      @NotNull @Valid AddPaymentRequest addPaymentRequest) {
    MDC.put(ACTION, ADD_PAYMENT_FLOW);
    MDC.put(FLOW_NAME, fdr);
    MDC.put(PSP_ID, psp);

    log.infof(
        AppMessageUtil.logProcess("%s by psp:[%s] with fdr:[%s]"), ADD_PAYMENT_FLOW, fdr, psp);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    validator.validateAddPayment(ADD_PAYMENT_FLOW, psp, fdr, configData);

    // save on DB
    service.addPayment(ADD_PAYMENT_FLOW, psp, fdr, mapper.toAddPaymentDto(addPaymentRequest));

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
  public GenericResponse deletePaymentFlow(
      @PathParam(AppConstant.PATH_PARAM_PSP) String psp,
      @PathParam(AppConstant.PATH_PARAM_FDR) String fdr,
      @NotNull @Valid DeletePaymentRequest deletePaymentRequest) {
    MDC.put(ACTION, DELETE_PAYMENT_FLOW);
    MDC.put(FLOW_NAME, fdr);
    MDC.put(PSP_ID, psp);

    log.infof(
        AppMessageUtil.logProcess("%s by psp:[%s] with fdr:[%s]"), DELETE_PAYMENT_FLOW, fdr, psp);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    validator.validateDeletePayment(DELETE_PAYMENT_FLOW, psp, fdr, configData);

    // save on DB
    service.deletePayment(
        DELETE_PAYMENT_FLOW, psp, fdr, mapper.toDeletePaymentDto(deletePaymentRequest));

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
      @PathParam(AppConstant.PATH_PARAM_PSP) String psp,
      @PathParam(AppConstant.PATH_PARAM_FDR) String fdr) {
    MDC.put(ACTION, PUBLISH_REPORTING_FLOW);
    MDC.put(FLOW_NAME, fdr);
    MDC.put(PSP_ID, psp);

    log.infof(
        AppMessageUtil.logProcess("%s by psp:[%s] with fdr:[%s]"),
        PUBLISH_REPORTING_FLOW,
        fdr,
        psp);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    validator.validatePublish(PUBLISH_REPORTING_FLOW, psp, fdr, configData);

    // save on DB
    service.publishByReportingFlowName(PUBLISH_REPORTING_FLOW, psp, fdr);

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
      @PathParam(AppConstant.PATH_PARAM_PSP) String psp,
      @PathParam(AppConstant.PATH_PARAM_FDR) String fdr) {
    MDC.put(ACTION, DELETE_REPORTING_FLOW);
    MDC.put(FLOW_NAME, fdr);
    MDC.put(PSP_ID, psp);

    log.infof(
        AppMessageUtil.logProcess("%s by psp:[%s] with fdr:[%s]"), DELETE_REPORTING_FLOW, fdr, psp);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    validator.validateDelete(DELETE_REPORTING_FLOW, psp, fdr, configData);

    // save on DB
    service.deleteByReportingFlowName(DELETE_REPORTING_FLOW, psp, fdr);

    return GenericResponse.builder().message(String.format("Flow [%s] deleted", fdr)).build();
  }
}
