package it.gov.pagopa.fdr.service;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.controller.model.common.response.GenericResponse;
import it.gov.pagopa.fdr.controller.model.flow.request.CreateFlowRequest;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsCreatedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsPublishedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowCreatedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowResponse;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.repository.FdrFlowRepository;
import it.gov.pagopa.fdr.repository.FdrPaymentRepository;
import it.gov.pagopa.fdr.repository.entity.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.flow.FdrFlowEntity;
import it.gov.pagopa.fdr.repository.enums.FlowStatusEnum;
import it.gov.pagopa.fdr.repository.exception.TransactionRollbackException;
import it.gov.pagopa.fdr.service.middleware.mapper.FlowMapper;
import it.gov.pagopa.fdr.service.middleware.validator.SemanticValidator;
import it.gov.pagopa.fdr.service.model.FindFlowsByFiltersArgs;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;

@ApplicationScoped
public class FlowService {

  private final Logger log;

  private final Config cachedConfig;

  private final FdrFlowRepository flowRepository;

  private final FdrPaymentRepository paymentRepository;

  private final FlowMapper flowMapper;

  public FlowService(
      Logger log,
      Config cachedConfig,
      FdrFlowRepository flowRepository,
      FdrPaymentRepository paymentRepository,
      FlowMapper flowMapper) {

    this.log = log;
    this.cachedConfig = cachedConfig;
    this.flowRepository = flowRepository;
    this.paymentRepository = paymentRepository;
    this.flowMapper = flowMapper;
  }

  @WithSpan(kind = SERVER)
  public PaginatedFlowsResponse getPaginatedPublishedFlowsForCI(FindFlowsByFiltersArgs args) {

    /*
    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
    String action = (String) MDC.get(ACTION);
    MDC.put(ORGANIZATION_ID, organizationId);
    if (null != idPsp && !idPsp.isBlank()) {
      MDC.put(PSP_ID, idPsp);
    }
     */

    String organizationId = args.getOrganizationId();
    String pspId = args.getPspId();
    long pageNumber = args.getPageNumber();
    long pageSize = args.getPageSize();

    log.debugf(
        "Executing query on published flows by organizationId [%s], pspId [%s] - pageIndex: [%s],"
            + " pageSize:[%s]",
        organizationId, pspId, pageNumber, pageSize);

    ConfigDataV1 configData = cachedConfig.getClonedCache();
    SemanticValidator.validateGetPaginatedFlowsRequestForOrganizations(configData, args);

    RepositoryPagedResult<FdrFlowEntity> paginatedResult =
        this.flowRepository.findPublishedByOrganizationIdAndOptionalPspId(
            organizationId, pspId, args.getPublishedGt(), (int) pageNumber, (int) pageSize);
    log.debugf(
        "Found [%s] entities in [%s] pages. Mapping data to final response.",
        paginatedResult.getTotalElements(), paginatedResult.getTotalPages());

    return flowMapper.toPaginatedFlowResponse(
        paginatedResult, args.getPageSize(), args.getPageNumber());
  }

  @WithSpan(kind = SERVER)
  public PaginatedFlowsPublishedResponse getPaginatedPublishedFlowsForPSP(
      FindFlowsByFiltersArgs args) {

    String organizationId = args.getOrganizationId();
    String pspId = args.getPspId();
    long pageNumber = args.getPageNumber();
    long pageSize = args.getPageSize();

    log.debugf(
        "Executing query on published flows by pspId [%s], organizationId [%s] - pageIndex: [%s],"
            + " pageSize:[%s]",
        pspId, organizationId, pageNumber, pageSize);

    ConfigDataV1 configData = cachedConfig.getClonedCache();
    SemanticValidator.validateGetPaginatedFlowsRequestForPsps(configData, args);

    RepositoryPagedResult<FdrFlowEntity> paginatedResult =
        this.flowRepository.findPublishedByPspIdAndOptionalOrganizationId(
            pspId, organizationId, args.getPublishedGt(), (int) pageNumber, (int) pageSize);
    log.debugf(
        "Found [%s] entities in [%s] pages. Mapping data to final response.",
        paginatedResult.getTotalElements(), paginatedResult.getTotalPages());

    return flowMapper.toPaginatedFlowPublishedResponse(
        paginatedResult, args.getPageSize(), args.getPageNumber());
  }

