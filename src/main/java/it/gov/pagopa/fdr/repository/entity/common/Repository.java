package it.gov.pagopa.fdr.repository.entity.common;

import io.quarkus.mongodb.panache.PanacheQuery;
import java.util.List;

public abstract class Repository {

  protected <T> RepositoryPagedResult<T> getPagedResult(PanacheQuery<T> query) {

    List<T> elements = query.list();
    long totalElements = query.count();
    long totalPages = query.pageCount();
    if (elements == null) {
      elements = List.of();
      totalElements = 0;
      totalPages = 0;
    }
    return RepositoryPagedResult.<T>builder()
        .data(elements)
        .totalElements(totalElements)
        .totalPages((int) totalPages)
        .build();
  }
}
