package it.gov.pagopa.fdr.repository.entity.common;

import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

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

  @SafeVarargs
  protected static Sort getSort(Pair<String, Direction>... sortColumns) {
    Sort sort = Sort.empty();
    if (sortColumns != null) {
      for (Pair<String, Direction> sortColumn : sortColumns) {
        String column = sortColumn.getLeft();
        Direction direction = sortColumn.getRight();
        if (!column.isBlank()) {
          sort.and(column, direction);
        }
      }
    }
    return sort;
  }
}
