package it.gov.pagopa.fdr.repository;

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
}
