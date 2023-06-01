package it.gov.pagopa.fdr.service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FlowInternalDto {

  private String name;

  private String pspId;

  private Long revision;
}
