package it.gov.pagopa.fdr.rest.upload.mapper;

import it.gov.pagopa.fdr.rest.upload.request.UploadChunkRequest;
import it.gov.pagopa.fdr.rest.upload.request.UploadRequest;
import it.gov.pagopa.fdr.service.upload.dto.FlowDto;
import it.gov.pagopa.fdr.service.upload.dto.FlowDtoStatusEnum;
import java.time.Instant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface FlowRestServiceMapper {

  FlowRestServiceMapper INSTANCE = Mappers.getMapper(FlowRestServiceMapper.class);

  default FlowDto toFlowDto(
      UploadRequest uploadRequest,
      Instant now,
      String fileName,
      long fileSize,
      String path,
      FlowDtoStatusEnum status) {
    return FlowDto.builder()
        .date(Instant.parse(uploadRequest.getDate()))
        .idPsp(uploadRequest.getIdPsp())
        .idFlow(uploadRequest.getIdFlow())
        .received(now)
        .fileName(fileName)
        .fileSize(fileSize)
        .path(path)
        .numberOfChunk(1)
        .totChunk(1)
        .status(status)
        .build();
  }

  default FlowDto toFlowDto(
      UploadChunkRequest uploadChunkRequest,
      Instant now,
      String fileName,
      long fileSize,
      String path,
      FlowDtoStatusEnum status) {
    return FlowDto.builder()
        .date(Instant.parse(uploadChunkRequest.getDate()))
        .idPsp(uploadChunkRequest.getIdPsp())
        .idFlow(uploadChunkRequest.getIdFlow())
        .received(now)
        .fileName(fileName)
        .fileSize(fileSize)
        .path(path)
        .numberOfChunk(uploadChunkRequest.getNumberOfChunk())
        .totChunk(uploadChunkRequest.getTotChunk())
        .status(status)
        .build();
  }
}
