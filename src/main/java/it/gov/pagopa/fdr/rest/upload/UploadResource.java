package it.gov.pagopa.fdr.rest.upload;

import it.gov.pagopa.fdr.rest.upload.request.UploadRequest;
import it.gov.pagopa.fdr.rest.upload.response.UploadResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;import java.io.IOException;import java.nio.file.Files;import java.nio.file.Paths;import java.nio.file.StandardCopyOption;

@Path("/upload")
@Tag(name = "Upload", description = "Upload operations")
public class UploadResource {

  @Inject Logger log;

  @POST
  public UploadResponse upload(
      @RestForm String description,
      @RestForm("file") FileUpload file,
      @RestForm @PartType(MediaType.APPLICATION_JSON) UploadRequest uploadRequest) {
    log.infof("Upload file: [%s] size: []", file.fileName(), file.size());
    try{
      Files.move(file.filePath(), Paths.get("/Users/massimoscattarella/projects/pagopa/pagopa-fdr/target/"+file.fileName()), StandardCopyOption.REPLACE_EXISTING);
    }catch (IOException e) {
      throw new RuntimeException(e);
    }
    return UploadResponse.builder().name(file.fileName()).build();
  }
}
