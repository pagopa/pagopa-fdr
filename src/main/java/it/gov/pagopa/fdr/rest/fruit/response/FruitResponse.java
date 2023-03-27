package it.gov.pagopa.fdr.rest.fruit.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FruitResponse {

  private String name;

  private String description;
}
