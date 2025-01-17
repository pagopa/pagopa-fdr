package it.gov.pagopa.fdr.controller.model.flow.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import it.gov.pagopa.fdr.controller.model.flow.Receiver;
import it.gov.pagopa.fdr.controller.model.flow.Sender;
import it.gov.pagopa.fdr.controller.model.flow.enums.ReportingFlowStatusEnum;
import it.gov.pagopa.fdr.util.AppConstant;
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
  "fdr",
  "fdrDate",
  "regulation",
  "regulationDate",
  "bicCodePouringBank",
  "sender",
  "receiver"
})
public class SingleFlowResponse {

  @Schema(example = "4")
  @JsonProperty(AppConstant.REVISION)
  private Long revision;

  @Schema(example = "2023-04-03T12:00:30.900000Z")
  private Instant created;

  @Schema(example = "2023-04-03T12:00:30.900000Z")
  private Instant updated;

  @Schema(example = "2023-04-03T12:00:30.900000Z")
  private Instant published;

  @Schema(example = "CREATED")
  private ReportingFlowStatusEnum status;

  @Schema(example = "2016-08-16pspTest-1178")
  @JsonProperty(AppConstant.FDR)
  private String fdr;

  @Schema(example = "2023-04-05T09:21:37.810000Z")
  private Instant fdrDate;

  private Sender sender;

  private Receiver receiver;

  @Schema(example = "SEPA - Bonifico xzy")
  private String regulation;

  @Schema(example = "2023-04-03T12:00:30.900000Z")
  private Instant regulationDate;

  @Schema(example = "UNCRITMMXXX")
  private String bicCodePouringBank;

  @Schema(example = "100")
  public Long computedTotPayments;

  @Schema(example = "100.90")
  public Double computedSumPayments;

  @Schema(example = "100")
  public Long totPayments;

  @Schema(example = "100.90")
  public Double sumPayments;
}
