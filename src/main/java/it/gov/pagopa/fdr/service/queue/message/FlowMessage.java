package it.gov.pagopa.fdr.service.queue.message;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FlowMessage {

  private String name;

  private String pspId;

  private Long retry;

  private Long revision;
}
