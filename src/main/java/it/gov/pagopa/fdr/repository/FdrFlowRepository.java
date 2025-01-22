package it.gov.pagopa.fdr.repository;

import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import it.gov.pagopa.fdr.repository.entity.common.Repository;
import it.gov.pagopa.fdr.repository.entity.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.flow.FdrFlowEntity;
import it.gov.pagopa.fdr.repository.entity.flow.projection.FdrFlowIdProjection;
import it.gov.pagopa.fdr.repository.enums.FlowStatusEnum;
import it.gov.pagopa.fdr.util.StringUtil;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

@ApplicationScoped
public class FdrFlowRepository extends Repository {

  public static final String QUERY_GET_BY_PSP_AND_NAME_AND_REVISION_AND_ORGANIZATION_AND_STATUS =
      "sender.psp_id = :pspId"
          + " and name = :flowName"
          + " and revision = :revision"
          + " and receiver.organization_id = :organizationId"
          + " and status = :status";

  public static final String QUERY_GET_UNPUBLISHED_BY_PSP =
      "sender.psp_id = :pspId and status != 'PUBLISHED'";

  public static final String QUERY_GET_UNPUBLISHED_BY_PSP_AND_NAME =
      "sender.psp_id = :pspId and name = :flowName and status != 'PUBLISHED'";

  public static final String QUERY_GET_UNPUBLISHED_BY_PSP_AND_NAME_AND_ORGANIZATION =
      "sender.psp_id = :pspId"
          + " and name = :flowName"
          + " and receiver.organization_id = :organizationId"
          + " and status != 'PUBLISHED'";

  public static final String QUERY_GET_UNPUBLISHED_BY_ORGANIZATION_AND_PSP_AND_NAME =
      "sender.psp_id = :pspId"
          + " and name = :flowName"
          + " and receiver.organization_id = :organizationId"
          + " and status != 'PUBLISHED'";

  public static final String QUERY_GET_PUBLISHED_BY_PSP_AND_NAME =
      "sender.psp_id = :pspId and name = :flowName and status = 'PUBLISHED'";

  public RepositoryPagedResult<FdrFlowEntity> findPublishedByOrganizationIdAndOptionalPspId(
      String organizationId, String pspId, Instant publishedGt, int pageNumber, int pageSize) {

    Parameters parameters = new Parameters();
    List<String> queryBuilder = new ArrayList<>();

    // setting mandatory field: organization id
    queryBuilder.add("receiver.organization_id = :organizationId");
    parameters.and("organizationId", organizationId);

    // setting optional field: PSP id
    if (!StringUtil.isNullOrBlank(pspId)) {
      queryBuilder.add("sender.psp_id = :pspId");
      parameters.and("pspId", pspId);
    }

    // setting optional field: publish date
    if (publishedGt != null) {
      queryBuilder.add("published > :publishedGt");
      parameters.and("publishedGt", publishedGt);
    }

    // setting mandatory field: flow status
    queryBuilder.add("status = :status");
    parameters.and("status", FlowStatusEnum.PUBLISHED);
    String queryString = String.join(" and ", queryBuilder);

    Page page = Page.of(pageNumber - 1, pageSize);
    Sort sort = getSort(Pair.of("_id", Direction.Ascending));

    PanacheQuery<FdrFlowEntity> resultPage =
        FdrFlowEntity.findPageByQuery(queryString, sort, parameters).page(page);
    return getPagedResult(resultPage);
  }

  public RepositoryPagedResult<FdrFlowEntity> findPublishedByPspIdAndOptionalOrganizationId(
      String pspId, String organizationId, Instant publishedGt, int pageNumber, int pageSize) {

    Parameters parameters = new Parameters();
    List<String> queryBuilder = new ArrayList<>();

    // setting mandatory field: PSP id
    queryBuilder.add("sender.psp_id = :pspId");
    parameters.and("pspId", pspId);

    // setting optional field: organization id
    if (!StringUtil.isNullOrBlank(organizationId)) {
      queryBuilder.add("receiver.organization_id = :organizationId");
      parameters.and("organizationId", organizationId);
    }

    // setting optional field: publish date
    if (publishedGt != null) {
      queryBuilder.add("published > :publishedGt");
      parameters.and("publishedGt", publishedGt);
    }

    // setting mandatory field: flow status
    queryBuilder.add("status = :status");
    parameters.and("status", FlowStatusEnum.PUBLISHED);
    String queryString = String.join(" and ", queryBuilder);

    Page page = Page.of(pageNumber - 1, pageSize);
    Sort sort = getSort(Pair.of("_id", Direction.Ascending));

    PanacheQuery<FdrFlowEntity> resultPage =
        FdrFlowEntity.findPageByQuery(queryString, sort, parameters).page(page);
    return getPagedResult(resultPage);
  }

