package it.gov.pagopa.fdr.controller.psps;

import static it.gov.pagopa.fdr.util.MDCKeys.*;

import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.controller.model.common.response.GenericResponse;
import it.gov.pagopa.fdr.controller.model.flow.request.CreateFlowRequest;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsCreatedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsPublishedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowCreatedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowResponse;
import it.gov.pagopa.fdr.controller.model.payment.request.AddPaymentRequest;
import it.gov.pagopa.fdr.controller.model.payment.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.controller.model.payment.response.PaginatedPaymentsResponse;
import it.gov.pagopa.fdr.controller.psps.mapper.PspsResourceServiceMapper;
import it.gov.pagopa.fdr.controller.psps.validation.InternalPspValidationService;
import it.gov.pagopa.fdr.controller.psps.validation.PspsValidationService;
import it.gov.pagopa.fdr.service.dto.*;
import it.gov.pagopa.fdr.service.psps.PspsService;
import it.gov.pagopa.fdr.service.re.model.EventTypeEnum;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import jakarta.ws.rs.core.Response.Status;
import java.time.Instant;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;
import org.jboss.resteasy.reactive.RestResponse;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;

public abstract class BasePspResource {

  public static final String S_BY_PSP_S_WITH_FDR_S = "%s by psp:[%s] with fdr:[%s]";

  private final Logger log;

  private final Config config;

  private final PspsValidationService validator;

  private final InternalPspValidationService internalValidator;

  private final PspsResourceServiceMapper mapper;

  private final PspsService service;

  protected BasePspResource(
      Logger log,
      Config config,
      PspsValidationService validator,
      InternalPspValidationService internalValidator,
      PspsResourceServiceMapper mapper,
      PspsService service) {
    this.log = log;
    this.config = config;
    this.validator = validator;
    this.internalValidator = internalValidator;
    this.mapper = mapper;
    this.service = service;
  }

