package it.gov.pagopa.fdr.rest.fruit.request;

import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FruitDeleteRequest {
    @NotEmpty(message = "{Fruit.name.required}")
    private String name;

}
