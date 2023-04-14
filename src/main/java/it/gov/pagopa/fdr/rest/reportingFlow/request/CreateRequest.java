package it.gov.pagopa.fdr.rest.reportingFlow.request;

import it.gov.pagopa.fdr.rest.reportingFlow.request.model.Pagamento;
import it.gov.pagopa.fdr.rest.reportingFlow.request.model.Receiver;
import it.gov.pagopa.fdr.rest.reportingFlow.request.model.Sender;
import it.gov.pagopa.fdr.util.CheckInstantFormat;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.validator.constraints.Length;

@Getter
@Builder
@Jacksonized
public class CreateRequest {
  @NotNull(message = "reporting-flow.create.reportingFlow.not-null")
  @Length(min = 1, message = "reporting-flow.create.reportingFlow.length|${validatedValue}|{min}")
  @Schema(example = "1")
  public String reportingFlow;

  @NotNull(message = "reporting-flow.create.dateReportingFlow.not-null")
  @CheckInstantFormat(
      message = "reporting-flow.create.dateReportingFlow.format|${validatedValue}")
  public String dateReportingFlow;

  private Sender sender;
  private Receiver receiver;

  private String regulation;
  private Instant dateRegulation;

  private Optional<String> bicCodePouringBank;

  private List<Pagamento> payments;
}
