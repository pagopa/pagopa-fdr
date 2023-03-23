package it.gov.pagopa.fdr.rest.fruit.request;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FruitAddRequest {
  @NotEmpty(message = "fruit.name.required")
  @Min(value = 2, message = "fruit.name.lenght.min")
  @Max(value = 3, message = "fruit.name.lenght.min")
  private String name;

  private String description;
}
