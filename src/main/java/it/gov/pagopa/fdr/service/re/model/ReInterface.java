package it.gov.pagopa.fdr.service.re.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(
    callSuper = true,
    exclude = {"payload"})
@JsonIgnoreProperties(value = {"payload"})
public class ReInterface extends ReAbstract {

  private HttpTypeEnum httpType;

  private String httpMethod;

  private String httpUrl;

  private String payload;

  private BlobHttpBody blobBodyRef;

  private Map<String, List<String>> header;
}
