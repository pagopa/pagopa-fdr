package it.gov.pagopa.fdr.storage.model;

import it.gov.pagopa.fdr.controller.model.flow.Receiver;
import it.gov.pagopa.fdr.controller.model.flow.Sender;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FlowBlob {
  private String fdr;
  private Instant fdrDate;
  private Long revision;
  private Instant created;
  private Instant updated;
  private Instant published;
  private String status;
  private Sender sender;
  private Receiver receiver;
  private String regulation;
  private String regulationDate;
  private String bicCodePouringBank;
  private Long computedTotPayments;
  private BigDecimal computedSumPayments;
  private List<PaymentBlob> payments;
}
