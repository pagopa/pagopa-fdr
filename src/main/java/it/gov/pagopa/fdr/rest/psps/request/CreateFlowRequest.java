package it.gov.pagopa.fdr.rest.psps.request;

import it.gov.pagopa.fdr.rest.model.Receiver;
import it.gov.pagopa.fdr.rest.model.Sender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@Jacksonized
public class CreateFlowRequest {
  @NotNull
  @Pattern(regexp = "[a-zA-Z0-9\\-_]{1,35}")
  @Schema(example = "2016-08-16pspTest-1178")
  private String reportingFlowName;

  @NotNull
  @Schema(example = "2023-04-05T09:21:37.810000Z")
  private Instant reportingFlowDate;

  @NotNull @Valid private Sender sender;

  @NotNull @Valid private Receiver receiver;

  @NotNull
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(example = "SEPA - Bonifico xzy")
  private String regulation;

  @NotNull
  @Schema(example = "2023-04-03T12:00:30.900000Z")
  private Instant regulationDate;

  @Schema(example = "UNCRITMMXXX")
  @Pattern(regexp = "^(.{1,35})$") // TODO non dovrebbe essere 5 numerici?
  private String bicCodePouringBank;
}
