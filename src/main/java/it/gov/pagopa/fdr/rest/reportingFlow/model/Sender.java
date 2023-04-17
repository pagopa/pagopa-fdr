package it.gov.pagopa.fdr.rest.reportingFlow.model;

import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class Sender {

  @NotNull(message = "reporting-flow.create.sender.type.notNull")
  //  @ValueOfEnum(
  //      enumClass = TipoIdentificativoUnivoco.class,
  //      message = "reporting-flow.create.sender.type.valueOfEnum|${validatedValue}")
  private TipoIdentificativoUnivoco type;

  private String id;
  private String name;

  private String idBroker;
  private String idChannel;
  private String password;
}