  public FdrFlowEntity findPublishedByOrganizationIdAndPspIdAndName(
      String organizationId, String pspId, String flowName, long revision) {

    // defining query with mandatory fields
    Parameters parameters = new Parameters();
    parameters.and("pspId", pspId);
    parameters.and("flowName", flowName);
    parameters.and("revision", revision);
    parameters.and("organizationId", organizationId);
    parameters.and("status", FlowStatusEnum.PUBLISHED);

    return FdrFlowEntity.findByQuery(
            FdrFlowRepository.QUERY_GET_BY_PSP_AND_NAME_AND_REVISION_AND_ORGANIZATION_AND_STATUS,
            parameters)
        .project(FdrFlowEntity.class)
        .firstResultOptional()
        .orElse(null);
  }

  public FdrFlowEntity findPublishedByPspIdAndName(String pspId, String flowName) {

    Parameters parameters = new Parameters();
    parameters.and("pspId", pspId);
    parameters.and("flowName", flowName);

    return FdrFlowEntity.findByQuery(
            FdrFlowRepository.QUERY_GET_PUBLISHED_BY_PSP_AND_NAME, parameters)
        .project(FdrFlowEntity.class)
        .firstResultOptional()
        .orElse(null);
  }

  public RepositoryPagedResult<FdrFlowEntity> findUnpublishedByPspId(
      String pspId, Instant createdGt, int pageNumber, int pageSize) {

    Parameters parameters = new Parameters();
    parameters.and("pspId", pspId);
    parameters.and("createdGt", createdGt);

    Page page = Page.of(pageNumber - 1, pageSize);
    Sort sort = getSort(Pair.of("_id", Direction.Ascending));

    PanacheQuery<FdrFlowEntity> resultPage =
        FdrFlowEntity.findPageByQuery(
                FdrFlowRepository.QUERY_GET_UNPUBLISHED_BY_PSP, sort, parameters)
            .page(page);
    return getPagedResult(resultPage);
  }

  public FdrFlowEntity findUnpublishedByPspIdAndName(String pspId, String flowName) {

    Parameters parameters = new Parameters();
    parameters.and("pspId", pspId);
    parameters.and("flowName", flowName);

    return FdrFlowEntity.findByQuery(
            FdrFlowRepository.QUERY_GET_UNPUBLISHED_BY_PSP_AND_NAME, parameters)
        .project(FdrFlowEntity.class)
        .firstResultOptional()
        .orElse(null);
  }

  public FdrFlowEntity findUnpublishedByOrganizationIdAndPspIdAndName(
      String organizationId, String pspId, String flowName) {

    Parameters parameters = new Parameters();
    parameters.and("organizationId", organizationId);
    parameters.and("pspId", pspId);
    parameters.and("flowName", flowName);

    return FdrFlowEntity.findByQuery(
            FdrFlowRepository.QUERY_GET_UNPUBLISHED_BY_ORGANIZATION_AND_PSP_AND_NAME, parameters)
        .project(FdrFlowEntity.class)
        .firstResultOptional()
        .orElse(null);
  }

  public FdrFlowIdProjection findIdByOrganizationIdAndPspIdAndNameAndRevision(
      String organizationId, String pspId, String flowName, long revision, FlowStatusEnum status) {

    // defining query with mandatory fields
    Parameters parameters = new Parameters();
    parameters.and("pspId", pspId);
    parameters.and("flowName", flowName);
    parameters.and("revision", revision);
    parameters.and("organizationId", organizationId);
    parameters.and("status", status);

    return FdrFlowEntity.findByQuery(
            FdrFlowRepository.QUERY_GET_BY_PSP_AND_NAME_AND_REVISION_AND_ORGANIZATION_AND_STATUS,
            parameters)
        .project(FdrFlowIdProjection.class)
        .firstResultOptional()
        .orElse(null);
  }

  public FdrFlowIdProjection findUnpublishedIdByPspIdAndNameAndOrganization(
      String pspId, String flowName, String organizationId) {

    // defining query with mandatory fields
    Parameters parameters = new Parameters();
    parameters.and("pspId", pspId);
    parameters.and("flowName", flowName);
    parameters.and("organizationId", organizationId);

    return FdrFlowEntity.findByQuery(
            FdrFlowRepository.QUERY_GET_UNPUBLISHED_BY_PSP_AND_NAME_AND_ORGANIZATION, parameters)
        .project(FdrFlowIdProjection.class)
        .firstResultOptional()
        .orElse(null);
  }

  public void createEntity(FdrFlowEntity entity) {
    entity.setTimestamp(Instant.now());
    entity.persist();
  }

  public void updateEntity(FdrFlowEntity entity) {
    entity.setTimestamp(Instant.now());
    entity.update();
  }

  public void deleteEntity(FdrFlowEntity entity) {
    entity.delete();
  }
}
