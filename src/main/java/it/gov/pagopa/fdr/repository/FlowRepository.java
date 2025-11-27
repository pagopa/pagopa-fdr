package it.gov.pagopa.fdr.repository;

import io.micrometer.core.annotation.Timed;
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
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.NoResultException;
import org.hibernate.Session;
import org.jboss.logging.Logger;

@ApplicationScoped
public class FlowRepository extends Repository implements PanacheRepository<FlowEntity> {

  public static final String QUERY_GET_BY_PSP_AND_NAME_AND_REVISION_AND_ORGANIZATION_AND_STATUS =
      "pspDomainId = ?1"
          + " and name = ?2"
          + " and revision = ?3"
          + " and orgDomainId = ?4"
          + " and status = ?5";

  public static final String QUERY_GET_UNPUBLISHED_BY_ORGANIZATION_AND_PSP_AND_NAME =
      "pspDomainId = ?1" + " and name = ?2" + " and orgDomainId = ?3" + " and status != ?4";

  public static final String QUERY_GET_UNPUBLISHED_BY_PSP_AND_NAME =
      "pspDomainId = ?1 and name = ?2 and status != ?3";

  public static final String QUERY_GET_UNPUBLISHED_BY_PSP_AND_NAME_AND_ORGANIZATION =
      "pspDomainId = ?1" + " and name = ?2" + " and orgDomainId = ?3" + " and status != ?4";

  public static final String QUERY_GET_LAST_PUBLISHED_BY_PSP_AND_NAME =
      "pspDomainId = ?1 and name = ?2 and status = ?3 and isLatest = ?4";
  public static final String PSP_ID = "pspId";
  public static final String STATUS = "status";
  public static final String AND = " and ";
  public static final String PSP_DOMAIN_ID_PSP_ID = "pspDomainId = :pspId";

  private final EntityManager entityManager;

  private final Logger log;

  public FlowRepository(Logger log, EntityManager em) {
    this.log = log;
    this.entityManager = em;
  }

  public Optional<FlowEntity> findUnpublishedByPspIdAndName(String pspId, String flowName) {
    return find(
            QUERY_GET_UNPUBLISHED_BY_PSP_AND_NAME, pspId, flowName, FlowStatusEnum.PUBLISHED.name())
        .firstResultOptional();
  }

