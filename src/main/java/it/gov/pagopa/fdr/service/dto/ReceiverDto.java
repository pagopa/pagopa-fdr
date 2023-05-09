package it.gov.pagopa.fdr.service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReceiverDto {

  private String id;

  private String ecId;

  private String ecName;
}
