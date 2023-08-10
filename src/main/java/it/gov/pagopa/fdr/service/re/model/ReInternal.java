package it.gov.pagopa.fdr.service.re.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ReInternal extends ReAbstract {
  private boolean fdrPhysicalDelete;
  private FdrStatusEnum fdrStatus;

  private Long revision;
}
