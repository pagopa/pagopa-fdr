package it.gov.pagopa.fdr.repository;

import static io.quarkus.panache.common.Sort.by;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import it.gov.pagopa.fdr.repository.common.Repository;
import it.gov.pagopa.fdr.repository.entity.FlowToHistoryEntity;
import it.gov.pagopa.fdr.repository.enums.FlowToHistoryStatusEnum;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FlowToHistoryRepository extends Repository
    implements PanacheRepository<FlowToHistoryEntity> {

  public void createEntity(FlowToHistoryEntity entity) {

    entity.persist();
  }

  public PanacheQuery<FlowToHistoryEntity> findTopNNeverStartedOrderByCreated(Integer limit) {
    return find(
            "generationProcess != ?1 and retries < ?2 and isExternal = ?3",
            by("created").descending(),
            FlowToHistoryStatusEnum.OK.name(),
            3,
            true)
        .page(0, limit);
  }
}
