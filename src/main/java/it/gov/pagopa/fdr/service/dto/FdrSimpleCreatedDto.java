package it.gov.pagopa.fdr.service.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FdrSimpleCreatedDto {

  private String fdr;

  private String organizationId;

  private Long revision;

  private Instant created;
}
