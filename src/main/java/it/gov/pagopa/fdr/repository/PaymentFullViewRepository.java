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
import it.gov.pagopa.fdr.repository.entity.PaymentFullViewEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class PaymentFullViewRepository extends Repository implements PanacheRepository<PaymentFullViewEntity> {

  public static final String INDEX = "id.index";

  public static final String QUERY_GET_BY_FLOW_ID = "id.flowId = ?1";

  public static final String QUERY_GET_BY_FLOW_ID_AND_INDEXES = "id.flowId = ?1" + " and id.index in ?2";

  public PaymentFullViewRepository() {
  }

  public RepositoryPagedResult<PaymentFullViewEntity> findByPspAndIuvAndIur(
      String pspId,
      String iuv,
      String iur,
      Instant createdFrom,
      Instant createdTo,
      String orgDomainId,
      int pageNumber,
      int pageSize) {

    StringBuilder query =
        new StringBuilder(
            "SELECT p FROM PaymentFullViewEntity p LEFT JOIN FETCH p.flow WHERE p.flow.pspDomainId = :psp");
    Parameters params = new Parameters().and("psp", pspId);

    if (orgDomainId != null) {
      query.append("  and p.flow.orgDomainId = :orgDomainId");
      params.and("orgDomainId", orgDomainId);
    }
    if (iuv != null) {
      query.append(" and p.iuv = :iuv");
      params.and("iuv", iuv);
    }
    if (iur != null) {
      query.append(" and p.iur = :iur");
      params.and("iur", iur);
    }
    if (createdFrom != null) {
      query.append(" and p.created >= :createdFrom");
      params.and("createdFrom", createdFrom);
    }
    if (createdTo != null) {
      query.append(" and p.created <= :createdTo");
      params.and("createdTo", createdTo);
    }

    // add ORDER BY as qualified alias to avoid ambiguity with the JOIN on flow
    query.append(" ORDER BY p.id.index ASC");

    Page page = Page.of(pageNumber - 1, pageSize);

    PanacheQuery<PaymentFullViewEntity> resultPage =
        PaymentFullViewEntity.findPageByQuery(query.toString(), params).page(page);
    return getPagedResult(resultPage);
  }

  public PanacheQuery<PaymentFullViewEntity> findPageByFlowId(Long flowId, int pageNumber, int pageSize) {

    Page page = Page.of(pageNumber, pageSize);
    Sort sort = getSort(SortField.of(INDEX, Direction.Ascending));

    return find(QUERY_GET_BY_FLOW_ID, sort, flowId).page(page);
  }

  public RepositoryPagedResult<PaymentFullViewEntity> findByFlowId(
      Long flowId, int pageNumber, int pageSize) {

    Page page = Page.of(pageNumber - 1, pageSize);
    Sort sort = getSort(SortField.of(INDEX, Direction.Ascending));

    PanacheQuery<PaymentFullViewEntity> resultPage = find(QUERY_GET_BY_FLOW_ID, sort, flowId).page(page);
    return getPagedResult(resultPage);
  }

  public List<PaymentFullViewEntity> findByFlowIdAndIndexes(Long flowId, Set<Long> indexes) {
    return find(QUERY_GET_BY_FLOW_ID_AND_INDEXES, flowId, indexes).list();
  }
}
