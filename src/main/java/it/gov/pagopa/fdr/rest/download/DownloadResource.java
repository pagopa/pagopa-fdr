package it.gov.pagopa.fdr.rest.download;

import it.gov.pagopa.fdr.rest.download.response.DownloadResponse;
import java.io.File;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/download")
@Tag(name = "Download", description = "Download operations")
public class DownloadResource {
  @GET
  @Produces(MediaType.MULTIPART_FORM_DATA)
  public DownloadResponse getFile() {

    return DownloadResponse.builder()
        .name("Chart.yaml")
        .file(new File("/Users/massimoscattarella/projects/pagopa/pagopa-fdr/target/Chart.yaml"))
        .build();
  }
}
