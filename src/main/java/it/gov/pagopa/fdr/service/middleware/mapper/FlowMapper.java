package it.gov.pagopa.fdr.service.middleware.mapper;

import it.gov.pagopa.fdr.controller.model.common.Metadata;
import it.gov.pagopa.fdr.controller.model.flow.FlowByPSP;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowResponse;
import it.gov.pagopa.fdr.repository.entity.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.flow.FdrFlowEntity;
import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface FlowMapper {

  FlowMapper INSTANCE = Mappers.getMapper(FlowMapper.class);

  default List<FlowByPSP> toFlowByPSP(List<FdrFlowEntity> list) {

    List<FlowByPSP> converted = new ArrayList<>();
    for (FdrFlowEntity entity : list) {
      converted.add(
          FlowByPSP.builder()
              .fdr(entity.getName())
              .pspId(entity.getSender() != null ? entity.getSender().getPspId() : null)
              .revision(entity.getRevision())
              .published(entity.getPublished())
              .build());
    }
    return converted;
  }

  default PaginatedFlowsResponse toPaginatedFlowResponse(
      RepositoryPagedResult<FdrFlowEntity> paginatedResult, long pageSize, long pageNumber) {

    return PaginatedFlowsResponse.builder()
        .metadata(
            Metadata.builder()
                .pageSize((int) pageSize)
                .pageNumber((int) pageNumber)
                .totPage(paginatedResult.getTotalPages())
                .build())
        .count(paginatedResult.getTotalElements())
        .data(toFlowByPSP(paginatedResult.getData()))
        .build();
  }

  @Mapping(source = "name", target = "fdr")
  @Mapping(source = "totAmount", target = "sumPayments")
  @Mapping(source = "computedTotAmount", target = "computedSumPayments")
  SingleFlowResponse toSingleFlowResponse(FdrFlowEntity result);
}
