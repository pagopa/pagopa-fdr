package it.gov.pagopa.fdr.rest.fruit.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FruitAddRequest {
  private String name;
  private String description;
}
