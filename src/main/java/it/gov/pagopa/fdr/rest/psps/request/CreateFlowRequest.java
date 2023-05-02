package it.gov.pagopa.fdr.rest.psps.request;

import it.gov.pagopa.fdr.rest.model.Receiver;
import it.gov.pagopa.fdr.rest.model.Sender;
import java.time.Instant;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@Jacksonized
public class CreateFlowRequest {
  @NotNull
  @Pattern(regexp = "^\\S+$")
  @Schema(example = "60000000001-1173")
  private String reportingFlowName;

  @NotNull
  @Schema(example = "2023-04-05T09:21:37.810000Z")
  private Instant reportingFlowDate;

  @NotNull @Valid private Sender sender;

  @NotNull @Valid private Receiver receiver;

  @NotNull
  @Schema(example = "SEPA - Bonifico xzy")
  private String regulation;

  @NotNull
  @Schema(example = "2023-04-03T12:00:30.900000Z")
  private Instant regulationDate;

  @Schema(example = "UNCRITMMXXX")
  @Pattern(regexp = "^(\\w{8}|\\w{11})$")
  private String bicCodePouringBank;
}
