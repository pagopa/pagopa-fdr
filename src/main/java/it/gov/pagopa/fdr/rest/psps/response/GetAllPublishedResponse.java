package it.gov.pagopa.fdr.rest.psps.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import it.gov.pagopa.fdr.rest.model.Fdr;
import it.gov.pagopa.fdr.rest.model.FdrPublished;
import it.gov.pagopa.fdr.rest.model.Metadata;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Getter
@SuperBuilder
@Jacksonized
@JsonPropertyOrder({"metadata", "count", "data"})
public class GetAllPublishedResponse {

  private Metadata metadata;

  @Schema(example = "100")
  private long count;

  private List<FdrPublished> data;
}