package it.gov.pagopa.fdr.rest.reportingFlow.response;

import it.gov.pagopa.fdr.rest.reportingFlow.model.Receiver;
import it.gov.pagopa.fdr.rest.reportingFlow.model.Sender;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowStatusEnumDto;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@Jacksonized
public class GetIdResponse {

  @Schema(example = "643accaa4733f71aea4c71bf")
  public String id;

  @Schema(example = "643accaa4733f71aea4c71bf")
  public ReportingFlowStatusEnumDto status;

  @Schema(example = "60000000001-1173")
  private String reportingFlow;

  @Schema(example = "2023-04-05T09:21:37.810000Z")
  private Instant dateReportingFlow;

  private Sender sender;

  private Receiver receiver;

  @Schema(example = "SEPA - Bonifico xzy")
  private String regulation;

  @Schema(example = "2023-04-03T12:00:30.900000Z")
  private Instant dateRegulation;

  @Schema(example = "UNCRITMMXXX")
  private String bicCodePouringBank;
}
