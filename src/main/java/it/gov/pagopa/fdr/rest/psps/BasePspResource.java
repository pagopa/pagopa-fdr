package it.gov.pagopa.fdr.rest.psps;

import static it.gov.pagopa.fdr.util.MDCKeys.ACTION;
import static it.gov.pagopa.fdr.util.MDCKeys.FDR;
import static it.gov.pagopa.fdr.util.MDCKeys.ORGANIZATION_ID;
import static it.gov.pagopa.fdr.util.MDCKeys.PSP_ID;

import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.rest.model.GenericResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetAllResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetPaymentResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetResponse;
import it.gov.pagopa.fdr.rest.psps.mapper.PspsResourceServiceMapper;
import it.gov.pagopa.fdr.rest.psps.request.AddPaymentRequest;
import it.gov.pagopa.fdr.rest.psps.request.CreateRequest;
import it.gov.pagopa.fdr.rest.psps.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.rest.psps.response.GetAllCreatedResponse;
import it.gov.pagopa.fdr.rest.psps.response.GetAllPublishedResponse;
import it.gov.pagopa.fdr.rest.psps.response.GetCreatedResponse;
import it.gov.pagopa.fdr.rest.psps.validation.InternalPspValidationService;
import it.gov.pagopa.fdr.rest.psps.validation.PspsValidationService;
import it.gov.pagopa.fdr.service.dto.*;
import it.gov.pagopa.fdr.service.psps.PspsService;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response.Status;
import java.time.Instant;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;
import org.slf4j.MDC;

public abstract class BasePspResource {

  public static final String S_BY_PSP_S_WITH_FDR_S = "%s by psp:[%s] with fdr:[%s]";


  @Inject Logger log;

  @Inject Config config;

  @Inject PspsValidationService validator;

  @Inject InternalPspValidationService internalValidator;

  @Inject PspsResourceServiceMapper mapper;

  @Inject PspsService service;

  protected RestResponse<GenericResponse> baseCreate(
      String pspId, String fdr, CreateRequest createRequest) {
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

  protected GenericResponse baseAddPayment(
      String pspId, String fdr, AddPaymentRequest addPaymentRequest) {
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

  protected GenericResponse baseDeletePayment(
      String pspId, String fdr, DeletePaymentRequest deletePaymentRequest) {
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

  protected GenericResponse basePublish(String pspId, String fdr, boolean internalPublish) {
    String action = MDC.get(ACTION);
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

  protected GetAllCreatedResponse baseGetAllCreated(
      String idPsp, Instant createdGt, long pageNumber, long pageSize) {
    String action = MDC.get(ACTION);
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

  protected GetCreatedResponse baseGetCreated(String fdr, String psp, String organizationId) {
    String action = MDC.get(ACTION);
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

  protected GetPaymentResponse baseGetCreatedFdrPayment(
      String fdr, String psp, String organizationId, long pageNumber, long pageSize) {

    String action = MDC.get(ACTION);
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
        service.findPaymentByReportingFlowName(action, fdr, psp, organizationId, pageNumber, pageSize);

    return mapper.toGetPaymentResponse(fdrGetPaymentDto);
  }

  protected GetAllPublishedResponse baseGetAllPublished(
          String idPsp,
          String organizationId,
          Instant publishedGt,
          long pageNumber,
          long pageSize) {
    String action = MDC.get(ACTION);
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
            service.findAllPublished(
                    action,
                    idPsp,
                    organizationId,
                    publishedGt,
                    pageNumber,
                    pageSize);

    return mapper.toGetAllPublishedResponse(fdrAllDto);
  }

  protected GetResponse baseGetPublished(
          String psp, String fdr, Long rev, String organizationId) {
    String action = MDC.get(ACTION);
    MDC.put(PSP_ID, psp);
    MDC.put(FDR, fdr);
    MDC.put(ORGANIZATION_ID, organizationId);

    log.infof(
            AppMessageUtil.logProcess("%s by psp:[%s] with fdr=[%s], ec=[%s]"),
            action,
            psp,
            fdr,
            organizationId
            );

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    validator.validateGetPublished(action, fdr, psp, organizationId, configData);

    // get from db
    FdrGetDto fdrGetDto = service.findByReportingFlowNamePublished(action, fdr, rev, psp, organizationId);

    return mapper.toGetIdResponsePublished(fdrGetDto);
  }

  protected GetPaymentResponse baseGetFdrPaymentPublished(
          String psp,
          String fdr,
          Long rev,
          String organizationId,
          long pageNumber,
          long pageSize) {

    String action = MDC.get(ACTION);
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
            service.findPaymentByReportingFlowNamePublished(action, fdr, rev, psp, organizationId, pageNumber, pageSize);

    return mapper.toGetPaymentResponse(fdrGetPaymentDto);
  }
}
