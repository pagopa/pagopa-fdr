package it.gov.pagopa.fdr.rest.reportingFlow.request;

import it.gov.pagopa.fdr.rest.reportingFlow.request.model.Pagamento;
import it.gov.pagopa.fdr.rest.reportingFlow.request.model.Receiver;
import it.gov.pagopa.fdr.rest.reportingFlow.request.model.Sender;
import it.gov.pagopa.fdr.util.CheckInstantFormat;
import it.gov.pagopa.fdr.util.OrSize;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@Jacksonized
public class CreateRequest {
  @NotNull(message = "reporting-flow.create.reportingFlow.notNull")
  @NotBlank(message = "reporting-flow.create.reportingFlow.notBlank|${validatedValue}")
  @Schema(example = "60000000001-1173")
  public String reportingFlow;

  @NotNull(message = "reporting-flow.create.dateReportingFlow.notNull ")
  @CheckInstantFormat(
      message = "reporting-flow.create.dateReportingFlow.checkInstantFormat|${validatedValue}")
  @Schema(example = "2023-04-05T09:21:37.810000Z")
  public String dateReportingFlow;

  private Sender sender;
  private Receiver receiver;

  @NotNull(message = "reporting-flow.create.regulation.notNull")
  @NotBlank(message = "reporting-flow.create.regulation.notBlank|${validatedValue}")
  @Schema(example = "SEPA - Bonifico xzy")
  private String regulation;

  @NotNull(message = "reporting-flow.create.dateRegulation.notNull")
  @CheckInstantFormat(
      message = "reporting-flow.create.dateRegulation.checkInstantFormat|${validatedValue}")
  @Schema(example = "2023-04-03T12:00:30.900000Z")
  private String dateRegulation;

  @Schema(example = "UNCRITMMXXX")
  private Optional<
          @OrSize(
              lengths = {8, 11},
              message =
                  "reporting-flow.create.bicCodePouringBank.orSize|${validatedValue}|{lengths}")
          String>
      bicCodePouringBank;

  private List<Pagamento> payments;
}
