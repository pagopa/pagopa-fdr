package it.gov.pagopa.fdr.rest.organizations;

import static it.gov.pagopa.fdr.util.MDCKeys.*;

import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.rest.organizations.mapper.OrganizationsResourceServiceMapper;
import it.gov.pagopa.fdr.rest.organizations.response.GetAllResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetPaymentResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetResponse;
import it.gov.pagopa.fdr.rest.organizations.validation.InternalOrganizationsValidationService;
import it.gov.pagopa.fdr.rest.organizations.validation.OrganizationsValidationService;
import it.gov.pagopa.fdr.service.dto.FdrAllDto;
import it.gov.pagopa.fdr.service.dto.FdrGetDto;
import it.gov.pagopa.fdr.service.dto.FdrGetPaymentDto;
import it.gov.pagopa.fdr.service.organizations.OrganizationsService;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;
import org.slf4j.MDC;

public abstract class BaseOrganizationsResource {

  @Inject Config config;
  @Inject Logger log;

  @Inject OrganizationsValidationService validator;
  @Inject InternalOrganizationsValidationService internalValidator;

  @Inject OrganizationsResourceServiceMapper mapper;

  @Inject OrganizationsService service;

  protected GetAllResponse getAll(
      String organizationId, String idPsp, long pageNumber, long pageSize, boolean internalGetAll) {
    String action = MDC.get(ACTION);
    MDC.put(ORGANIZATION_ID, organizationId);
    if (null != idPsp && !idPsp.isBlank()) {
      MDC.put(PSP_ID, idPsp);
    }

    log.infof(
        AppMessageUtil.logProcess("%s by ec:[%s] with psp:[%s] - page:[%s], pageSize:[%s]"),
        action,
        organizationId,
        idPsp,
        pageNumber,
        pageSize);

    ConfigDataV1 configData = config.getClonedCache();
    // validation
    if (internalGetAll) {
      internalValidator.validateGetAllInternal(action, idPsp, configData);
    } else {
      validator.validateGetAllByEc(action, organizationId, idPsp, configData);
    }

    // get from db
    FdrAllDto fdrAllDto =
        service.find(action, organizationId, internalGetAll ? null : idPsp, pageNumber, pageSize);

    return mapper.toGetAllResponse(fdrAllDto);
  }

  protected GetResponse get(
      String organizationId, String fdr, Long rev, String psp, boolean internalGet) {
    String action = MDC.get(ACTION);
    MDC.put(ORGANIZATION_ID, organizationId);
    MDC.put(FDR, fdr);
    MDC.put(PSP_ID, psp);

    log.infof(
        AppMessageUtil.logProcess("%s by ec:[%s] with fdr=[%s], psp=[%s]"),
        action,
        fdr,
        organizationId,
        psp);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    if (internalGet) {
      internalValidator.validateGetInternal(action, fdr, psp, configData);
    } else {
      validator.validateGet(action, fdr, organizationId, psp, configData);
    }

    // get from db
    FdrGetDto fdrGetDto = service.findByReportingFlowName(action, fdr, rev, psp);

    return mapper.toGetIdResponse(fdrGetDto);
  }

  protected GetPaymentResponse getFdrPayment(
      String organizationId,
      String fdr,
      Long rev,
      String psp,
      long pageNumber,
      long pageSize,
      boolean internalGetPayment) {

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
    if (internalGetPayment) {
      internalValidator.validateGetPaymentInternal(action, fdr, psp, configData);
    } else {
      validator.validateGetPayment(action, fdr, organizationId, psp, configData);
    }

    // get from db
    FdrGetPaymentDto fdrGetPaymentDto =
        service.findPaymentByReportingFlowName(action, fdr, rev, psp, pageNumber, pageSize);

    return mapper.toGetPaymentResponse(fdrGetPaymentDto);
  }
}
