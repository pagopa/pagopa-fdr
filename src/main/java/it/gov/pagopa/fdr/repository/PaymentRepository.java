package it.gov.pagopa.fdr.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import it.gov.pagopa.fdr.repository.common.Repository;
import it.gov.pagopa.fdr.repository.common.SortField;
import it.gov.pagopa.fdr.repository.entity.PaymentEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PaymentRepository extends Repository implements PanacheRepository<PaymentEntity> {

    public static final String INDEX = "id.index";

    public static final String QUERY_GET_BY_FLOW_ID = "id.flowId = ?1";

    public PanacheQuery<PaymentEntity> findPageByFlowId(Long flowId, int pageNumber, int pageSize) {

        Page page = Page.of(pageNumber, pageSize);
        Sort sort = getSort(SortField.of(INDEX, Sort.Direction.Ascending));

        return find(QUERY_GET_BY_FLOW_ID, sort, flowId).page(page);
    }

}
