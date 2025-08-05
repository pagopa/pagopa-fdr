package it.gov.pagopa.fdr.service;

import static io.opentelemetry.api.trace.SpanKind.SERVER;
import static it.gov.pagopa.fdr.util.constant.MDCKeys.IS_RE_ENABLED_FOR_THIS_CALL;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.controller.model.common.response.GenericResponse;
import it.gov.pagopa.fdr.controller.model.flow.request.CreateFlowRequest;
import it.gov.pagopa.fdr.controller.model.flow.response.*;
import it.gov.pagopa.fdr.repository.FlowRepository;
import it.gov.pagopa.fdr.repository.FlowToHistoryRepository;
import it.gov.pagopa.fdr.repository.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.FlowEntity;
import it.gov.pagopa.fdr.repository.entity.FlowToHistoryEntity;
import it.gov.pagopa.fdr.repository.enums.FlowStatusEnum;
import it.gov.pagopa.fdr.service.middleware.mapper.FlowMapper;
import it.gov.pagopa.fdr.service.middleware.mapper.FlowToHistoryMapper;
import it.gov.pagopa.fdr.service.middleware.validator.SemanticValidator;
import it.gov.pagopa.fdr.service.model.arguments.FindFlowsByFiltersArgs;
import it.gov.pagopa.fdr.service.model.re.*;
import it.gov.pagopa.fdr.util.constant.MDCKeys;
import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.error.exception.common.AppException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;
import org.slf4j.MDC;

@ApplicationScoped
public class FlowService {

  private final Logger log;

  private final ReService reService;

  private final Config cachedConfig;

  private final FlowRepository flowRepository;

  private final FlowToHistoryRepository flowToHistoryRepository;

  private final FlowMapper flowMapper;

  private final FlowToHistoryMapper flowToHistoryMapper;

  public FlowService(
      Logger log,
      ReService reService,
      Config cachedConfig,
      FlowRepository flowRepository,
      FlowToHistoryRepository flowToHistoryRepository,
      FlowMapper flowMapper,
      FlowToHistoryMapper flowToHistoryMapper) {

    this.log = log;
    this.reService = reService;
    this.cachedConfig = cachedConfig;
    this.flowRepository = flowRepository;
    this.flowToHistoryRepository = flowToHistoryRepository;
    this.flowMapper = flowMapper;
    this.flowToHistoryMapper = flowToHistoryMapper;
  }

  @WithSpan(kind = SERVER)
  public PaginatedFlowsResponse getPaginatedPublishedFlowsForCI(FindFlowsByFiltersArgs args) {

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

    RepositoryPagedResult<FlowEntity> paginatedResult =
        this.flowRepository.findLatestPublishedByOrganizationIdAndOptionalPspId(
            organizationId,
            pspId,
            args.getPublishedGt(),
            args.getFlowDate(),
            (int) pageNumber,
            (int) pageSize);
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

    RepositoryPagedResult<FlowEntity> paginatedResult =
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

    String pspId = args.getPspId();
    Instant createdGt = args.getCreatedGt();
    long pageSize = args.getPageSize();
    long pageNumber = args.getPageNumber();

    log.debugf("Executing query on flows by pspId [%s] created after [%s]", pspId, createdGt);

    ConfigDataV1 configData = cachedConfig.getClonedCache();
    SemanticValidator.validateOnlyPspFilters(configData, args);

    RepositoryPagedResult<FlowEntity> paginatedResult =
        this.flowRepository.findUnpublishedByPspId(
            pspId, createdGt, (int) pageNumber, (int) pageSize);

    return this.flowMapper.toPaginatedFlowCreatedResponse(paginatedResult, pageSize, pageNumber);
  }

