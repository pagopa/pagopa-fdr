package it.gov.pagopa.fdr.service.reportingFlow.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentDto {

  private String iuv;

  private String iur;

  private Long index;

  private BigDecimal pay;

  private PaymentStatusDto payStatus;

  private Instant payDate;
}
