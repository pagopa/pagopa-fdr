package it.gov.pagopa.fdr.rest.support.mapper;

import it.gov.pagopa.fdr.rest.model.FdrByPspIdIuvIurBase;
import it.gov.pagopa.fdr.rest.model.Metadata;
import it.gov.pagopa.fdr.service.dto.MetadataDto;
import it.gov.pagopa.fdr.service.dto.PaymentByPspIdIuvIurDTO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface SupportResourceServiceMapper {
  SupportResourceServiceMapper INSTANCE = Mappers.getMapper(SupportResourceServiceMapper.class);

  List<FdrByPspIdIuvIurBase> toFdrByIuvIurList(List<PaymentByPspIdIuvIurDTO> fdrGetPaymentDto);

  Metadata toMetadata(MetadataDto metadataDto);
}
