package it.gov.pagopa.fdr.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@Jacksonized
public class Sender {

  @NotNull private SenderTypeEnum type;

  @NotNull
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(example = "SELBIT2B")
  private String id;

  @NotNull
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(example = "60000000001")
  private String pspId;

  @NotNull
  @Pattern(regexp = "^(.{3,70})$")
  @Schema(example = "Bank")
  private String pspName;

  @NotNull
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(example = "70000000001")
  private String brokerId;

  @NotNull
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(example = "80000000001")
  private String channelId;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Pattern(regexp = "^(\\w{8,15})$")
  @Schema(example = "1234567890", deprecated = true)
  private String password;
}
