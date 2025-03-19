package it.gov.pagopa.fdr.service.middleware.mapper;

import it.gov.pagopa.fdr.repository.entity.FlowEntity;
import it.gov.pagopa.fdr.repository.entity.FlowToHistoryEntity;
import java.time.Instant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface FlowToHistoryMapper {

  default FlowToHistoryEntity toEntity(FlowEntity flow, Boolean isInternal) {

    Instant now = Instant.now();

    FlowToHistoryEntity entity = new FlowToHistoryEntity();

    entity.setPspId(flow.pspDomainId);
    entity.setName(flow.getName());
    entity.setRevision(flow.getRevision());
    entity.setIsExternal(!isInternal);
    entity.setCreated(now);
    entity.setLastExecution(now);
    entity.setRetries(0);

    return entity;
  }
}
