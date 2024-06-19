package it.gov.pagopa.fdr.rest.model;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@SuperBuilder
@Jacksonized
public class Sender extends SenderCommon {

  @Pattern(regexp = "^(\\w{8,15})$")
  @Schema(
      example = "1234567890",
      deprecated = true,
      description = "[XML NodoInviaFlussoRendicontazione]=[password]")
  private String password;
}
