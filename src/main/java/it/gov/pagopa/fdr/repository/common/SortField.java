package it.gov.pagopa.fdr.repository.common;

import io.quarkus.panache.common.Sort.Direction;
import lombok.Data;

@Data
public class SortField {

  private String field;

  private Direction direction;

  public static SortField of(String field, Direction direction) {
    SortField sortField = new SortField();
    sortField.setField(field);
    sortField.setDirection(direction);
    return sortField;
  }
}
