package it.gov.pagopa.fdr.service.reportingFlow.dto;

import it.gov.pagopa.fdr.rest.reportingFlow.model.PaymentStatus;
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

  private BigDecimal payed;

  private PaymentStatus status;

  private Instant payedDate;
}
