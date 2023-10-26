package it.gov.pagopa.fdr.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.fdr.util.AppConstant;
import lombok.Builder;
import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;

@Getter
@Builder
public class FdrPublished {

  @Schema(example = "AAABBB")
  @JsonProperty(AppConstant.FDR)
  private String fdr;

  @Schema(example = "1")
  @JsonProperty(AppConstant.ORGANIZATION)
  private String organizationId;

  @Schema(example = "1")
  @JsonProperty(AppConstant.REVISION)
  private Long revision;

  @Schema(example = "2023-04-03T12:00:30.900000Z")
  private Instant published;
}
