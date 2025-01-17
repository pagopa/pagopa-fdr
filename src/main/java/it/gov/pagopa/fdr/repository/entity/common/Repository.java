package it.gov.pagopa.fdr.repository.entity.common;

import io.quarkus.mongodb.panache.PanacheQuery;

public abstract class Repository {

  protected <T> RepositoryPagedResult<T> getPagedResult(PanacheQuery<T> query, Class<T> clazz) {
    return RepositoryPagedResult.<T>builder()
        .data(query.list())
        .totalElements(query.count())
        .totalPages(query.pageCount())
        .build();
  }
}
