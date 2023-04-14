package it.gov.pagopa.fdr.service.reportingFlow.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class SenderDto {

  private TipoIdentificativoUnivocoDto type;
  private String id;
  private String name;

  private String idBroker;
  private String idChannel;
  private String password;
}
