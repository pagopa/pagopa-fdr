package it.gov.pagopa.fdr.service.psps.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentDto {

  private String iuv;

  private String iur;

  private Long index;

  private Double pay;

  private PaymentStatusEnumDto payStatus;

  private Instant payDate;
}
