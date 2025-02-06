package it.gov.pagopa.fdr.repository.common;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

public abstract class Repository {

  @ConfigProperty(name = "quarkus.mongodb.custom.can-explain-queries", defaultValue = "true")
  boolean canExplainQueries;

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

  public void explainQuery(
      Logger log, MongoCollection<?> collection, String query, Parameters parameters) {

    if (canExplainQueries) {
      Bson filter = generateFilterFromQuery(query, parameters);
      Document explainDocument = collection.find(filter).explain();
      log.infof("Explain query [%s] : %s", query, explainDocument);
    }
  }

  public static Bson generateFilterFromQuery(String query, Parameters parameters) {
    List<Bson> filters = new ArrayList<>();

    //
    Pattern pattern = Pattern.compile("([a-zA-Z0-9_\\.]+)\\s*(=|!=|in)\\s*(:[a-zA-Z0-9_]+|'.+?')");
    Matcher matcher = pattern.matcher(query);

    while (matcher.find()) {
      String field = matcher.group(1); // The field
      String operator = matcher.group(2); // The operator
      String valueKey = matcher.group(3); // The value

      Object value;
      if (valueKey.startsWith(":")) {
        String paramName = valueKey.substring(1);
        value = parameters.map().get(paramName);
      } else {
        value = valueKey.replace("'", "");
      }

      switch (operator) {
        case "=":
          filters.add(Filters.eq(field, value));
          break;
        case "!=":
          filters.add(Filters.ne(field, value));
          break;
        case "in":
          if (value instanceof List<?>) {
            filters.add(Filters.in(field, (List<?>) value));
          } else if (value instanceof Set<?>) {
            filters.add(Filters.in(field, (Set<?>) value));
          }
          break;
        default:
          throw new UnsupportedOperationException("Unsupported operator [" + operator + "]");
      }
    }

    return Filters.and(filters); // Combina tutti i filtri
  }
}
