package it.gov.pagopa.fdr.rest.reportingFlow.model;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class Sender {

  private TipoIdentificativoUnivoco type;
  private String id;
  private String name;

  private String idBroker;
  private String idChannel;
  private String password;
}
