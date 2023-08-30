package it.gov.pagopa.fdr.service.support;

import java.time.Instant;
import lombok.Builder;

@Builder
public class FindPaymentsByPspIdAndIuvIurArgs {
  public String action;
  public String pspId;
  public String iuv;
  public String iur;
  public Instant createdFrom;
  public Instant createdTo;
  public long pageNumber;
  public long pageSize;
}
