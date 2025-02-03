package it.gov.pagopa.fdr.repository.sql;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import it.gov.pagopa.fdr.repository.enums.FlowStatusEnum;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FlowRepository implements PanacheRepository<FlowEntity> {

  public static final String QUERY_GET_UNPUBLISHED_BY_PSP_AND_NAME =
      "senderPspId = ?1 and name = ?2 and status != ?3";

  public static final String QUERY_GET_LAST_PUBLISHED_BY_PSP_AND_NAME =
      "senderPspId = ?1 and name = ?2 and status = ?3 and isLatest = ?4";

  public FlowEntity findUnpublishedByPspIdAndName(String pspId, String flowName) {
    return find(
            QUERY_GET_UNPUBLISHED_BY_PSP_AND_NAME, pspId, flowName, FlowStatusEnum.PUBLISHED.name())
        .firstResultOptional()
        .orElse(null);
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

  public void updateLastPublishedAsNotLatest(String pspId, String flowName) {

    FlowEntity entity = findLastPublishedByPspIdAndName(pspId, flowName);
    if (entity != null) {
      entity.setIsLatest(false);
      updateEntity(entity);
    }
  }

  public void createEntity(FlowEntity entity) {
    // entity.setTimestamp(Instant.now());
    entity.persist();
  }

  public void updateEntity(FlowEntity entity) {
    // entity.setTimestamp(Instant.now());
    persist(entity);
  }

  public void deleteEntity(FlowEntity entity) {
    entity.delete();
  }
}
