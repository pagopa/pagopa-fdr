package it.gov.pagopa.fdr.repository;

import static io.smallrye.common.constraint.Assert.assertTrue;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.repository.entity.FlowToHistoryEntity;
import it.gov.pagopa.fdr.test.util.PostgresResource;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
class FlowToHistoryRepositoryTest {

  @Inject FlowToHistoryRepository flowToHistoryRepository;

  @Test
  @DisplayName("FlowToHistoryRepository OK - findTopNEntitiesOrderByCreated")
  void testFindPublishedByPspIdAndOptionalOrganizationId() {

    PanacheQuery<FlowToHistoryEntity> result =
        flowToHistoryRepository.findTopNEntitiesOrderByCreated(1);

    List<FlowToHistoryEntity> entities = result.list();
    assertTrue(entities.isEmpty());
  }
}
