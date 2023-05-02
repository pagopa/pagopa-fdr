package it.gov.pagopa.fdr.service.psps.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReceiverDto {

  private String id;

  private String ecId;

  private String ecName;
}
