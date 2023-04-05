package it.gov.pagopa.fdr.rest.upload;

import it.gov.pagopa.fdr.rest.upload.mapper.FlowRestServiceMapper;
import it.gov.pagopa.fdr.rest.upload.request.CloseChunkRequest;
import it.gov.pagopa.fdr.rest.upload.request.UploadChunkRequest;
import it.gov.pagopa.fdr.rest.upload.request.UploadRequest;
import it.gov.pagopa.fdr.rest.upload.response.CloseResponse;
import it.gov.pagopa.fdr.rest.upload.response.UploadChunkResponse;
import it.gov.pagopa.fdr.rest.upload.response.UploadResponse;
import it.gov.pagopa.fdr.service.upload.UploadService;
import it.gov.pagopa.fdr.service.upload.dto.FlowDtoStatusEnum;
import java.nio.file.Paths;
import java.time.Instant;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("/upload")
@Tag(name = "Upload", description = "Upload operations")
public class UploadResource {

  @Inject Logger log;

  @ConfigProperty(name = "app.upload-working-directory")
  String appUploadWorkingDirectory;

  @ConfigProperty(name = "app.upload-chunk-directory")
  String appUploadChunkDirectory;

  @Inject UploadService uploadService;

  @Inject FlowRestServiceMapper mapper;

  @Operation(summary = "Upload flow")
  @RequestBody(content = @Content(schema = @Schema(implementation = UploadSchema.class)))
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/BadRequest"),
        @APIResponse(responseCode = "415", description = "The provided file type is not supported"),
        @APIResponse(
            responseCode = "200",
            description = "OK",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UploadResponse.class)))
      })
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @POST
  public UploadResponse upload(
      @RestForm("file") @NotNull(message = "upload.file.not-null") FileUpload file,
      @RestForm("metadata")
          @PartType(MediaType.APPLICATION_JSON)
          @NotNull(message = "upload.metadata.not-null")
          @Valid
          UploadRequest uploadRequest) {

    Instant now = Instant.now();
    String date = uploadRequest.getDate();
    String idPsp = uploadRequest.getIdPsp();
    String idFlow = uploadRequest.getIdFlow();
    String fileName = file.fileName();

    long fileSize = file.size();

    log.infof(
        "Upload file - file-name: [%s], file-size: [%d], date: [%s], idPsp: [%s], id-flow: [%s]",
        fileName, fileSize, date, idPsp, idFlow);

    // validation
    uploadService.validateUploadRequest(uploadRequest);

    // store file
    String targetPath = String.format("%s/%s", appUploadWorkingDirectory, fileName);
    uploadService.moveFile(
        file.filePath(), Paths.get(targetPath), Paths.get(appUploadWorkingDirectory));

    // save metadata and status on DB
    String id =
        uploadService.save(
            mapper.toFlowDto(
                uploadRequest, now, fileName, fileSize, targetPath, FlowDtoStatusEnum.TO_VALIDATE));

    // notify uploaded to async process of validate
    uploadService.notifyUpload(idFlow);

    return UploadResponse.builder().id(id).received(now).build();
  }

  @Operation(summary = "Upload flow chunk")
  @RequestBody(content = @Content(schema = @Schema(implementation = UploadChunkSchema.class)))
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/BadRequest"),
        @APIResponse(responseCode = "415", description = "The provided file type is not supported"),
        @APIResponse(
            responseCode = "200",
            description = "OK",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UploadChunkResponse.class)))
      })
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @POST
  @Path("/chunk")
  public UploadChunkResponse uploadChunk(
      @RestForm("file") @NotNull(message = "upload.file.not-null") FileUpload file,
      @RestForm("metadata")
          @PartType(MediaType.APPLICATION_JSON)
          @NotNull(message = "upload.metadata.not-null")
          @Valid
          UploadChunkRequest uploadChunkRequest) {

    Instant now = Instant.now();
    String date = uploadChunkRequest.getDate();
    String idPsp = uploadChunkRequest.getIdPsp();
    String idFlow = uploadChunkRequest.getIdFlow();
    String fileName = file.fileName();
    long fileSize = file.size();
    int numberOfChunk = uploadChunkRequest.getNumberOfChunk();
    int totChunk = uploadChunkRequest.getTotChunk();

    log.infof(
        "Upload chunk file - file-name: [%s], file-size: [%d], date: [%s], idPsp: [%s], id-flow:"
            + " [%s], chunk: [%d/%d]",
        fileName, fileSize, date, idPsp, idFlow, numberOfChunk, totChunk);

    // validation
    uploadService.validateUploadChunkRequest(uploadChunkRequest);

    // store file
    String targetPath = String.format("%s/%s", appUploadChunkDirectory, fileName);
    uploadService.moveFile(
        file.filePath(), Paths.get(targetPath), Paths.get(appUploadChunkDirectory));

    // save on DB metadata and status
    String id =
        uploadService.save(
            mapper.toFlowDto(
                uploadChunkRequest,
                now,
                fileName,
                fileSize,
                targetPath,
                FlowDtoStatusEnum.PARTIAL_LOADED));

    return UploadChunkResponse.builder()
        .id(id)
        .received(now)
        .numberOfChunk(numberOfChunk)
        .totChunk(totChunk)
        .build();
  }

  @Operation(summary = "Close flow chunk")
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/BadRequest"),
        @APIResponse(
            responseCode = "200",
            description = "OK",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = CloseResponse.class)))
      })
  @POST
  @Path("/close-chunk")
  public CloseResponse closeChunk(CloseChunkRequest closeChunkRequest) {
    String id = closeChunkRequest.getId();

    // validation
    uploadService.validateCloseChunkRequest(closeChunkRequest);

    // save on DB metadata and status
    uploadService.updateStatus(id, FlowDtoStatusEnum.TO_VALIDATE);

    // notify uploaded to async process of validate
    uploadService.notifyUpload(id);

    return CloseResponse.builder().id(id).build();
  }

  // only for define openapi request
  @Schema(type = SchemaType.STRING, format = "binary")
  public interface UploadItemSchema {}

  public class UploadSchema {
    public UploadItemSchema file;
    public UploadRequest metadata;
  }

  public class UploadChunkSchema {
    public UploadItemSchema file;
    public UploadChunkRequest metadata;
  }
}