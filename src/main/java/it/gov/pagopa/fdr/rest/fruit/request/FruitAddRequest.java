package it.gov.pagopa.fdr.rest.fruit.request;

import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FruitAddRequest {

  @Size(min = 2, max = 3, message = "fruit.name.length.size|${validatedValue}|{min}|{max}")
  private String name;

  private String description;
}
