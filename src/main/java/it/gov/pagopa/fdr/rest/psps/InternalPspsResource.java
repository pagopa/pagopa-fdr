package it.gov.pagopa.fdr.rest.psps;

import static it.gov.pagopa.fdr.util.MDCKeys.ACTION;
import static it.gov.pagopa.fdr.util.MDCKeys.FDR;
import static it.gov.pagopa.fdr.util.MDCKeys.ORGANIZATION_ID;
import static it.gov.pagopa.fdr.util.MDCKeys.PSP_ID;

import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.rest.model.GenericResponse;
import it.gov.pagopa.fdr.rest.psps.mapper.PspsResourceServiceMapper;
import it.gov.pagopa.fdr.rest.psps.request.AddPaymentRequest;
import it.gov.pagopa.fdr.rest.psps.request.CreateRequest;
import it.gov.pagopa.fdr.rest.psps.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.rest.psps.validation.PspsValidationService;
import it.gov.pagopa.fdr.service.psps.PspsService;
import it.gov.pagopa.fdr.service.re.model.FdrActionEnum;
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

@Tag(name = "Internal PSP", description = "PSP operations")
@Path("/internal/psps/{" + AppConstant.PSP + "}/fdrs/{" + AppConstant.FDR + "}")
@Consumes("application/json")
@Produces("application/json")
public class InternalPspsResource {

  public static final String S_BY_PSP_S_WITH_FDR_S = "%s by psp:[%s] with fdr:[%s]";

  @Inject Logger log;

  @Inject PspsValidationService validator;

  @Inject PspsResourceServiceMapper mapper;

  @Inject PspsService service;

  @Inject Config config;

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

    String action = MDC.get(ACTION);
    MDC.put(PSP_ID, pspId);

    String organizationId = createRequest.getReceiver().getOrganizationId();
    MDC.put(ORGANIZATION_ID, organizationId);
    MDC.put(FDR, fdr);

    log.infof(
        AppMessageUtil.logProcess("%s by psp:[%s] with fdr:[%s], organizationId:[%s]"),
        action,
        pspId,
        fdr,
        organizationId);

    ConfigDataV1 configData = config.getClonedCache();
    // validation
    validator.validateCreateFlow(action, pspId, fdr, createRequest, configData);

    // save on DB
    service.save(action, mapper.toReportingFlowDto(createRequest));

    return RestResponse.status(
        Status.CREATED,
        GenericResponse.builder().message(String.format("Fdr [%s] saved", fdr)).build());
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
    String action = MDC.get(ACTION);
    MDC.put(FDR, fdr);
    MDC.put(PSP_ID, pspId);

    log.infof(AppMessageUtil.logProcess(S_BY_PSP_S_WITH_FDR_S), action, fdr, pspId);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    validator.validateAddPayment(action, pspId, fdr, configData);

    // save on DB
    service.addPayment(action, pspId, fdr, mapper.toAddPaymentDto(addPaymentRequest));

    return GenericResponse.builder().message(String.format("Fdr [%s] payment added", fdr)).build();
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
    String action = MDC.get(ACTION);
    MDC.put(FDR, fdr);
    MDC.put(PSP_ID, pspId);

    log.infof(AppMessageUtil.logProcess(S_BY_PSP_S_WITH_FDR_S), action, fdr, pspId);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    validator.validateDeletePayment(action, pspId, fdr, configData);

    // save on DB
    service.deletePayment(action, pspId, fdr, mapper.toDeletePaymentDto(deletePaymentRequest));

    return GenericResponse.builder()
        .message(String.format("Fdr [%s] payment deleted", fdr))
        .build();
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
    String action = MDC.get(ACTION);
    MDC.put(FDR, fdr);
    MDC.put(PSP_ID, pspId);

    log.infof(AppMessageUtil.logProcess(S_BY_PSP_S_WITH_FDR_S), action, fdr, pspId);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    validator.validatePublish(action, pspId, fdr, configData);

    // save on DB
    service.internalPublishByFdr(action, pspId, fdr);

    return GenericResponse.builder().message(String.format("Fdr [%s] published", fdr)).build();
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
    String action = MDC.get(ACTION);
    MDC.put(FDR, fdr);
    MDC.put(PSP_ID, pspId);

    log.infof(AppMessageUtil.logProcess(S_BY_PSP_S_WITH_FDR_S), action, fdr, pspId);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    validator.validateDelete(action, pspId, fdr, configData);

    // save on DB
    service.deleteByFdr(action, pspId, fdr);

    return GenericResponse.builder().message(String.format("Fdr [%s] deleted", fdr)).build();
  }
}
