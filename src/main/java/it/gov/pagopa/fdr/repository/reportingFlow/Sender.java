package it.gov.pagopa.fdr.repository.reportingFlow;

import lombok.Data;

@Data
public class Sender {

  public TipoIdentificativoUnivoco type;
  public String id;

  public String idPsp;
  public String namePsp;

  public String idBroker;
  public String idChannel;
  public String password;
}
