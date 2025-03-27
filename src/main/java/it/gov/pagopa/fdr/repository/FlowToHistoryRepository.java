package it.gov.pagopa.fdr.repository;

import static io.quarkus.panache.common.Sort.by;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import it.gov.pagopa.fdr.repository.common.Repository;
import it.gov.pagopa.fdr.repository.entity.FlowToHistoryEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Instant;

@ApplicationScoped
public class FlowToHistoryRepository extends Repository
    implements PanacheRepository<FlowToHistoryEntity> {

  public void createEntity(FlowToHistoryEntity entity) {

    entity.persist();
  }

  @Transactional
  public void deleteByIdTransactional(Long id) {
    this.deleteById(id);
  }

  public PanacheQuery<FlowToHistoryEntity> findTopNEntitiesOrderByCreated(
      Integer limit, Integer maxRetries) {
    return find(
            "retries < ?1 and (lockUntil IS NULL OR lockUntil < ?2)",
            by("created").descending(),
            maxRetries,
            Instant.now())
        .page(0, limit);
  }
}
