package it.gov.pagopa.fdr.controller.model.flow.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import it.gov.pagopa.fdr.controller.model.flow.Receiver;
import it.gov.pagopa.fdr.controller.model.flow.Sender;
import it.gov.pagopa.fdr.controller.model.flow.enums.ReportingFlowStatusEnum;
import it.gov.pagopa.fdr.util.constant.ControllerConstants;
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
public class SingleFlowCreatedResponse {

  @Schema(example = "4", description = "The revision (or version) of the flow.")
  @JsonProperty(ControllerConstants.PARAMETER_REVISION)
  private Long revision;

  @Schema(
      example = "2025-01-01T12:00:30.900000Z",
      description = "The date and time on which the flow is created.")
  private Instant created;

  @Schema(
      example = "2025-01-01T12:10:30.900000Z",
      description = "The last date and time on which the flow is updated.")
  private Instant updated;

  @Schema(example = "CREATED", description = "The specific status of publication of the flow")
  private ReportingFlowStatusEnum status;

  @Schema(
      example = "2025-01-0188888888888-0000001",
      description = "The unique identifier of the flow.")
  @JsonProperty(ControllerConstants.PARAMETER_FDR)
  private String fdr;

  @Schema(
      example = "2025-01-01T12:20:30.800000Z",
      description = "The date related to the flow reporting.")
  private Instant fdrDate;

  @Schema(
      description =
          "The information related to the entity that will compile and send the published flow.")
  private Sender sender;

  @Schema(
      description = "The information related to the entity that will receive the published flow.")
  private Receiver receiver;

  @Schema(
      example = "SEPA - Bonifico X",
      description = "The description related to the regulation payment related to the flow.")
  private String regulation;

  @Schema(
      example = "2023-04-03T12:00:30.900000Z",
      description = "The date of the regulation payment related to the flow.")
  private Instant regulationDate;

  @Schema(
      example = "UNCRITMMXXX",
      description =
          "The BIC code of the bank where the regulation payment related to the flow will be"
              + " poured.")
  private String bicCodePouringBank;

  @Schema(
      example = "93",
      description =
          "The computed total number of payments included in the flow during the compilation.")
  public Long computedTotPayments;

  @Schema(
      example = "81.66",
      description =
          "The computed total amount of payments calculated in the flow during the compilation.")
  public Double computedSumPayments;

  @Schema(
      example = "100",
      description =
          "The total number of payments to be included in the flow during the flow compilation.")
  public Long totPayments;

  @Schema(
      example = "100.95",
      description =
          "The total amount of payments to be calculated in the flow during the flow compilation.")
  public Double sumPayments;
}