  protected RestResponse<GenericResponse> baseCreate(
      String pspId, String fdr, CreateFlowRequest createRequest) {
    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
    String action = (String) MDC.get(ACTION);
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

  protected GenericResponse baseAddPayment(
      String pspId, String fdr, AddPaymentRequest addPaymentRequest) {
    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
    String action = (String) MDC.get(ACTION);
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

  protected GenericResponse baseDeletePayment(
      String pspId, String fdr, DeletePaymentRequest deletePaymentRequest) {
    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
    String action = (String) MDC.get(ACTION);
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

  protected GenericResponse basePublish(String pspId, String fdr, boolean internalPublish) {
    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
    String action = (String) MDC.get(ACTION);
    MDC.put(FDR, fdr);
    MDC.put(PSP_ID, pspId);

    log.infof(AppMessageUtil.logProcess(S_BY_PSP_S_WITH_FDR_S), action, fdr, pspId);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    validator.validatePublish(action, pspId, fdr, configData);

    // save on DB e pubblica su coda solo se non iternal
    service.publishByFdr(action, pspId, fdr, internalPublish);

    return GenericResponse.builder().message(String.format("Fdr [%s] published", fdr)).build();
  }

  protected GenericResponse baseDelete(String pspId, String fdr) {
    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
    String action = (String) MDC.get(ACTION);
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

  protected PaginatedFlowsCreatedResponse baseGetAllCreated(
      String idPsp, Instant createdGt, long pageNumber, long pageSize) {
    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
    String action = (String) MDC.get(ACTION);
    if (null != idPsp && !idPsp.isBlank()) {
      MDC.put(PSP_ID, idPsp);
    }

    log.infof(
        AppMessageUtil.logProcess("%s by psp:[%s] - page:[%s], pageSize:[%s]"),
        action,
        idPsp,
        pageNumber,
        pageSize);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    internalValidator.validateGetAllInternal(action, idPsp, configData);

    // get from db
    FdrAllCreatedDto fdrAllDto = service.find(action, idPsp, createdGt, pageNumber, pageSize);

    return mapper.toGetAllResponse(fdrAllDto);
  }

  protected SingleFlowCreatedResponse baseGetCreated(
      String fdr, String psp, String organizationId) {
    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
    String action = (String) MDC.get(ACTION);
    MDC.put(FDR, fdr);
    MDC.put(PSP_ID, psp);
    MDC.put(ORGANIZATION_ID, organizationId);

    log.infof(AppMessageUtil.logProcess("%s by fdr=[%s], psp=[%s]"), action, fdr, psp);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    internalValidator.validateGetInternal(action, fdr, psp, organizationId, configData);

    // get from db
    FdrGetCreatedDto fdrGetDto = service.findByReportingFlowName(action, fdr, psp, organizationId);

    return mapper.toGetCreatedResponse(fdrGetDto);
  }

  protected PaginatedPaymentsResponse baseGetCreatedFdrPayment(
      String fdr, String psp, String organizationId, long pageNumber, long pageSize) {
    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
    String action = (String) MDC.get(ACTION);
    MDC.put(FDR, fdr);
    MDC.put(PSP_ID, psp);
    MDC.put(ORGANIZATION_ID, organizationId);

    log.infof(
        AppMessageUtil.logProcess("%s with id:[%s] - page:[%s], pageSize:[%s]"),
        action,
        fdr,
        pageNumber,
        pageSize);

    ConfigDataV1 configData = config.getClonedCache();
    // validation
    internalValidator.validateGetPaymentInternal(action, fdr, psp, organizationId, configData);

    // get from db
    FdrGetPaymentDto fdrGetPaymentDto =
        service.findPaymentByReportingFlowName(
            action, fdr, psp, organizationId, pageNumber, pageSize);

    return mapper.toGetPaymentResponse(fdrGetPaymentDto);
  }

  protected PaginatedFlowsPublishedResponse baseGetAllPublished(
      String idPsp, String organizationId, Instant publishedGt, long pageNumber, long pageSize) {
    String action = (String) MDC.get(ACTION);
    MDC.put(PSP_ID, idPsp);
    if (null != organizationId && !organizationId.isBlank()) {
      MDC.put(ORGANIZATION_ID, organizationId);
    }

    log.infof(
        AppMessageUtil.logProcess("%s by psp:[%s] with ec:[%s] - page:[%s], pageSize:[%s]"),
        action,
        idPsp,
        organizationId,
        pageNumber,
        pageSize);

    ConfigDataV1 configData = config.getClonedCache();
    // validation
    validator.validateGetAllPublished(action, idPsp, organizationId, configData);

    // get from db
    FdrAllPublishedDto fdrAllDto =
        service.findAllPublished(action, idPsp, organizationId, publishedGt, pageNumber, pageSize);

    return mapper.toGetAllPublishedResponse(fdrAllDto);
  }

  protected SingleFlowResponse baseGetPublished(
      String psp, String fdr, Long rev, String organizationId) {
    String action = (String) MDC.get(ACTION);
    MDC.put(PSP_ID, psp);
    MDC.put(FDR, fdr);
    MDC.put(ORGANIZATION_ID, organizationId);

    log.infof(
        AppMessageUtil.logProcess("%s by psp:[%s] with fdr=[%s], ec=[%s]"),
        action,
        psp,
        fdr,
        organizationId);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    validator.validateGetPublished(action, fdr, psp, organizationId, configData);

    // get from db
    FdrGetDto fdrGetDto =
        service.findByReportingFlowNamePublished(action, fdr, rev, psp, organizationId);

    return mapper.toGetIdResponsePublished(fdrGetDto);
  }

  protected PaginatedPaymentsResponse baseGetFdrPaymentPublished(
      String psp, String fdr, Long rev, String organizationId, long pageNumber, long pageSize) {

    String action = (String) MDC.get(ACTION);
    MDC.put(ORGANIZATION_ID, organizationId);
    MDC.put(FDR, fdr);
    MDC.put(PSP_ID, psp);

    log.infof(
        AppMessageUtil.logProcess("%s with id:[%s] - page:[%s], pageSize:[%s]"),
        action,
        fdr,
        pageNumber,
        pageSize);

    ConfigDataV1 configData = config.getClonedCache();
    // validation
    validator.validateGetPaymentPublished(action, fdr, psp, organizationId, configData);

    // get from db
    FdrGetPaymentDto fdrGetPaymentDto =
        service.findPaymentByReportingFlowNamePublished(
            action, fdr, rev, psp, organizationId, pageNumber, pageSize);

    return mapper.toGetPaymentResponse(fdrGetPaymentDto);
  }
}
