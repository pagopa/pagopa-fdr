package it.gov.pagopa.fdr.rest.reportingFlow.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@JsonPropertyOrder({
  "status",
  "revision",
  "created",
  "updated",
  "reportingFlowName",
  "reportingFlowDate",
  "regulation",
  "regulationDate",
  "bicCodePouringBank",
  "sender",
  "receiver"
})
public class GetIdResponse {
  @Schema(example = "2023-04-05T09:21:37.810000Z")
  public Long revision;

  @Schema(example = "2023-04-03T12:00:30.900000Z")
  public Instant created;

  @Schema(example = "2023-04-03T12:00:30.900000Z")
  public Instant updated;

  @Schema(example = "643accaa4733f71aea4c71bf")
  public ReportingFlowStatusEnumDto status;

  @Schema(example = "60000000001-1173")
  private String reportingFlowName;

  @Schema(example = "2023-04-05T09:21:37.810000Z")
  private Instant reportingFlowDate;

  private Sender sender;

  private Receiver receiver;

  @Schema(example = "SEPA - Bonifico xzy")
  private String regulation;

  @Schema(example = "2023-04-03T12:00:30.900000Z")
  private Instant regulationDate;

  @Schema(example = "UNCRITMMXXX")
  private String bicCodePouringBank;
}
