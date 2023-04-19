package it.gov.pagopa.fdr.rest.reportingFlow.model;

import it.gov.pagopa.fdr.util.validation.ListSize;
import it.gov.pagopa.fdr.util.validation.OrSize;
import java.time.Instant;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@SuperBuilder
@Jacksonized
public class ReportingFlow {
  @NotNull(message = "reporting-flow.create.reportingFlow.notNull")
  @NotBlank(message = "reporting-flow.create.reportingFlow.notBlank|${validatedValue}")
  @Schema(example = "60000000001-1173")
  private String reportingFlow;

  @NotNull(message = "reporting-flow.create.dateReportingFlow.notNull ")
  @Schema(example = "2023-04-05T09:21:37.810000Z")
  private Instant dateReportingFlow;

  @NotNull(message = "reporting-flow.create.sender.notNull")
  @Valid
  private Sender sender;

  @NotNull(message = "reporting-flow.create.receiver.notNull")
  @Valid
  private Receiver receiver;

  @NotNull(message = "reporting-flow.create.regulation.notNull")
  @NotBlank(message = "reporting-flow.create.regulation.notBlank|${validatedValue}")
  @Schema(example = "SEPA - Bonifico xzy")
  private String regulation;

  @NotNull(message = "reporting-flow.create.dateRegulation.notNull")
  @Schema(example = "2023-04-03T12:00:30.900000Z")
  private Instant dateRegulation;

  @Schema(example = "UNCRITMMXXX")
  @OrSize(
      lengths = {8, 11},
      message = "reporting-flow.create.bicCodePouringBank.orSize|${validatedValue}|{lengths}")
  private String bicCodePouringBank;

  @NotNull(message = "reporting-flow.create.payments.notNull")
  @ListSize(min = 1, max = 100, message = "reporting-flow.create.payments.listSize|{min}|{max}")
  @Valid
  private List<Pagamento> payments;
}
