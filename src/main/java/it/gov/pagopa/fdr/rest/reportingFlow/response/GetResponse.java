package it.gov.pagopa.fdr.rest.reportingFlow.response;

import it.gov.pagopa.fdr.rest.reportingFlow.model.ReportingFlow;
import it.gov.pagopa.fdr.rest.reportingFlow.model.ReportingFlowStatusEnum;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@SuperBuilder
@Jacksonized
public class GetResponse extends ReportingFlow {

  @Schema(example = "643accaa4733f71aea4c71bf")
  private String id;

  @Schema(example = "TO_VALIDATE")
  private ReportingFlowStatusEnum status;
}
