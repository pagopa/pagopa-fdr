package it.gov.pagopa.fdr.rest.organizations.model;

import java.time.Instant;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@Jacksonized
public class Payment {

  @NotNull
  @Pattern(regexp = "^\\w+$")
  @Schema(example = "abcdefg")
  private String iuv;

  @NotNull
  @Pattern(regexp = "^\\w+$")
  @Schema(example = "abcdefg")
  private String iur;

  @NotNull
  @Schema(example = "1")
  private Long index;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = false)
  @Digits(integer = Integer.MAX_VALUE, fraction = 2)
  @Schema(example = "0.01")
  private Double pay;

  @NotNull
  @Schema(example = "PAGAMENTO_ESEGUITO")
  private PaymentStatusEnum payStatus;

  @NotNull
  @Schema(example = "2023-02-03T12:00:30.900000Z")
  private Instant payDate;
}
