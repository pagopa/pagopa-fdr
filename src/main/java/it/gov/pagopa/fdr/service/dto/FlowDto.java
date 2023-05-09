package it.gov.pagopa.fdr.service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FlowDto {

  private String name;

  private String pspId;
}
