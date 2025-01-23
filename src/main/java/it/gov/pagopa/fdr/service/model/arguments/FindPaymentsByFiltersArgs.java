package it.gov.pagopa.fdr.service.model.arguments;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FindPaymentsByFiltersArgs {

  private String pspId;
  private String iuv;
  private String iur;
  private Instant createdFrom;
  private Instant createdTo;
  private long pageNumber;
  private long pageSize;
}
