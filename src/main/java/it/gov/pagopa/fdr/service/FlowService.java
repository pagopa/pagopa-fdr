package it.gov.pagopa.fdr.service;

import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsResponse;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.repository.FdrFlowRepository;
import it.gov.pagopa.fdr.repository.entity.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.flow.FdrFlowEntity;
import it.gov.pagopa.fdr.service.middleware.mapper.FlowMapper;
import it.gov.pagopa.fdr.service.middleware.validator.SemanticValidator;
import it.gov.pagopa.fdr.service.model.FindFlowsByFiltersArgs;
import it.gov.pagopa.fdr.util.validator.ValidationResult;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;

@ApplicationScoped
public class FlowService {

  private Logger log;

  private final Config cachedConfig;

  private final FdrFlowRepository flowRepository;

  private final FlowMapper flowMapper;

  public FlowService(
      Logger log, Config cachedConfig, FdrFlowRepository flowRepository, FlowMapper flowMapper) {

    this.log = log;
    this.cachedConfig = cachedConfig;
    this.flowRepository = flowRepository;
    this.flowMapper = flowMapper;
  }

  public PaginatedFlowsResponse getPaginatedPublishedFlows(FindFlowsByFiltersArgs args) {

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
        "Executing query on flows by organizationId [%s], pspId [%s] - pageIndex: [%s],"
            + " pageSize:[%s]",
        organizationId, pspId, pageNumber, pageSize);

    ConfigDataV1 configData = cachedConfig.getClonedCache();
    ValidationResult validationResult =
        SemanticValidator.validateGetPaginatedFlowsRequest(configData, args);

    if (validationResult.isInvalid()) {
      throw new AppException(validationResult.getError(), validationResult.getErrorArgs());
    }

    RepositoryPagedResult<FdrFlowEntity> paginatedResult =
        this.flowRepository.findPublishedByOrganizationIdAndPspId(
            organizationId, pspId, args.getPublishedGt(), (int) pageNumber, (int) pageSize);
    log.debugf(
        "Found [%s] entities in [%s] pages. Mapping data to final response.",
        paginatedResult.getTotalElements(), paginatedResult.getTotalPages());

    return flowMapper.toPaginatedFlowResponse(paginatedResult, pageSize, pageNumber);
  }
}
