package it.gov.pagopa.fdr.service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FdrSimpleInternalDto {

  private String fdr;

  private String pspId;

  private Long revision;
}
