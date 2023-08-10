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
import it.gov.pagopa.fdr.util.AppMessageUtil;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response.Status;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;
import org.slf4j.MDC;

public abstract class BasePspResource {

  public static final String S_BY_PSP_S_WITH_FDR_S = "%s by psp:[%s] with fdr:[%s]";

  @Inject Logger log;

  @Inject Config config;

  @Inject PspsValidationService validator;

  @Inject PspsResourceServiceMapper mapper;

  @Inject PspsService service;

  protected RestResponse<GenericResponse> create(
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

  protected GenericResponse addPayment(
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

  protected GenericResponse deletePayment(
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

  protected GenericResponse publish(String pspId, String fdr, boolean internalPublish) {
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

  protected GenericResponse delete(String pspId, String fdr) {
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
