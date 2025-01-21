package it.gov.pagopa.fdr.service;

import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.controller.model.common.response.GenericResponse;
import it.gov.pagopa.fdr.controller.model.payment.request.AddPaymentRequest;
import it.gov.pagopa.fdr.controller.model.payment.response.PaginatedPaymentsResponse;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.repository.FdrFlowRepository;
import it.gov.pagopa.fdr.repository.FdrPaymentRepository;
import it.gov.pagopa.fdr.repository.entity.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.flow.projection.FdrFlowIdProjection;
import it.gov.pagopa.fdr.repository.entity.payment.FdrPaymentEntity;
import it.gov.pagopa.fdr.repository.enums.FlowStatusEnum;
import it.gov.pagopa.fdr.service.middleware.mapper.PaymentMapper;
import it.gov.pagopa.fdr.service.middleware.validator.SemanticValidator;
import it.gov.pagopa.fdr.service.model.FindFlowsByFiltersArgs;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;

@ApplicationScoped
public class PaymentService {

  private Logger log;

  private final Config cachedConfig;

  private final FdrFlowRepository flowRepository;

  private final FdrPaymentRepository paymentRepository;

  private final PaymentMapper paymentMapper;

  public PaymentService(
      Logger log,
      Config cachedConfig,
      FdrFlowRepository flowRepository,
      FdrPaymentRepository paymentRepository,
      PaymentMapper paymentMapper) {

    this.log = log;
    this.cachedConfig = cachedConfig;
    this.flowRepository = flowRepository;
    this.paymentRepository = paymentRepository;
    this.paymentMapper = paymentMapper;
  }

  public PaginatedPaymentsResponse getPaymentsFromPublishedFlow(FindFlowsByFiltersArgs args) {

    /*
    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
    String action = (String) MDC.get(ACTION);
    MDC.put(ORGANIZATION_ID, organizationId);
    MDC.put(FDR, fdr);
    MDC.put(PSP_ID, psp);
     */

    String organizationId = args.getOrganizationId();
    String pspId = args.getPspId();
    String flowName = args.getFlowName();
    long revision = args.getRevision();
    long pageNumber = args.getPageNumber();
    long pageSize = args.getPageSize();

    log.debugf(
        "Executing query on payments related on flow by organizationId [%s], pspId [%s], flowName"
            + " [%s], revision:[%s]",
        organizationId, pspId, flowName, revision);

    ConfigDataV1 configData = cachedConfig.getClonedCache();
    SemanticValidator.validateGetPaymentsFromPublishedFlow(configData, args);

    FdrFlowIdProjection flowIdProjection =
        this.flowRepository.findIdByOrganizationIdAndPspIdAndName(
            organizationId, pspId, flowName, revision, FlowStatusEnum.PUBLISHED);
    if (flowIdProjection == null) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, flowName);
    }

    RepositoryPagedResult<FdrPaymentEntity> paginatedResult =
        this.paymentRepository.findByFlowObjectId(
            flowIdProjection.getId(), (int) pageNumber, (int) pageSize);

    return paymentMapper.toPaginatedPaymentsResponse(paginatedResult, pageSize, pageNumber);
  }

  @Transactional
  public GenericResponse addPaymentToExistingFlow(
      String pspId, String flowName, AddPaymentRequest request) {

    /*
    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
    String action = (String) MDC.get(ACTION);
    MDC.put(FDR, fdr);
    MDC.put(PSP_ID, pspId);
     */

    log.debugf(
        "Adding [%s] new payments on flow [%s], pspId [%s]",
        request.getPayments().size(), flowName, pspId);

    return GenericResponse.builder().message(String.format("Fdr [%s] saved", flowName)).build();
  }
}
