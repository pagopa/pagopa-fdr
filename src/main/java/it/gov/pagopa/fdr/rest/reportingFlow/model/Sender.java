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
public class Sender {

  @NotNull(message = "reporting-flow.create.sender.type.notNull")
  private TipoIdentificativoUnivoco type;

  @NotNull(message = "reporting-flow.create.sender.id.notNull")
  @NotBlank(message = "reporting-flow.create.sender.id.notBlank|${validatedValue}")
  @Schema(example = "SELBIT2B")
  private String id;

  @NotNull(message = "reporting-flow.create.sender.idPsp.notNull")
  @NotBlank(message = "reporting-flow.create.sender.idPsp.notBlank|${validatedValue}")
  @Schema(example = "60000000001")
  private String idPsp;

  @NotNull(message = "reporting-flow.create.sender.namePsp.notNull")
  @NotBlank(message = "reporting-flow.create.sender.namePsp.notBlank|${validatedValue}")
  @Schema(example = "Bank")
  private String namePsp;

  @NotNull(message = "reporting-flow.create.sender.idBroker.notNull")
  @NotBlank(message = "reporting-flow.create.sender.idBroker.notBlank|${validatedValue}")
  @Schema(example = "70000000001")
  private String idBroker;

  @NotNull(message = "reporting-flow.create.sender.idChannel.notNull")
  @NotBlank(message = "reporting-flow.create.sender.idChannel.notBlank|${validatedValue}")
  @Schema(example = "80000000001")
  private String idChannel;

  @NotNull(message = "reporting-flow.create.sender.password.notNull")
  @NotBlank(message = "reporting-flow.create.sender.password.notBlank|${validatedValue}")
  @Schema(example = "1234567890", deprecated = true)
  private String password;
}
