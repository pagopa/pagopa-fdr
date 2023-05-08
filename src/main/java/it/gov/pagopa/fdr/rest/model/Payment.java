package it.gov.pagopa.fdr.rest.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class Payment {

  @NotNull
  @Pattern(regexp = "^(\\w{1,35})$")
  @Schema(example = "abcdefg")
  private String iuv;

  @NotNull
  @Pattern(regexp = "^(\\w{1,35})$")
  @Schema(example = "abcdefg")
  private String iur;

  @NotNull
  @Min(value = 1)
  @Max(value = 5)
  @Schema(example = "1")
  private Long index;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = false)
  @Digits(integer = Integer.MAX_VALUE, fraction = 2)
  @Schema(example = "0.01")
  private Double pay;

  @NotNull
  @Schema(example = "EXECUTED")
  private PaymentStatusEnum payStatus;

  @NotNull
  @Schema(example = "2023-02-03T12:00:30.900000Z")
  private Instant payDate;
}
