package it.gov.pagopa.fdr.service.reportingFlow.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SenderDto {

  private TipoIdentificativoUnivocoDto type;
  private String id;

  public String idPsp;
  public String namePsp;

  private String idBroker;
  private String idChannel;
  private String password;
}
