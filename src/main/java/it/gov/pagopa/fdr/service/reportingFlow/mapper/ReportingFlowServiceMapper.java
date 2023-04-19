package it.gov.pagopa.fdr.service.reportingFlow.mapper;

import it.gov.pagopa.fdr.repository.reportingFlow.Pagamento;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlow;
import it.gov.pagopa.fdr.repository.reportingFlow.projection.ReportingFlowNoPayment;
import it.gov.pagopa.fdr.repository.reportingFlow.projection.ReportingFlowOnlyPayment;
import it.gov.pagopa.fdr.service.reportingFlow.dto.PagamentoDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowGetDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowGetPaymentDto;
import java.util.List;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface ReportingFlowServiceMapper {

  ReportingFlowServiceMapper INSTANCE = Mappers.getMapper(ReportingFlowServiceMapper.class);

  ReportingFlow toReportingFlow(ReportingFlowDto reportingFlowDto);

  ReportingFlowGetDto toReportingFlowGetDto(ReportingFlowNoPayment reportingFlow);

  ReportingFlowGetPaymentDto toReportingFlowGetPaymentDto(ReportingFlowOnlyPayment reportingFlow);

  List<Pagamento> toPagamentos(List<PagamentoDto> pagamentoDto);

  default String toGetResponse(ObjectId reportingFlowGetDto) {
    return reportingFlowGetDto.toString();
  }
}
