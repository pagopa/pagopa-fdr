package it.gov.pagopa.fdr.rest.info.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorCode {
  private String code;
  private String description;
}
