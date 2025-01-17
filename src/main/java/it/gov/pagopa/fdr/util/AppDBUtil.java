package it.gov.pagopa.fdr.util;

import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import java.util.List;

public class AppDBUtil {

  private AppDBUtil() {
    throw new IllegalStateException("Utility class");
  }

  // TODO to be removed
  public static Sort getSort(List<String> sortColumn) {
    Sort sort = Sort.empty();
    if (sortColumn != null && !sortColumn.isEmpty()) {
      sortColumn.stream()
          .filter(s -> s.replace(",", "").isBlank())
          .forEach(
              a -> {
                String[] split = a.split(",");
                String column = split[0].trim();
                String direction = split[1].trim();
                if (!column.isBlank()) {
                  if (direction.equalsIgnoreCase("asc")) {
                    sort.and(column, Direction.Ascending);
                  } else if (direction.equalsIgnoreCase("desc")) {
                    sort.and(column, Direction.Descending);
                  } else {
                    sort.and(column);
                  }
                }
              });
    }
    return sort;
  }
}
