package it.gov.pagopa.fdr.service.re.model;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public class ReInternal extends ReAbstract {
  private boolean flowPhisicalDelete;
  private FlowStatusEnum flowStatus;

  private boolean flowRead;

  private String flowName;

  private String pspId;

  private Long revision;

  private FlowActionEnum flowAction;
}
