package it.gov.pagopa.fdr.service.re.model;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ReInterface extends ReAbstract {

  private HttpTypeEnum httpType;

  private String httpMethod;

  private String httpUrl;

  private InputStream bodyRef;

  private BlobHttpBody blobBodyRef;

  private Map<String, List<String>> header;
}
