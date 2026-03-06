package it.gov.pagopa.fdr.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import it.gov.pagopa.fdr.repository.common.Repository;
import it.gov.pagopa.fdr.repository.entity.PaymentStagingEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Set;

@ApplicationScoped
public class PaymentStagingRepository extends Repository implements PanacheRepository<PaymentStagingEntity> {

  public static final String QUERY_GET_BY_FLOW_ID_AND_INDEXES = "id.flowId = ?1" + " and id.index in ?2";

  public PaymentStagingRepository() {
  }

  public List<PaymentStagingEntity> findByFlowIdAndIndexes(Long flowId, Set<Long> indexes) {
    return find(QUERY_GET_BY_FLOW_ID_AND_INDEXES, flowId, indexes).list();
  }

  public void deleteEntityInBulk(List<PaymentStagingEntity> entityBatch) {
    for (PaymentStagingEntity entity : entityBatch) {
      delete("id.flowId = ?1 and id.index = ?2", entity.getId().getFlowId(), entity.getId().getIndex());
    }
  }
}
