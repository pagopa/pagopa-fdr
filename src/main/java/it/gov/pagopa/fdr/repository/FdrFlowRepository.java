package it.gov.pagopa.fdr.repository;

import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import it.gov.pagopa.fdr.repository.entity.common.Repository;
import it.gov.pagopa.fdr.repository.entity.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.flow.FdrFlowEntity;
import it.gov.pagopa.fdr.repository.enums.FdrStatusEnum;
import it.gov.pagopa.fdr.util.StringUtil;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

@ApplicationScoped
public class FdrFlowRepository extends Repository {

  /**
   * @param organizationId *mandatory*
   * @param pspId
   * @param publishedGt
   * @param pageNumber *mandatory*
   * @param pageSize *mandatory*
   * @return
   */
  public RepositoryPagedResult<FdrFlowEntity> findPublishedByOrganizationIdAndPspId(
      String organizationId, String pspId, Instant publishedGt, int pageNumber, int pageSize) {

    Parameters parameters = new Parameters();
    List<String> queryBuilder = new ArrayList<>();

    // setting mandatory field: organization id
    queryBuilder.add("receiver.organization_id = :organizationId");
    parameters.and("organizationId", organizationId);

    // setting optional field: PSP id
    if (!StringUtil.isNullOrBlank(pspId)) {
      queryBuilder.add("sender.psp_id = :pspId");
      parameters.and("pspId", pspId);
    }

    // setting optional field: publish date
    if (publishedGt != null) {
      queryBuilder.add("published > :publishedGt");
      parameters.and("publishedGt", publishedGt);
    }

    // setting mandatory field: flow status
    queryBuilder.add("status = :status");
    parameters.and("status", FdrStatusEnum.PUBLISHED);

    Page page = Page.of(pageNumber - 1, pageSize);
    Sort sort = getSort(Pair.of("_id", Direction.Ascending));

    String queryString = String.join(" and ", queryBuilder);

    PanacheQuery<FdrFlowEntity> query =
        FdrFlowEntity.findByQuery(queryString, sort, parameters).page(page);
    return getPagedResult(query);
  }
}
