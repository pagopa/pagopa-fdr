package it.gov.pagopa.fdr.service.history.mapper;

import it.gov.pagopa.fdr.repository.fdr.FdrPaymentPublishEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPublishEntity;
import it.gov.pagopa.fdr.service.history.model.FdrHistoryEntity;
import it.gov.pagopa.fdr.service.history.model.FdrHistoryPaymentEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface HistoryServiceMapper {

  HistoryServiceMapper INSTANCE = Mappers.getMapper(HistoryServiceMapper.class);

  @Mapping(target = "paymentList", ignore = true)
  FdrHistoryEntity toFdrHistoryEntity(FdrPublishEntity fdrEntity);

  FdrHistoryPaymentEntity toFdrHistoryPaymentEntity(
      FdrPaymentPublishEntity fdrPaymentPublishEntity);

  List<FdrHistoryPaymentEntity> toFdrHistoryPaymentEntityList(
      List<FdrPaymentPublishEntity> fdrPaymentPublishEntities);
}
