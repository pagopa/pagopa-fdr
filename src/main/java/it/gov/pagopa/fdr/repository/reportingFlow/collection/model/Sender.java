package it.gov.pagopa.fdr.repository.reportingFlow.collection.model;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
public class Sender {

  public TipoIdentificativoUnivoco type;

  @BsonProperty("id")
  public String id;

  public String idPsp;
  public String namePsp;

  public String idBroker;
  public String idChannel;
  public String password;
}
