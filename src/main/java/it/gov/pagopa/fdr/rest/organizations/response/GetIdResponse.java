package it.gov.pagopa.fdr.rest.organizations.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import it.gov.pagopa.fdr.rest.model.Receiver;
import it.gov.pagopa.fdr.rest.model.ReportingFlowStatusEnum;
import it.gov.pagopa.fdr.rest.model.Sender;
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
  @Schema(example = "4")
  public Long revision;

  @Schema(example = "2023-04-03T12:00:30.900000Z")
  public Instant created;

  @Schema(example = "2023-04-03T12:00:30.900000Z")
  public Instant updated;

  @Schema(example = "CREATED")
  public ReportingFlowStatusEnum status;

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

  @Schema(example = "100")
  public Long totPayments;

  @Schema(example = "100.90")
  public Double sumPayments;
}
