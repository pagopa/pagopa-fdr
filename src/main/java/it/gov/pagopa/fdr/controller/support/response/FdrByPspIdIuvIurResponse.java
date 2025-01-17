package it.gov.pagopa.fdr.controller.support.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import it.gov.pagopa.fdr.controller.model.FdrByPspIdIuvIurBase;
import it.gov.pagopa.fdr.controller.model.Metadata;
import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@SuperBuilder
@Jacksonized
@JsonPropertyOrder({"metadata", "count", "data"})
public class FdrByPspIdIuvIurResponse {

  private Metadata metadata;

  @Schema(example = "100")
  private long count;

  private List<FdrByPspIdIuvIurBase> data;
}
