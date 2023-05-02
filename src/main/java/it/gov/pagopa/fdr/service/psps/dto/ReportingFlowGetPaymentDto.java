package it.gov.pagopa.fdr.service.psps.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportingFlowGetPaymentDto {

  private MetadataDto metadata;

  private Long count;

  private List<PaymentDto> data;
}
