package it.gov.pagopa.fdr.repository.common;

import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
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

  protected static Sort getSort(SortField... sortColumns) {
    Sort sort = Sort.empty();
    if (sortColumns != null) {
      for (SortField sortColumn : sortColumns) {
        String column = sortColumn.getField();
        Direction direction = sortColumn.getDirection();
        if (!column.isBlank()) {
          if (direction != null) {
            sort.and(column, direction);
          } else {
            sort.and(column);
          }
        }
      }
    }
    return sort;
  }
}
