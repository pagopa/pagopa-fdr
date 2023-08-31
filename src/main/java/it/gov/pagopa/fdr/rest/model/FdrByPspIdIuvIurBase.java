package it.gov.pagopa.fdr.rest.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.Instant;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@SuperBuilder
@Jacksonized
@JsonPropertyOrder({"pspId", "organizationId", "fdr", "revision", "created"})
public class FdrByPspIdIuvIurBase {
  private String pspId;
  private String organizationId;
  private String fdr;
  private Long revision;
  private Instant created;
}