  public Optional<FlowEntity> findUnpublishedByPspIdAndNameReadOnly(String pspId, String flowName) {
    try {
      FlowEntity entity = entityManager.createQuery(
                      "FROM FlowEntity WHERE pspDomainId = :pspId AND name = :flowName AND status != :status",
                      FlowEntity.class
              )
              .setParameter(PSP_ID, pspId)
              .setParameter("flowName", flowName)
              .setParameter(STATUS, FlowStatusEnum.PUBLISHED.name())
              .setHint("org.hibernate.readOnly", true)
              .getSingleResult();
      return Optional.ofNullable(entity);
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }

  public Optional<FlowEntity> findUnpublishedByOrganizationIdAndPspIdAndName(
      String organizationId, String pspId, String flowName) {
    return find(
            QUERY_GET_UNPUBLISHED_BY_ORGANIZATION_AND_PSP_AND_NAME,
            pspId,
            flowName,
            organizationId,
            FlowStatusEnum.PUBLISHED.name())
        .firstResultOptional();
  }

  public RepositoryPagedResult<FlowEntity> findUnpublishedByPspId(
      String pspId, Instant createdGt, int pageNumber, int pageSize) {

    Parameters parameters = new Parameters();
    List<String> queryBuilder = new ArrayList<>();

    // setting mandatory field: organization id
    queryBuilder.add(PSP_DOMAIN_ID_PSP_ID);
    parameters.and(PSP_ID, pspId);

    // setting mandatory field: flow status
    queryBuilder.add("status != :status");
    parameters.and(STATUS, FlowStatusEnum.PUBLISHED.name());

    // setting optional field: created date
    if (createdGt != null) {
      queryBuilder.add("created > :createdGt");
      parameters.and("createdGt", createdGt);
    }

    String queryString = String.join(AND, queryBuilder);

    Page page = Page.of(pageNumber - 1, pageSize);
    Sort sort = getSort(SortField.of("name", Direction.Ascending));

    PanacheQuery<FlowEntity> resultPage =
        FlowEntity.findPageByQuery(queryString, sort, parameters).page(page);
    return getPagedResult(resultPage);
  }

  public Optional<FlowEntity> findPublishedByOrganizationIdAndPspIdAndName(
      String organizationId, String pspId, String flowName, long revision) {

    return find(
            QUERY_GET_BY_PSP_AND_NAME_AND_REVISION_AND_ORGANIZATION_AND_STATUS,
            pspId,
            flowName,
            revision,
            organizationId,
            FlowStatusEnum.PUBLISHED.name())
        .firstResultOptional();
  }

  public RepositoryPagedResult<FlowEntity> findPublishedByPspIdAndOptionalOrganizationId(
      String pspId, String organizationId, Instant publishedGt, int pageNumber, int pageSize) {

    Parameters parameters = new Parameters();
    List<String> queryBuilder = new ArrayList<>();

    // setting mandatory field: PSP id
    queryBuilder.add(PSP_DOMAIN_ID_PSP_ID);
    parameters.and(PSP_ID, pspId);

    // setting optional field: organization id
    if (!StringUtil.isNullOrBlank(organizationId)) {
      queryBuilder.add("orgDomainId = :organizationId");
      parameters.and("organizationId", organizationId);
    }

    // setting optional field: publish date
    if (publishedGt != null) {
      queryBuilder.add("published > :publishedGt");
      parameters.and("publishedGt", publishedGt);
    }

    // setting mandatory field: flow status
    queryBuilder.add("status = :status");
    parameters.and(STATUS, FlowStatusEnum.PUBLISHED.name());
    String queryString = String.join(AND, queryBuilder);

    Page page = Page.of(pageNumber - 1, pageSize);
    Sort sort =
        getSort(
            SortField.of("name", Direction.Ascending),
            SortField.of("revision", Direction.Ascending));

    PanacheQuery<FlowEntity> resultPage =
        FlowEntity.findPageByQuery(queryString, sort, parameters).page(page);
    return getPagedResult(resultPage);
  }

  public Optional<FlowEntity> findLastPublishedByPspIdAndName(String pspId, String flowName) {

    return find(
            QUERY_GET_LAST_PUBLISHED_BY_PSP_AND_NAME,
            pspId,
            flowName,
            FlowStatusEnum.PUBLISHED.name(),
            true)
        .firstResultOptional();
  }

  public Optional<Long> findIdByOrganizationIdAndPspIdAndNameAndRevision(
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
        .map(FlowEntity::getId);
  }

  public Optional<Long> findUnpublishedIdByPspIdAndNameAndOrganization(
      String pspId, String flowName, String organizationId) {

    return find(
            QUERY_GET_UNPUBLISHED_BY_PSP_AND_NAME_AND_ORGANIZATION,
            pspId,
            flowName,
            organizationId,
            FlowStatusEnum.PUBLISHED.name())
        .firstResultOptional()
        .map(FlowEntity::getId);
  }

  public RepositoryPagedResult<FlowEntity> findLatestPublishedByOrganizationIdAndOptionalPspId(
      String organizationId, String pspId, Instant publishedGt, Instant flowDate, int pageNumber, int pageSize) {

    Parameters parameters = new Parameters();
    List<String> queryBuilder = new ArrayList<>();

    // setting mandatory field: organization id
    queryBuilder.add("orgDomainId = :organizationId");
    parameters.and("organizationId", organizationId);

    // setting optional field: PSP id
    if (!StringUtil.isNullOrBlank(pspId)) {
      queryBuilder.add(PSP_DOMAIN_ID_PSP_ID);
      parameters.and(PSP_ID, pspId);
    }

    // setting optional field: publish date
    if (publishedGt != null) {
      queryBuilder.add("published > :publishedGt");
      parameters.and("publishedGt", publishedGt);
    }

    if (flowDate != null) {
      queryBuilder.add("date > :flowDate");
      parameters.and("flowDate", flowDate);
    }

    // setting mandatory field: flow status
    queryBuilder.add("status = :status");
    parameters.and(STATUS, FlowStatusEnum.PUBLISHED.name());

    // setting mandatory field: is_latest flag as true
    queryBuilder.add("isLatest = :isLatest");
    parameters.and("isLatest", true);
    String queryString = String.join(AND, queryBuilder);

    Page page = Page.of(pageNumber - 1, pageSize);
    // Sorting by ID ensures that the sorting is deterministic in cases where multiple records have the same date.
    Sort sort = getSort(SortField.of("date", Direction.Ascending), SortField.of("id", Direction.Ascending));

    PanacheQuery<FlowEntity> resultPage =
        FlowEntity.findPageByQuery(queryString, sort, parameters).page(page);
    return getPagedResult(resultPage);
  }

  @Timed(value = "flowRepository.createEntity.task", description = "Time taken to perform createEntity", percentiles = 0.95, histogram = true)
  public void createEntity(FlowEntity entity) {
    entity.persist();
  }

  @Timed(value = "flowRepository.updateEntity.task", description = "Time taken to perform updateEntity", percentiles = 0.95, histogram = true)
  public void updateEntity(FlowEntity entity) {
    persist(entity);
  }

  @Timed(value = "flowRepository.updateComputedValues.task", description = "Time taken to perform updateComputedValues", percentiles = 0.95, histogram = true)
  public void updateComputedValues(Long flowId, long paymentsToAdd, BigDecimal amountToAdd, Instant now, FlowStatusEnum status) throws SQLException {

    Session session = entityManager.unwrap(Session.class);

    String query =
        "UPDATE flow SET"
            + " computed_tot_payments = computed_tot_payments + ?,"
            + " computed_tot_amount = computed_tot_amount + ?,"
            + " updated = ?,"
            + " status = ?"
            + " WHERE id = ?";

    try (PreparedStatement preparedStatement = session.doReturningWork(connection -> connection.prepareStatement(query))) {

      preparedStatement.setLong(1, paymentsToAdd);
      preparedStatement.setBigDecimal(2, amountToAdd.setScale(2, RoundingMode.HALF_UP));
      preparedStatement.setTimestamp(3, Timestamp.from(now));
      preparedStatement.setString(4, status.name());
      preparedStatement.setLong(5, flowId);
      preparedStatement.execute();

    } catch (SQLException e) {

      log.error("An error occurred while executing payments bulk insert", e);
      throw e;
    }
  }

  @Timed(value = "flowRepository.updateLastPublishedAsNotLatest.task", description = "Time taken to perform updateLastPublishedAsNotLatest", percentiles = 0.95, histogram = true)
  public void updateLastPublishedAsNotLatest(String pspId, String flowName) {

    Optional<FlowEntity> optEntity = findLastPublishedByPspIdAndName(pspId, flowName);
    if (optEntity.isPresent()) {
      FlowEntity entity = optEntity.get();
      entity.setIsLatest(false);
      updateEntity(entity);
    }
  }

  public void deleteEntity(FlowEntity entity) {
    entity.delete();
  }

  public PanacheQuery<FlowEntity> findByPspIdAndNameAndRevision(
      String pspId, String name, Long revision) {
    return find("pspDomainId = ?1 and name = ?2 and revision = ?3", pspId, name, revision);
  }
}
