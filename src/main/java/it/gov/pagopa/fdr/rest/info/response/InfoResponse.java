package it.gov.pagopa.fdr.rest.info.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class InfoResponse {
  private String name;
  private String version;
  private String environment;

  private String description;

  private List<ErrorCode> errorCodes;

  @Builder
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ErrorCode {
    private String code;
    private String description;
    private int statusCode;
  }
}
