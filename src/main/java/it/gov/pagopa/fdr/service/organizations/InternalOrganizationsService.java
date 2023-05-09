package it.gov.pagopa.fdr.service.organizations;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.repository.reportingFlow.FdrPaymentPublishEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.FdrPublishEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.projection.FdrPublishReportingFlowNameProjection;
import it.gov.pagopa.fdr.service.dto.FlowDto;
import it.gov.pagopa.fdr.service.dto.MetadataDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowByIdEcDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowGetDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowGetPaymentDto;
import it.gov.pagopa.fdr.service.organizations.mapper.InternalOrganizationsServiceServiceMapper;
import it.gov.pagopa.fdr.util.AppDBUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
public class InternalOrganizationsService {

  @Inject InternalOrganizationsServiceServiceMapper mapper;

  @Inject Logger log;

  @WithSpan(kind = SERVER)
  public ReportingFlowByIdEcDto findByInternals(long pageNumber, long pageSize) {
    log.debugf("Get all data from DB");

    Page page = Page.of((int) pageNumber - 1, (int) pageSize);
    Sort sort = AppDBUtil.getSort(List.of("_id,asc"));

    PanacheQuery<FdrPublishEntity> reportingFlowPanacheQuery =
        FdrPublishEntity.find(
            "receiver.internal_ndp_read = :internalRead",
            sort,
            Parameters.with("internalRead", Boolean.TRUE).map());

    PanacheQuery<FdrPublishReportingFlowNameProjection> reportingFlowNameProjectionPanacheQuery =
        reportingFlowPanacheQuery.page(page).project(FdrPublishReportingFlowNameProjection.class);

    List<FdrPublishReportingFlowNameProjection> reportingFlowIds =
        reportingFlowNameProjectionPanacheQuery.list();

    long totPage = reportingFlowNameProjectionPanacheQuery.pageCount();
    long countReportingFlow = reportingFlowNameProjectionPanacheQuery.count();

    return ReportingFlowByIdEcDto.builder()
        .metadata(
            MetadataDto.builder()
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .totPage(totPage)
                .build())
        .count(countReportingFlow)
        .data(
            reportingFlowIds.stream()
                .map(
                    rf ->
                        FlowDto.builder()
                            .name(rf.reporting_flow_name)
                            .pspId(rf.sender.pspId)
                            .build())
                .toList())
        .build();
  }

  @WithSpan(kind = SERVER)
  public ReportingFlowGetDto findByReportingFlowNameInternals(
      String reportingFlowName, String pspId) {
    log.debugf("Get data from DB");

    FdrPublishEntity reportingFlowEntity =
        FdrPublishEntity.find(
                "reporting_flow_name = :flowName and sender.psp_id = :pspId",
                Parameters.with("flowName", reportingFlowName).and("pspId", pspId).map())
            .project(FdrPublishEntity.class)
            .firstResultOptional()
            .orElseThrow(
                () ->
                    new AppException(
                        AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, reportingFlowName));

    return mapper.toReportingFlowGetDto(reportingFlowEntity);
  }

  @WithSpan(kind = SERVER)
  public ReportingFlowGetPaymentDto findPaymentByReportingFlowNameInternals(
      String reportingFlowName, String pspId, long pageNumber, long pageSize) {
    log.debugf("Get data from DB");

    Page page = Page.of((int) pageNumber - 1, (int) pageSize);
    Sort sort = AppDBUtil.getSort(List.of("index,asc"));

    PanacheQuery<FdrPaymentPublishEntity> reportingFlowPaymentEntityPanacheQuery =
        FdrPaymentPublishEntity.find(
                "ref_fdr_reporting_flow_name = :flowName and sender.psp_id = :pspId",
                sort,
                Parameters.with("flowName", reportingFlowName).and("pspId", pspId).map())
            .page(page);

    List<FdrPaymentPublishEntity> list = reportingFlowPaymentEntityPanacheQuery.list();

    long totPage = Long.valueOf(reportingFlowPaymentEntityPanacheQuery.pageCount());
    long countReportingFlowPayment = reportingFlowPaymentEntityPanacheQuery.count();

    return ReportingFlowGetPaymentDto.builder()
        .metadata(
            MetadataDto.builder()
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .totPage(totPage)
                .build())
        .count(countReportingFlowPayment)
        .data(mapper.toPagamentoDtos(list))
        .build();
  }

  @WithSpan(kind = SERVER)
  public void changeInternalReadFlag(String pspId, String reportingFlowName) {
    log.debugf("Change read flag");

    Instant now = Instant.now();
    FdrPublishEntity reportingFlowEntity =
        FdrPublishEntity.find(
                "reporting_flow_name = :flowName and sender.psp_id = :pspId",
                Parameters.with("flowName", reportingFlowName).and("pspId", pspId).map())
            .project(FdrPublishEntity.class)
            .firstResultOptional()
            .orElseThrow(
                () ->
                    new AppException(
                        AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, reportingFlowName));
    reportingFlowEntity.updated = now;
    reportingFlowEntity.internal_ndp_read = Boolean.TRUE;
    reportingFlowEntity.update();
  }
}
