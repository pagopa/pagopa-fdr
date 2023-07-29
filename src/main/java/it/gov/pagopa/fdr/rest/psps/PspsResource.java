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
import it.gov.pagopa.fdr.service.re.model.FlowActionEnum;
import it.gov.pagopa.fdr.util.AppConstant;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import it.gov.pagopa.fdr.util.Re;
import jakarta.inject.Inject;
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
@Path("/psps/{" + AppConstant.PSP + "}/flows/{" + AppConstant.FDR + "}")
@Consumes("application/json")
@Produces("application/json")
public class PspsResource {

  public static final String S_BY_PSP_S_WITH_FDR_S = "%s by psp:[%s] with fdr:[%s]";

  @Inject Logger log;

  @Inject PspsValidationService validator;

  @Inject PspsResourceServiceMapper mapper;

  @Inject PspsService service;

  @Inject Config config;

  @Operation(
      operationId = "fdrCreate",
      summary = "Create reporting flow",
      description = "Create new reporting flow")
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
  @Re(flowName = FlowActionEnum.CREATE_FLOW)
  public RestResponse<GenericResponse> fdrCreate(
      @PathParam(AppConstant.PSP) String psp,
      @PathParam(AppConstant.FDR) @Pattern(regexp = "[a-zA-Z0-9\\-_]{1,35}") String fdr,
      @NotNull @Valid CreateFlowRequest createFlowRequest) {

    String action = MDC.get(ACTION);
    MDC.put(PSP_ID, psp);

    String ecId = createFlowRequest.getReceiver().getEcId();
    MDC.put(EC_ID, ecId);
    MDC.put(FLOW_NAME, fdr);

    log.infof(
        AppMessageUtil.logProcess("%s by psp:[%s] with flowName:[%s], ecId:[%s]"),
        action,
        psp,
        fdr,
        ecId);

    ConfigDataV1 configData = config.getClonedCache();
    // validation
    validator.validateCreateFlow(action, psp, fdr, createFlowRequest, configData);

    // save on DB
    service.save(action, mapper.toReportingFlowDto(createFlowRequest));

    return RestResponse.status(
        Status.CREATED,
        GenericResponse.builder().message(String.format("Flow [%s] saved", fdr)).build());
  }

  @Operation(
      operationId = "addFdrPayment",
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
  @Path("/payments/add")
  @Re(flowName = FlowActionEnum.ADD_PAYMENT)
  public GenericResponse addFdrPayment(
      @PathParam(AppConstant.PSP) String psp,
      @PathParam(AppConstant.FDR) String fdr,
      @NotNull @Valid AddPaymentRequest addPaymentRequest) {
    String action = MDC.get(ACTION);
    MDC.put(FLOW_NAME, fdr);
    MDC.put(PSP_ID, psp);

    log.infof(AppMessageUtil.logProcess(S_BY_PSP_S_WITH_FDR_S), action, fdr, psp);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    validator.validateAddPayment(action, psp, fdr, configData);

    // save on DB
    service.addPayment(action, psp, fdr, mapper.toAddPaymentDto(addPaymentRequest));

    return GenericResponse.builder().message(String.format("Flow [%s] payment added", fdr)).build();
  }

  @Operation(
      operationId = "deleteFdrPayment",
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
  @Path("/payments/del")
  @Re(flowName = FlowActionEnum.DELETE_PAYMENT)
  public GenericResponse deleteFdrPayment(
      @PathParam(AppConstant.PSP) String psp,
      @PathParam(AppConstant.FDR) String fdr,
      @NotNull @Valid DeletePaymentRequest deletePaymentRequest) {
    String action = MDC.get(ACTION);
    MDC.put(FLOW_NAME, fdr);
    MDC.put(PSP_ID, psp);

    log.infof(AppMessageUtil.logProcess(S_BY_PSP_S_WITH_FDR_S), action, fdr, psp);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    validator.validateDeletePayment(action, psp, fdr, configData);

    // save on DB
    service.deletePayment(action, psp, fdr, mapper.toDeletePaymentDto(deletePaymentRequest));

    return GenericResponse.builder()
        .message(String.format("Flow [%s] payment deleted", fdr))
        .build();
  }

  @Operation(
      operationId = "publishFdr",
      summary = "Publish reporting flow",
      description = "Publish reporting flow")
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
  @Re(flowName = FlowActionEnum.PUBLISH)
  public GenericResponse publishFdr(
      @PathParam(AppConstant.PSP) String psp, @PathParam(AppConstant.FDR) String fdr) {
    String action = MDC.get(ACTION);
    MDC.put(FLOW_NAME, fdr);
    MDC.put(PSP_ID, psp);

    log.infof(AppMessageUtil.logProcess(S_BY_PSP_S_WITH_FDR_S), action, fdr, psp);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    validator.validatePublish(action, psp, fdr, configData);

    // save on DB
    service.publishByReportingFlowName(action, psp, fdr);

    return GenericResponse.builder().message(String.format("Flow [%s] published", fdr)).build();
  }

  @Operation(
      operationId = "deleteFdr",
      summary = "Delete reporting flow",
      description = "Delete reporting flow")
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
  @Re(flowName = FlowActionEnum.DELETE_FLOW)
  public GenericResponse deleteFdr(
      @PathParam(AppConstant.PSP) String psp, @PathParam(AppConstant.FDR) String fdr) {
    String action = MDC.get(ACTION);
    MDC.put(FLOW_NAME, fdr);
    MDC.put(PSP_ID, psp);

    log.infof(AppMessageUtil.logProcess(S_BY_PSP_S_WITH_FDR_S), action, fdr, psp);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    validator.validateDelete(action, psp, fdr, configData);

    // save on DB
    service.deleteByReportingFlowName(action, psp, fdr);

    return GenericResponse.builder().message(String.format("Flow [%s] deleted", fdr)).build();
  }
}