  @WithSpan(kind = SERVER)
  public PaginatedFlowsCreatedResponse getAllFlowsNotInPublishedStatus(
      FindFlowsByFiltersArgs args) {

    /*
    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
    String action = (String) MDC.get(ACTION);
    if (null != idPsp && !idPsp.isBlank()) {
      MDC.put(PSP_ID, idPsp);
    }
     */

    String pspId = args.getPspId();
    Instant createdGt = args.getCreatedGt();
    long pageSize = args.getPageSize();
    long pageNumber = args.getPageNumber();

    log.debugf("Executing query on flows by pspId [%s] created after [%s]", pspId, createdGt);

    ConfigDataV1 configData = cachedConfig.getClonedCache();
    SemanticValidator.validateOnlyPspFilters(configData, args);

    RepositoryPagedResult<FdrFlowEntity> paginatedResult =
        this.flowRepository.findUnpublishedByPspId(
            pspId, createdGt, (int) pageSize, (int) pageNumber);

    return this.flowMapper.toPaginatedFlowCreatedResponse(paginatedResult, pageSize, pageNumber);
  }

  @WithSpan(kind = SERVER)
  public SingleFlowResponse getSinglePublishedFlow(FindFlowsByFiltersArgs args) {

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

    log.debugf(
        "Executing query on published flows by organizationId [%s], pspId [%s] flowName [%s],"
            + " revision:[%s]",
        organizationId, pspId, flowName, revision);

    ConfigDataV1 configData = cachedConfig.getClonedCache();
    SemanticValidator.validateGetSingleFlowFilters(configData, args);

    FdrFlowEntity result =
        this.flowRepository.findPublishedByOrganizationIdAndPspIdAndName(
            organizationId, pspId, flowName, revision);
    if (result == null) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, flowName);
    }

    log.debugf("Entity found. Mapping data to final response.");
    return this.flowMapper.toSingleFlowResponse(result);
  }

  @WithSpan(kind = SERVER)
  public SingleFlowCreatedResponse getSingleFlowNotInPublishedStatus(FindFlowsByFiltersArgs args) {

    /*
    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
    String action = (String) MDC.get(ACTION);
    MDC.put(FDR, fdr);
    MDC.put(PSP_ID, psp);
    MDC.put(ORGANIZATION_ID, organizationId);
     */

    String organizationId = args.getOrganizationId();
    String pspId = args.getPspId();
    String flowName = args.getFlowName();

    log.debugf(
        "Executing query on unpublished flows by organizationId [%s], pspId [%s] flowName [%s]",
        organizationId, pspId, flowName);

    ConfigDataV1 configData = cachedConfig.getClonedCache();
    SemanticValidator.validateGetSingleFlowFilters(configData, args);

    FdrFlowEntity result =
        this.flowRepository.findUnpublishedByOrganizationIdAndPspIdAndName(
            organizationId, pspId, flowName);
    if (result == null) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, flowName);
    }

    log.debugf("Entity found. Mapping data to final response.");
    return this.flowMapper.toSingleFlowCreatedResponse(result);
  }

  @WithSpan(kind = SERVER)
  public GenericResponse createEmptyFlow(String pspId, String flowName, CreateFlowRequest request) {

    /*
    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
    String action = (String) MDC.get(ACTION);
    MDC.put(PSP_ID, pspId);
    MDC.put(ORGANIZATION_ID, request.getReceiver().getOrganizationId());
    MDC.put(FDR, fdr);
     */

    log.debugf(
        "Saving new flows by organizationId [%s], pspId [%s], flowName [%s]",
        request.getReceiver().getOrganizationId(), pspId, flowName);

    ConfigDataV1 configData = cachedConfig.getClonedCache();
    SemanticValidator.validateCreateFlowRequest(configData, pspId, flowName, request);

    // check if there is already another unpublished flow that is in progress
    FdrFlowEntity publishingFlow = flowRepository.findUnpublishedByPspIdAndName(pspId, flowName);
    if (publishingFlow != null) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_ALREADY_EXIST,
          flowName,
          publishingFlow.getStatus());
    }

    // retrieve the last published flow, in order to take its revision and increment it
    FdrFlowEntity lastPublishedFlow = flowRepository.findPublishedByPspIdAndName(pspId, flowName);
    Long revision = lastPublishedFlow != null ? (lastPublishedFlow.getRevision() + 1) : 1L;

    // finally, persist the newly generated entity
    FdrFlowEntity entity = flowMapper.toEntity(request, revision);
    this.flowRepository.createEntity(entity);

    /*
    reService.sendEvent(
        ReInternal.builder()
            .serviceIdentifier(AppVersionEnum.FDR003).created(Instant.now())
            .sessionId(sessionId).eventType(EventTypeEnum.INTERNAL).fdrPhysicalDelete(false)
            .fdrStatus(it.gov.pagopa.fdr.service.re.model.FdrStatusEnum.CREATED)
            .flowRead(false).fdr(fdr).pspId(pspId).organizationId(ecId)
            .revision(revision).fdrAction(FdrActionEnum.CREATE_FLOW).build());
     */

    return GenericResponse.builder().message(String.format("Fdr [%s] saved", flowName)).build();
  }

  @WithSpan(kind = SERVER)
  public GenericResponse publishFlow(String pspId, String flowName, boolean isInternalCall) {

    /*
     MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
     String action = (String) MDC.get(ACTION);
     MDC.put(FDR, fdr);
     MDC.put(PSP_ID, pspId);
    */

    log.debugf("Publishing existing flows by pspId [%s], flowName [%s]", pspId, flowName);

    ConfigDataV1 configData = cachedConfig.getClonedCache();
    SemanticValidator.validateOnlyFlowFilters(configData, pspId, flowName);

    // check if there is an unpublished flow that is in progress
    FdrFlowEntity publishingFlow = flowRepository.findUnpublishedByPspIdAndName(pspId, flowName);
    if (publishingFlow == null) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, flowName);
    }
    if (publishingFlow.getStatus() != FlowStatusEnum.INSERTED) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION,
          flowName,
          publishingFlow.getStatus());
    }

    // check if retrieved flow can be published
    SemanticValidator.validatePublishingFlow(publishingFlow);

    // update the publishing flow in order to set its status to PUBLISHED
    Instant now = Instant.now();
    publishingFlow.setUpdated(now);
    publishingFlow.setPublished(now);
    publishingFlow.setStatus(FlowStatusEnum.PUBLISHED);
    this.flowRepository.updateEntity(publishingFlow);

    // TODO do this in transactional way
    // FdrFlowToHistoryEntity flowToHistoryEntity = flowMapper.toEntity(publishingFlow,
    // isInternalCall);
    // this.flowToHistoryRepository.createEntity(flowToHistoryEntity);

    return GenericResponse.builder().message(String.format("Fdr [%s] published", flowName)).build();
  }

  @WithSpan(kind = SERVER)
  public GenericResponse deleteExistingFlow(String pspId, String flowName) {

    /*
     MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
     String action = (String) MDC.get(ACTION);
     MDC.put(FDR, fdr);
     MDC.put(PSP_ID, pspId);
    */

    log.debugf("Deleting existing flows by pspId [%s], flowName [%s]", pspId, flowName);

    ConfigDataV1 configData = cachedConfig.getClonedCache();
    SemanticValidator.validateOnlyFlowFilters(configData, pspId, flowName);

    // check if there is already another unpublished flow that is in progress
    FdrFlowEntity publishingFlow = flowRepository.findUnpublishedByPspIdAndName(pspId, flowName);
    if (publishingFlow == null) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, flowName);
    }

    // delete flow and if there are multiple payments related to it yet, delete them
    this.flowRepository.deleteEntity(publishingFlow);
    if (publishingFlow.getComputedTotPayments() > 0) {

      // try to delete payments using a transaction: if it does not end successfully, it throws an
      // accepted Exception that will cause a compensation operation in order to execute the
      // rollback.
      try {
        this.paymentRepository.deleteByFlowObjectId(publishingFlow.id);
      } catch (TransactionRollbackException e) {
        this.flowRepository.createEntity(publishingFlow);
        throw e;
      }
    }

    return GenericResponse.builder().message(String.format("Fdr [%s] deleted", flowName)).build();
  }
}
