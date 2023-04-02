package it.gov.pagopa.fdr.rest.download.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import javax.ws.rs.core.MediaType;
import java.io.File;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloadResponse {
  @RestForm String name;

  @RestForm
  @PartType(MediaType.APPLICATION_OCTET_STREAM)
  File file;
}
