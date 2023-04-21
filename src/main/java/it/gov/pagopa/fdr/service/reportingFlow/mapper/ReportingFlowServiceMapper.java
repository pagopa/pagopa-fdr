package it.gov.pagopa.fdr.service.reportingFlow.mapper;

import it.gov.pagopa.fdr.repository.reportingFlow.Pagamento;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlow;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowRevision;
import it.gov.pagopa.fdr.repository.reportingFlow.projection.ReportingFlowNoPayment;
import it.gov.pagopa.fdr.service.reportingFlow.dto.PagamentoDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowGetDto;
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

  List<Pagamento> toPagamentos(List<PagamentoDto> pagamentoDto);

  List<PagamentoDto> toPagamentoDtos(List<Pagamento> pagamentos);

  ReportingFlowRevision toReportingFlowRevision(ReportingFlow reportingFlow);

  default String toGetResponse(ObjectId reportingFlowGetDto) {
    return reportingFlowGetDto.toString();
  }
}
