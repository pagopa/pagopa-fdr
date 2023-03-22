package it.gov.pagopa.fdr.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"errorId", "httpStatusCode", "httpStatusDescription", "appErrorCode", "errors"})
@RegisterForReflection
public class ErrorResponse {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String errorId;

  private int httpStatusCode;
  private String httpStatusDescription;

  private String appErrorCode;
  private List<ErrorMessage> errors;

  @Builder
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @JsonPropertyOrder({"path", "message"})
  @RegisterForReflection
  public static class ErrorMessage {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String path;

    private String message;
  }
}
