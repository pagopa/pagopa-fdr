package it.gov.pagopa.fdr.rest.fruit.request;

import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FruitAddRequest {
  @NotEmpty(message = "fruit.name.required")
  @Length(min = 2, max = 3, message = "{msg:fruit_name_length({name},2,3)}")
  private String name;

  private String description;
}
