package it.gov.pagopa.fdr.service.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class FdrSimplePublishedDto {

  private String fdr;

  private String organizationId;

  private Long revision;

  private Instant published;
}
