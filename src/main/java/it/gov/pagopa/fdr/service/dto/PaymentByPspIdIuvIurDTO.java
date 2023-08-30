package it.gov.pagopa.fdr.service.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentByPspIdIuvIurDTO {
  private String pspId;
  private String organizationId;
  private String fdr;
  private Long revision;
  private Instant created;
}
