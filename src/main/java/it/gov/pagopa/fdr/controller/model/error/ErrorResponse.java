package it.gov.pagopa.fdr.controller.model.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@Jacksonized
@JsonPropertyOrder({"errorId", "httpStatusCode", "httpStatusDescription", "appErrorCode", "errors"})
@RegisterForReflection
public class ErrorResponse {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Schema(example = "50905466-1881-457b-b42f-fb7b2bfb1610")
  private String errorId;

  @Schema(example = "500")
  private int httpStatusCode;

  @Schema(example = "Internal Server Error")
  private String httpStatusDescription;

  @Schema(example = "FDR-500")
  private String appErrorCode;

  private List<ErrorMessage> errors;
}
