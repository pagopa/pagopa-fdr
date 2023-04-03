package it.gov.pagopa.fdr.service.upload;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.repository.entity.flow.Flow;
import it.gov.pagopa.fdr.repository.entity.flow.FlowStatusEnum;
import it.gov.pagopa.fdr.rest.upload.request.CloseChunkRequest;
import it.gov.pagopa.fdr.rest.upload.request.UploadChunkRequest;
import it.gov.pagopa.fdr.rest.upload.request.UploadRequest;
import it.gov.pagopa.fdr.service.upload.dto.FlowDto;
import it.gov.pagopa.fdr.service.upload.dto.FlowDtoStatusEnum;
import it.gov.pagopa.fdr.service.upload.mapper.FlowEntityServiceMapper;
import it.gov.pagopa.fdr.util.AppFileUtil;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class UploadService {

  @Inject FlowEntityServiceMapper mapper;

  @Inject Logger log;

  @WithSpan(kind = SERVER)
  public void validateUploadRequest(UploadRequest uploadRequest) {
    log.debug("Validate uploadRequest");
  }

  @WithSpan(kind = SERVER)
  public void validateUploadChunkRequest(UploadChunkRequest uploadChunkRequest) {
    log.debug("Validate uploadChunkRequest");
  }

  @WithSpan(kind = SERVER)
  public void validateCloseChunkRequest(CloseChunkRequest closeChunkRequest) {
    log.debug("Validate closeChunkRequest");
  }

  @WithSpan(kind = SERVER)
  public void moveFile(Path source, Path target, Path targetDirectory) {
    log.debugf("Create directory [%s]", targetDirectory.toString());
    AppFileUtil.createDirectoryIfNotExist(targetDirectory);

    log.debugf("Moving file from [%s] to [%s]", source.toString(), target.toString());
    AppFileUtil.moveFile(source, target);
  }

  public void notifyUpload(String idFlow) {
    log.debugf("Notify - async validation idFlow: [%s]", idFlow);
    // send to e-hub
  }

  @WithSpan(kind = SERVER)
  public void validateUploadChunk(UploadChunkRequest uploadChunkRequest) {}

  public void save(FlowDto flowDto) {
    log.debugf("Save data on DB, idFlow: [%s]", flowDto.getIdFlow());

    Instant now = Instant.now();

    Optional<Flow> byIdOptional = Flow.findByIdOptional(flowDto.getIdFlow());
    Flow flow =
        byIdOptional.orElseGet(
            () -> {
              Flow flowInt = mapper.toFlow(flowDto);
              flowInt.created = now;
              flowInt.updated = Instant.now();
              return flowInt;
            });
    flow.updated = now;
    flow.flowFiles.add(mapper.toFlowFile(flowDto));

    flow.persist();
  }

  public void updateStatus(String idFlow, FlowDtoStatusEnum status) {
    log.debugf("Update status idFlow: [%s]", idFlow);

    Optional<Flow> byIdOptional = Flow.findByIdOptional(idFlow);
    Flow flow =
        byIdOptional.orElseThrow(
            () ->
                new AppException(
                    AppErrorCodeMessageEnum.UPLOAD_CLOSE_PARTIAL_CHUNK_FLOW_NOT_FOUND, idFlow));

    flow.status = FlowStatusEnum.TO_VALIDATE;

    flow.persist();
  }
}