  @WithSpan(kind = SERVER)
  public SingleFlowResponse getSinglePublishedFlow(FindFlowsByFiltersArgs args) {

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

    Optional<FlowEntity> result =
        this.flowRepository.findPublishedByOrganizationIdAndPspIdAndName(
            organizationId, pspId, flowName, revision);
    if (result.isEmpty()) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, flowName);
    }

    log.debugf("Entity found. Mapping data to final response.");
    return this.flowMapper.toSingleFlowResponse(result.get());
  }

  @WithSpan(kind = SERVER)
  public SingleFlowCreatedResponse getSingleFlowNotInPublishedStatus(FindFlowsByFiltersArgs args) {

    String organizationId = args.getOrganizationId();
    String pspId = args.getPspId();
    String flowName = args.getFlowName();

    log.debugf(
        "Executing query on unpublished flows by organizationId [%s], pspId [%s] flowName [%s]",
        organizationId, pspId, flowName);

    ConfigDataV1 configData = cachedConfig.getClonedCache();
    SemanticValidator.validateGetSingleFlowFilters(configData, args);

    Optional<FlowEntity> result =
        this.flowRepository.findUnpublishedByOrganizationIdAndPspIdAndName(
            organizationId, pspId, flowName);
    if (result.isEmpty()) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, flowName);
    }

    log.debugf("Entity found. Mapping data to final response.");
    return this.flowMapper.toSingleFlowCreatedResponse(result.get());
  }

  @WithSpan(kind = SERVER)
  @Transactional(rollbackOn = Exception.class)
  public GenericResponse createEmptyFlow(String pspId, String flowName, CreateFlowRequest request) {

    log.debugf(
        "Saving new flows by organizationId [%s], pspId [%s], flowName [%s]",
        request.getReceiver().getOrganizationId(), pspId, flowName);

    ConfigDataV1 configData = cachedConfig.getClonedCache();
    SemanticValidator.validateCreateFlowRequest(configData, pspId, flowName, request);

    // check if there is already another unpublished flow that is in progress
    Optional<FlowEntity> optPublishingFlow =
        flowRepository.findUnpublishedByPspIdAndNameReadOnly(pspId, flowName);
    if (optPublishingFlow.isPresent()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_ALREADY_EXIST,
          flowName,
          optPublishingFlow.get().getStatus());
    }

    // retrieve the last published flow, in order to take its revision and increment it
    Optional<FlowEntity> lastPublishedFlow =
        flowRepository.findLastPublishedByPspIdAndName(pspId, flowName);
    Long revision = lastPublishedFlow.map(flowEntity -> (flowEntity.getRevision() + 1)).orElse(1L);

    // finally, persist the newly generated entity
    FlowEntity entity = flowMapper.toEntity(request, revision);
    this.flowRepository.createEntity(entity);

    // Send event to Registro Eventi for internal operation
    storeInternalREEvent(entity, FdrStatusEnum.CREATED, FdrActionEnum.CREATE_FLOW);

    return GenericResponse.builder().message(String.format("Fdr [%s] saved", flowName)).build();
  }

  @WithSpan(kind = SERVER)
  @Transactional(rollbackOn = Exception.class)
  public GenericResponse publishFlow(String pspId, String flowName, boolean isInternalCall) {

    log.debugf("Publishing existing flows by pspId [%s], flowName [%s]", pspId, flowName);

    ConfigDataV1 configData = cachedConfig.getClonedCache();
    SemanticValidator.validateOnlyFlowFilters(configData, pspId, flowName);

    // check if there is an unpublished flow that is in progress
    Optional<FlowEntity> optPublishingFlow =
        flowRepository.findUnpublishedByPspIdAndName(pspId, flowName);
    if (optPublishingFlow.isEmpty()) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, flowName);
    }
    FlowEntity publishingFlow = optPublishingFlow.get();
    if (!FlowStatusEnum.INSERTED.name().equals(publishingFlow.getStatus())) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION,
          flowName,
          publishingFlow.getStatus());
    }

    // check if retrieved flow can be published
    SemanticValidator.validatePublishingFlow(publishingFlow);
    publishNewRevision(pspId, flowName, publishingFlow);

    FlowToHistoryEntity flowToHistoryEntity =
        flowToHistoryMapper.toEntity(publishingFlow, isInternalCall);
    this.flowToHistoryRepository.createEntity(flowToHistoryEntity);

    // Send event to Registro Eventi for internal operation
    storeInternalREEvent(publishingFlow, FdrStatusEnum.PUBLISHED, FdrActionEnum.PUBLISH);

    return GenericResponse.builder().message(String.format("Fdr [%s] published", flowName)).build();
  }

  @WithSpan(kind = SERVER)
  @Transactional(rollbackOn = Exception.class)
  public GenericResponse deleteExistingFlow(String pspId, String flowName) {

    log.debugf("Deleting existing flows by pspId [%s], flowName [%s]", pspId, flowName);

    ConfigDataV1 configData = cachedConfig.getClonedCache();
    SemanticValidator.validateOnlyFlowFilters(configData, pspId, flowName);

    // check if there is already another unpublished flow that is in progress
    Optional<FlowEntity> optPublishingFlow =
        flowRepository.findUnpublishedByPspIdAndName(pspId, flowName);
    if (optPublishingFlow.isEmpty()) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, flowName);
    }

    // delete flow and if there are multiple payments related to it yet, delete them in async mode
    FlowEntity publishingFlow = optPublishingFlow.get();
    this.flowRepository.deleteEntity(publishingFlow);

    // Send event to Registro Eventi for internal operation
    storeInternalREEvent(publishingFlow, FdrStatusEnum.DELETED, FdrActionEnum.DELETE_FLOW);

    return GenericResponse.builder().message(String.format("Fdr [%s] deleted", flowName)).build();
  }

  public void publishNewRevision(String pspId, String flowName, FlowEntity publishingFlow) {

    this.flowRepository.updateLastPublishedAsNotLatest(pspId, flowName);

    // update the publishing flow in order to set its status to PUBLISHED
    Instant now = Instant.now();
    publishingFlow.setUpdated(now);
    publishingFlow.setPublished(now);
    publishingFlow.setIsLatest(true);
    publishingFlow.setStatus(FlowStatusEnum.PUBLISHED.name());
    this.flowRepository.updateEntity(publishingFlow);
  }

  private void storeInternalREEvent(
      FlowEntity publishingFlow, FdrStatusEnum status, FdrActionEnum action) {

    MDC.put(MDCKeys.ORGANIZATION_ID, publishingFlow.getReceiverId());
    MDC.put(MDCKeys.FDR_STATUS, status.name());

    boolean canBeWrittenAsREEvent =
        "1".equals(Optional.ofNullable(MDC.get(IS_RE_ENABLED_FOR_THIS_CALL)).orElse("0"));
    if (canBeWrittenAsREEvent) {
      reService.sendEvent(
          ReEvent.builder()
              .serviceIdentifier(AppVersionEnum.FDR003)
              .created(Instant.now())
              .sessionId(MDC.get(MDCKeys.TRX_ID))
              .eventType(EventTypeEnum.INTERNAL)
              .fdrStatus(status)
              .fdr(publishingFlow.getName())
              .pspId(publishingFlow.getSenderId())
              .organizationId(publishingFlow.getReceiverId())
              .revision(publishingFlow.getRevision())
              .fdrAction(action)
              .build());
    }
  }
}
