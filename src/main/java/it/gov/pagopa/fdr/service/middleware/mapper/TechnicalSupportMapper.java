package it.gov.pagopa.fdr.service.middleware.mapper;

import it.gov.pagopa.fdr.controller.model.flow.FlowBySenderAndReceiver;
import it.gov.pagopa.fdr.repository.entity.PaymentEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface TechnicalSupportMapper {

  TechnicalSupportMapper INSTANCE = Mappers.getMapper(TechnicalSupportMapper.class);

  List<FlowBySenderAndReceiver> toFlowBySenderAndReceiver(List<PaymentEntity> list);

  @Mapping(source = "flow.pspDomainId", target = "pspId")
  @Mapping(source = "flow.orgDomainId", target = "organizationId")
  @Mapping(source = "flow.name", target = "fdr")
  @Mapping(source = "flow.revision", target = "revision")
  FlowBySenderAndReceiver toFlowBySenderAndReceiver(PaymentEntity list);
}
