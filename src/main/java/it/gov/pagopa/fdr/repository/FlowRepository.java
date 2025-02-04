package it.gov.pagopa.fdr.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import it.gov.pagopa.fdr.repository.common.Repository;
import it.gov.pagopa.fdr.repository.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.common.SortField;
import it.gov.pagopa.fdr.repository.entity.FlowEntity;
import it.gov.pagopa.fdr.repository.enums.FlowStatusEnum;
import it.gov.pagopa.fdr.util.common.StringUtil;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class FlowRepository extends Repository implements PanacheRepository<FlowEntity> {

  public static final String QUERY_GET_BY_PSP_AND_NAME_AND_REVISION_AND_ORGANIZATION_AND_STATUS =
      "senderPspId = ?1"
          + " and name = ?2"
          + " and revision = ?3"
          + " and receiverOrganizationId = ?4"
          + " and status = ?5";

  public static final String QUERY_GET_UNPUBLISHED_BY_ORGANIZATION_AND_PSP_AND_NAME =
      "senderPspId = ?1"
          + " and name = ?2"
          + " and receiverOrganizationId = ?3"
          + " and status != ?4";

  public static final String QUERY_GET_UNPUBLISHED_BY_PSP_AND_NAME =
      "senderPspId = ?1 and name = ?2 and status != ?3";

  public static final String QUERY_GET_UNPUBLISHED_BY_PSP_AND_NAME_AND_ORGANIZATION =
      "senderPspId = ?1"
          + " and name = ?2"
          + " and receiverOrganizationId = ?3"
          + " and status != ?4";

  public static final String QUERY_GET_LAST_PUBLISHED_BY_PSP_AND_NAME =
      "senderPspId = ?1 and name = ?2 and status = ?3 and isLatest = ?4";

  public FlowEntity findUnpublishedByPspIdAndName(String pspId, String flowName) {
    return find(
            QUERY_GET_UNPUBLISHED_BY_PSP_AND_NAME, pspId, flowName, FlowStatusEnum.PUBLISHED.name())
        .firstResultOptional()
        .orElse(null);
  }

  public FlowEntity findUnpublishedByOrganizationIdAndPspIdAndName(
      String organizationId, String pspId, String flowName) {
    return find(
            QUERY_GET_UNPUBLISHED_BY_ORGANIZATION_AND_PSP_AND_NAME,
            pspId,
            flowName,
            organizationId,
            FlowStatusEnum.PUBLISHED.name())
        .firstResultOptional()
        .orElse(null);
  }

  public RepositoryPagedResult<FlowEntity> findUnpublishedByPspId(
      String pspId, Instant createdGt, int pageNumber, int pageSize) {

    Parameters parameters = new Parameters();
    List<String> queryBuilder = new ArrayList<>();

    // setting mandatory field: organization id
    queryBuilder.add("senderPspId = :pspId");
    parameters.and("pspId", pspId);

    // setting mandatory field: flow status
    queryBuilder.add("status != :status");
    parameters.and("status", FlowStatusEnum.PUBLISHED);

    // setting optional field: created date
    if (createdGt != null) {
      queryBuilder.add("created > :createdGt");
      parameters.and("createdGt", createdGt);
    }

    String queryString = String.join(" and ", queryBuilder);

    Page page = Page.of(pageNumber - 1, pageSize);
    Sort sort = getSort(SortField.of("id", Direction.Ascending));

    PanacheQuery<FlowEntity> resultPage =
        FlowEntity.findPageByQuery(queryString, sort, parameters).page(page);
    return getPagedResult(resultPage);
  }

  public FlowEntity findPublishedByOrganizationIdAndPspIdAndName(
      String organizationId, String pspId, String flowName, long revision) {

    return find(
            QUERY_GET_BY_PSP_AND_NAME_AND_REVISION_AND_ORGANIZATION_AND_STATUS,
            pspId,
            flowName,
            revision,
            organizationId,
            FlowStatusEnum.PUBLISHED.name())
        .firstResultOptional()
        .orElse(null);
  }

  public RepositoryPagedResult<FlowEntity> findPublishedByPspIdAndOptionalOrganizationId(
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
    Sort sort = getSort(SortField.of("id", Direction.Ascending));

    PanacheQuery<FlowEntity> resultPage =
        FlowEntity.findPageByQuery(queryString, sort, parameters).page(page);
    return getPagedResult(resultPage);
  }

  public FlowEntity findLastPublishedByPspIdAndName(String pspId, String flowName) {

    return find(
            QUERY_GET_LAST_PUBLISHED_BY_PSP_AND_NAME,
            pspId,
            flowName,
            FlowStatusEnum.PUBLISHED.name(),
            true)
        .firstResultOptional()
        .orElse(null);
  }

  public Long findIdByOrganizationIdAndPspIdAndNameAndRevision(
      String organizationId,
      String pspId,
      String flowName,
      long revision,
      FlowStatusEnum flowStatusEnum) {

    return find(
            QUERY_GET_BY_PSP_AND_NAME_AND_REVISION_AND_ORGANIZATION_AND_STATUS,
            pspId,
            flowName,
            revision,
            organizationId,
            flowStatusEnum.name())
        .firstResultOptional()
        .map(FlowEntity::getId)
        .orElse(null);
  }

  public Long findUnpublishedIdByPspIdAndNameAndOrganization(
      String pspId, String flowName, String organizationId) {

    return find(
            QUERY_GET_UNPUBLISHED_BY_PSP_AND_NAME_AND_ORGANIZATION,
            pspId,
            flowName,
            organizationId,
            FlowStatusEnum.PUBLISHED.name())
        .firstResultOptional()
        .map(FlowEntity::getId)
        .orElse(null);
  }

  public RepositoryPagedResult<FlowEntity> findLatestPublishedByOrganizationIdAndOptionalPspId(
      String organizationId, String pspId, Instant publishedGt, int pageNumber, int pageSize) {

    Parameters parameters = new Parameters();
    List<String> queryBuilder = new ArrayList<>();

    // setting mandatory field: organization id
    queryBuilder.add("receiverOrganizationId = :organizationId");
    parameters.and("organizationId", organizationId);

    // setting optional field: PSP id
    if (!StringUtil.isNullOrBlank(pspId)) {
      queryBuilder.add("senderPspId = :pspId");
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

    // setting mandatory field: is_latest flag as true
    queryBuilder.add("isLatest = :isLatest");
    parameters.and("isLatest", true);
    String queryString = String.join(" and ", queryBuilder);

    Page page = Page.of(pageNumber - 1, pageSize);
    Sort sort = getSort(SortField.of("id", Direction.Ascending));

    PanacheQuery<FlowEntity> resultPage =
        FlowEntity.findPageByQuery(queryString, sort, parameters).page(page);
    return getPagedResult(resultPage);
  }

  public void createEntity(FlowEntity entity) {
    // entity.setTimestamp(Instant.now());
    entity.persist();
  }

  public void updateEntity(FlowEntity entity) {
    // entity.setTimestamp(Instant.now());
    persist(entity);
  }

  public void updateLastPublishedAsNotLatest(String pspId, String flowName) {

    FlowEntity entity = findLastPublishedByPspIdAndName(pspId, flowName);
    if (entity != null) {
      entity.setIsLatest(false);
      updateEntity(entity);
    }
  }

  public void deleteEntity(FlowEntity entity) {
    entity.delete();
  }
}
