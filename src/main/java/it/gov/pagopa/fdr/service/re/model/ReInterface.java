package it.gov.pagopa.fdr.service.re.model;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public class ReInterface extends ReAbstract {

  private HttpTypeEnum httpType;

  private String httpMethod;

  private String httpUrl;

  private String bodyRef;

  private Map<String, List<String>> header;
}
