package it.gov.pagopa.fdr.service.middleware.mapper;

import it.gov.pagopa.fdr.repository.entity.re.BlobRefEntity;
import it.gov.pagopa.fdr.repository.entity.re.ReEventEntity;
import it.gov.pagopa.fdr.service.model.re.BlobHttpBody;
import it.gov.pagopa.fdr.service.model.re.ReEvent;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface ReEventMapper {

  String PATTERN_DATE_FORMAT = "yyyy-MM-dd";
  DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern(PATTERN_DATE_FORMAT).withZone(ZoneId.systemDefault());

  ReEventMapper INSTANCE = Mappers.getMapper(ReEventMapper.class);

  default ReEventEntity toEntity(ReEvent reEvent) {

    BlobHttpBody reqBlobBodyRef = reEvent.getReqBodyRef();
    BlobRefEntity reqRefEntity = new BlobRefEntity();
    reqRefEntity.setStorageAccount(reqBlobBodyRef.getStorageAccount());
    reqRefEntity.setContainerName(reqBlobBodyRef.getContainerName());
    reqRefEntity.setFileName(reqBlobBodyRef.getFileName());
    reqRefEntity.setFileLength(reqBlobBodyRef.getFileLength());

    BlobHttpBody resBlobBodyRef = reEvent.getResBodyRef();
    BlobRefEntity resRefEntity = new BlobRefEntity();
    resRefEntity.setStorageAccount(resBlobBodyRef.getStorageAccount());
    resRefEntity.setContainerName(resBlobBodyRef.getContainerName());
    resRefEntity.setFileName(resBlobBodyRef.getFileName());
    resRefEntity.setFileLength(resBlobBodyRef.getFileLength());

    String formattedCreationDate = DATE_FORMATTER.format(reEvent.getCreated());

    ReEventEntity converted = new ReEventEntity();
    converted.setPartitionKey(formattedCreationDate);
    converted.setUniqueId(String.format("%s_%s", formattedCreationDate, reEvent.hashCode()));
    converted.setFdr(reEvent.getFdr());
    converted.setFdrAction(nameOrNull(reEvent.getFdrAction()));
    converted.setServiceIdentifier(nameOrNull(reEvent.getServiceIdentifier()));
    converted.setCreated(reEvent.getCreated().toString());
    converted.setSessionId(reEvent.getSessionId());
    converted.setEventType(nameOrNull(reEvent.getEventType()));
    converted.setPspId(reEvent.getPspId());
    converted.setOrganizationId(reEvent.getOrganizationId());
    converted.setFdrStatus(nameOrNull(reEvent.getFdrStatus()));
    converted.setHttpMethod(reEvent.getHttpMethod());
    converted.setHttpUrl(reEvent.getHttpUrl());
    converted.setReqBlobBodyRef(reqRefEntity);
    converted.setResBlobBodyRef(resRefEntity);
    converted.setHeader(reEvent.getHeader());

    Integer revision = null;
    if (reEvent.getRevision() != null) {
      revision = Math.toIntExact(reEvent.getRevision());
    }
    converted.setRevision(revision);

    return converted;
  }

  private static String nameOrNull(Enum<?> value) {
    return value != null ? value.name() : null;
  }
}
