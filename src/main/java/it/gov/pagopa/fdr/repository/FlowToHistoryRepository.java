package it.gov.pagopa.fdr.repository;

import static io.quarkus.panache.common.Sort.by;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import it.gov.pagopa.fdr.repository.common.Repository;
import it.gov.pagopa.fdr.repository.entity.FlowToHistoryEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FlowToHistoryRepository extends Repository
    implements PanacheRepository<FlowToHistoryEntity> {

  public void createEntity(FlowToHistoryEntity entity) {

    entity.persist();
  }

  public PanacheQuery<FlowToHistoryEntity> findTopNEntitiesOrderByCreated(Integer limit) {
    return find("retries < ?2 and isExternal = ?3", by("created").descending(), 3, true)
        .page(0, limit);
  }
}
