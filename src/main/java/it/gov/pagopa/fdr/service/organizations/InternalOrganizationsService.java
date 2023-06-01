package it.gov.pagopa.fdr.service.organizations;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.repository.fdr.FdrHistoryEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentPublishEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPublishEntity;
import it.gov.pagopa.fdr.repository.fdr.projection.FdrPublishReportingFlowNameProjection;
import it.gov.pagopa.fdr.service.dto.FlowInternalDto;
import it.gov.pagopa.fdr.service.dto.MetadataDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowGetDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowGetPaymentDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowInternalDto;
import it.gov.pagopa.fdr.service.organizations.mapper.InternalOrganizationsServiceServiceMapper;
import it.gov.pagopa.fdr.util.AppDBUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
public class InternalOrganizationsService {

  @Inject InternalOrganizationsServiceServiceMapper mapper;

  @Inject Logger log;

  @WithSpan(kind = SERVER)
  public ReportingFlowInternalDto findByInternals(
      String flowName, String pspId, long pageNumber, long pageSize) {
    log.debugf("Get all data from DB");

    Page page = Page.of((int) pageNumber - 1, (int) pageSize);
    Sort sort = AppDBUtil.getSort(List.of("_id,asc"));

    List<String> queryAnd = new ArrayList<>();
    Parameters parameters = new Parameters();
    if (pspId != null && !pspId.isBlank()) {
      queryAnd.add("sender.psp_id = :pspId");
      parameters.and("pspId", pspId);
    }
    if (flowName != null && !flowName.isBlank()) {
      queryAnd.add("reporting_flow_name = :flowName");
      parameters.and("flowName", flowName);
    }

    PanacheQuery<FdrHistoryEntity> reportingFlowPanacheQuery;
    if (queryAnd.isEmpty()) {
      reportingFlowPanacheQuery = FdrHistoryEntity.findAll(sort);
    } else {
      reportingFlowPanacheQuery =
          FdrHistoryEntity.find(String.join(" and ", queryAnd), sort, parameters);
    }

    PanacheQuery<FdrPublishReportingFlowNameProjection> reportingFlowNameProjectionPanacheQuery =
        reportingFlowPanacheQuery.page(page).project(FdrPublishReportingFlowNameProjection.class);

    List<FdrPublishReportingFlowNameProjection> reportingFlowIds =
        reportingFlowNameProjectionPanacheQuery.list();

    long totPage = reportingFlowNameProjectionPanacheQuery.pageCount();
    long countReportingFlow = reportingFlowNameProjectionPanacheQuery.count();

    return ReportingFlowInternalDto.builder()
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
                        FlowInternalDto.builder()
                            .name(rf.getReportingFlowName())
                            .pspId(rf.getSender().getPspId())
                            .revision(rf.getRevision())
                            .build())
                .toList())
        .build();
  }

  @WithSpan(kind = SERVER)
  public ReportingFlowGetDto findByReportingFlowNameInternals(
      String reportingFlowName, String pspId) {
    log.debugf("Get data from DB");

    FdrPublishEntity reportingFlowEntity =
        FdrPublishEntity.findByFlowNameAndPspId(reportingFlowName, pspId)
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
        FdrPaymentPublishEntity.findByFlowNameAndPspId(reportingFlowName, pspId, sort).page(page);

    List<FdrPaymentPublishEntity> list = reportingFlowPaymentEntityPanacheQuery.list();

    long totPage = reportingFlowPaymentEntityPanacheQuery.pageCount();
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
  public void changeInternalReadFlag(String reportingFlowName, String pspId) {
    log.debugf("Change read flag");

    Instant now = Instant.now();
    FdrPublishEntity reportingFlowEntity =
        FdrPublishEntity.findByFlowNameAndPspId(reportingFlowName, pspId)
            .project(FdrPublishEntity.class)
            .firstResultOptional()
            .orElseThrow(
                () ->
                    new AppException(
                        AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, reportingFlowName));
    reportingFlowEntity.setUpdated(now);
    reportingFlowEntity.setInternalNdpRead(Boolean.TRUE);
    reportingFlowEntity.update();
  }
}
