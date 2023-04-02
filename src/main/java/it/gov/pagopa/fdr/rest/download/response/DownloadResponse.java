package it.gov.pagopa.fdr.rest.download.response;

import java.io.File;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;

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
