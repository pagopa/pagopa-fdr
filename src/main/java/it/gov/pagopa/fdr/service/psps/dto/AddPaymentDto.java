package it.gov.pagopa.fdr.service.psps.dto;

import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class AddPaymentDto {

  private List<PaymentDto> payments;
}
