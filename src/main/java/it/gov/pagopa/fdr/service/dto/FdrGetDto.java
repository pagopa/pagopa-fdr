package it.gov.pagopa.fdr.service.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FdrGetDto {

  private Long revision;

  private Instant created;

  private Instant updated;

  private FdrStatusEnumDto status;

  private String fdr;

  private Instant fdrDate;

  private SenderDto sender;

  private ReceiverDto receiver;

  private String regulation;

  private Instant regulationDate;

  private String bicCodePouringBank;

  private Long totPayments;

  private Double sumPayments;
}
