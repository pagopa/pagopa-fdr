package it.gov.pagopa.fdr.rest.reportingFlow.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@Jacksonized
public class Receiver {

  @NotNull(message = "reporting-flow.create.receiver.id.notNull")
  @NotBlank(message = "reporting-flow.create.receiver.id.notBlank|${validatedValue}")
  @Schema(example = "APPBIT2B")
  private String id;

  @NotNull(message = "reporting-flow.create.receiver.idEc.notNull")
  @NotBlank(message = "reporting-flow.create.receiver.idEc.notBlank|${validatedValue}")
  @Schema(example = "20000000001")
  private String idEc;

  @NotNull(message = "reporting-flow.create.receiver.nameEc.notNull")
  @NotBlank(message = "reporting-flow.create.receiver.nameEc.notBlank|${validatedValue}")
  @Schema(example = "Comune di xyz")
  private String nameEc;
}