package it.gov.pagopa.fdr.service.middleware.mapper;

import it.gov.pagopa.fdr.controller.model.common.Metadata;
import it.gov.pagopa.fdr.controller.model.payment.Payment;
import it.gov.pagopa.fdr.controller.model.payment.enums.PaymentStatusEnum;
import it.gov.pagopa.fdr.controller.model.payment.response.PaginatedPaymentsResponse;
import it.gov.pagopa.fdr.repository.entity.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.payment.FdrPaymentEntity;
import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface PaymentMapper {

  PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

  default List<Payment> toPayment(List<FdrPaymentEntity> list) {

    List<Payment> converted = new ArrayList<>();
    for (FdrPaymentEntity entity : list) {
      converted.add(
          Payment.builder()
              .index(entity.getIndex())
              .iuv(entity.getIuv())
              .iur(entity.getIur())
              .idTransfer(entity.getTransferId())
              .pay(entity.getAmount())
              .payStatus(PaymentStatusEnum.valueOf(entity.getPayStatus().name()))
              .payDate(entity.getPayDate())
              .build());
    }
    return converted;
  }

  default PaginatedPaymentsResponse toPaginatedPaymentsResponse(
      RepositoryPagedResult<FdrPaymentEntity> paginatedResult, long pageSize, long pageNumber) {

    return PaginatedPaymentsResponse.builder()
        .metadata(
            Metadata.builder()
                .pageSize((int) pageSize)
                .pageNumber((int) pageNumber)
                .totPage(paginatedResult.getTotalPages())
                .build())
        .count(paginatedResult.getTotalElements())
        .data(toPayment(paginatedResult.getData()))
        .build();
  }
}
