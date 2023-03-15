package it.gov.pagopa.fdr.rest.fruit.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FruitAddRequest {
    @NotEmpty(message = "{Fruit.name.required}")
    private String name;

    private String description;
}
