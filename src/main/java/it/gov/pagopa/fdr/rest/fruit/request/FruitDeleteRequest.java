package it.gov.pagopa.fdr.rest.fruit.request;

import javax.validation.constraints.Min;import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FruitDeleteRequest {
  @NotEmpty(message = "fruit.name.required")
  @Length(min=2, max = 3, message = "fruit.name.length")
  private String name;
}
