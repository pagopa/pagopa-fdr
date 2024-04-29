package it.gov.pagopa.fdr.service.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentDto {

  private Long index;

  private String iuv;

  private String iur;

  private Long idTransfer;

  private Double pay;

  private PaymentStatusEnumDto payStatus;

  private Instant payDate;
}
