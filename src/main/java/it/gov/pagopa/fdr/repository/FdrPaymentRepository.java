package it.gov.pagopa.fdr.repository;

import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import it.gov.pagopa.fdr.repository.entity.common.Repository;
import it.gov.pagopa.fdr.repository.entity.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.payment.FdrPaymentEntity;
import it.gov.pagopa.fdr.util.AppDBUtil;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class FdrPaymentRepository extends Repository {

  public RepositoryPagedResult<FdrPaymentEntity> executeQueryByPspAndIuvAndIur(
      String pspId,
      String iuv,
      String iur,
      Instant createdFrom,
      Instant createdTo,
      int pageNumber,
      int pageSize) {

    Page page = Page.of(pageNumber - 1, pageSize);
    Sort sort = AppDBUtil.getSort(List.of("index,asc"));

    PanacheQuery<FdrPaymentEntity> query =
        FdrPaymentEntity.executeQueryByPspAndIuvAndIur(
                pspId, iuv, iur, createdFrom, createdTo, sort)
            .page(page);
    return getPagedResult(query);
  }
}
