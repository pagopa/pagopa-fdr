package it.gov.pagopa.fdr.repository;

import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import it.gov.pagopa.fdr.repository.entity.common.Repository;
import it.gov.pagopa.fdr.repository.entity.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.payment.FdrPaymentEntity;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import org.apache.commons.lang3.tuple.Pair;

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
    Sort sort = getSort(Pair.of("index", Direction.Ascending));

    PanacheQuery<FdrPaymentEntity> query =
        FdrPaymentEntity.executeQueryByPspAndIuvAndIur(
                pspId, iuv, iur, createdFrom, createdTo, sort)
            .page(page);
    return getPagedResult(query);
  }
}
