package it.gov.pagopa.fdr.controller.organizations;

import static it.gov.pagopa.fdr.util.MDCKeys.*;

import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowResponse;
import it.gov.pagopa.fdr.controller.model.payment.response.PaginatedPaymentsResponse;
import it.gov.pagopa.fdr.controller.organizations.mapper.OrganizationsResourceServiceMapper;
import it.gov.pagopa.fdr.controller.organizations.validation.InternalOrganizationsValidationService;
import it.gov.pagopa.fdr.controller.organizations.validation.OrganizationsValidationService;
import it.gov.pagopa.fdr.service.dto.FdrAllDto;
import it.gov.pagopa.fdr.service.dto.FdrGetDto;
import it.gov.pagopa.fdr.service.dto.FdrGetPaymentDto;
import it.gov.pagopa.fdr.service.organizations.OrganizationsService;
import it.gov.pagopa.fdr.service.re.model.EventTypeEnum;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import java.time.Instant;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;

public abstract class BaseOrganizationsResource {

  private final Config config;
  private final Logger log;

  private final OrganizationsValidationService validator;
  private final InternalOrganizationsValidationService internalValidator;

  private final OrganizationsResourceServiceMapper mapper;

  private final OrganizationsService service;

  protected BaseOrganizationsResource(
      Config config,
      Logger log,
      OrganizationsValidationService validator,
      InternalOrganizationsValidationService internalValidator,
      OrganizationsResourceServiceMapper mapper,
      OrganizationsService service) {
    this.config = config;
    this.log = log;
    this.validator = validator;
    this.internalValidator = internalValidator;
    this.mapper = mapper;
    this.service = service;
  }

  protected PaginatedFlowsResponse baseGetAll(
      String organizationId,
      String idPsp,
      Instant publishedGt,
      long pageNumber,
      long pageSize,
      boolean internalGetAll) {
    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
    String action = (String) MDC.get(ACTION);
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
    //    if (internalGetAll) {
    //      internalValidator.validateGetAllInternal(action, idPsp, configData);
    //    } else {
    validator.validateGetAllByEc(action, organizationId, idPsp, configData);
    //    }

    // get from db
    FdrAllDto fdrAllDto =
        service.find(
            action,
            internalGetAll ? null : organizationId,
            idPsp,
            publishedGt,
            pageNumber,
            pageSize);

    return mapper.toGetAllResponse(fdrAllDto);
  }

  protected SingleFlowResponse baseGet(
      String organizationId, String fdr, Long rev, String psp, boolean internalGet) {
    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
    String action = (String) MDC.get(ACTION);
    MDC.put(ORGANIZATION_ID, organizationId);
    MDC.put(FDR, fdr);
    MDC.put(PSP_ID, psp);

    log.infof(
        AppMessageUtil.logProcess("%s by ec:[%s] with fdr=[%s], psp=[%s]"),
        action,
        organizationId,
        fdr,
        psp);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    //    if (internalGet) {
    //      internalValidator.validateGetInternal(action, fdr, psp, configData);
    //    } else {
    validator.validateGet(action, fdr, organizationId, psp, configData);
    //    }

    // get from db
    FdrGetDto fdrGetDto = service.findByReportingFlowName(action, fdr, rev, psp, organizationId);

    return mapper.toGetIdResponse(fdrGetDto);
  }

  protected PaginatedPaymentsResponse baseGetFdrPayment(
      String organizationId,
      String fdr,
      Long rev,
      String psp,
      long pageNumber,
      long pageSize,
      boolean internalGetPayment) {
    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
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
    //    if (internalGetPayment) {
    //      internalValidator.validateGetPaymentInternal(action, fdr, psp, configData);
    //    } else {
    validator.validateGetPayment(action, fdr, organizationId, psp, configData);
    //    }

    // get from db
    FdrGetPaymentDto fdrGetPaymentDto =
        service.findPaymentByReportingFlowName(
            action, fdr, rev, psp, organizationId, pageNumber, pageSize);

    return mapper.toGetPaymentResponse(fdrGetPaymentDto);
  }
}
