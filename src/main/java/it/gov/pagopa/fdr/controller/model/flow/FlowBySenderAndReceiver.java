package it.gov.pagopa.fdr.controller.model.flow;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.Instant;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@SuperBuilder
@Jacksonized
@JsonPropertyOrder({"pspId", "organizationId", "fdr", "revision", "created"})
public class FlowBySenderAndReceiver {

  private String pspId;

  private String organizationId;

  private String fdr;

  private Long revision;

  private Instant created;
}
