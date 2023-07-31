package it.gov.pagopa.fdr.service.conversion.message;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FdrMessage {

  private String fdr;

  private String pspId;

  private Long retry;

  private Long revision;
}
