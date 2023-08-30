package it.gov.pagopa.fdr.rest.support.mapper;

import it.gov.pagopa.fdr.rest.model.Metadata;
import it.gov.pagopa.fdr.rest.support.response.FdrByIur;
import it.gov.pagopa.fdr.rest.support.response.FdrByIuv;
import it.gov.pagopa.fdr.service.dto.MetadataDto;
import it.gov.pagopa.fdr.service.dto.PaymentByPspIdIurDTO;
import it.gov.pagopa.fdr.service.dto.PaymentByPspIdIuvDTO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface SupportResourceServiceMapper {
  SupportResourceServiceMapper INSTANCE =
      Mappers.getMapper(SupportResourceServiceMapper.class);
  List<FdrByIuv> toFdrByIuvList(List<PaymentByPspIdIuvDTO> fdrGetPaymentDto);
  List<FdrByIur> toFdrByIurList(List<PaymentByPspIdIurDTO> fdrGetPaymentDto);
  Metadata toMetadata(MetadataDto metadataDto);
}
