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
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentHistoryEntity;
import it.gov.pagopa.fdr.repository.fdr.projection.FdrHistoryReportingFlowNameProjection;
import it.gov.pagopa.fdr.service.dto.FlowInternalDto;
import it.gov.pagopa.fdr.service.dto.MetadataDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowGetDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowGetPaymentDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowInternalDto;
import it.gov.pagopa.fdr.service.organizations.mapper.InternalOrganizationsServiceServiceMapper;
import it.gov.pagopa.fdr.util.AppDBUtil;
import it.gov.pagopa.fdr.util.AppMessageUtil;
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
      String action, String pspId, long pageNumber, long pageSize) {
    log.infof(AppMessageUtil.logExecute(action));

    Page page = Page.of((int) pageNumber - 1, (int) pageSize);
    Sort sort = AppDBUtil.getSort(List.of("_id,asc"));

    List<String> queryAnd = new ArrayList<>();
    Parameters parameters = new Parameters();
    if (pspId != null && !pspId.isBlank()) {
      queryAnd.add("sender.psp_id = :pspId");
      parameters.and("pspId", pspId);
    }

    PanacheQuery<FdrHistoryEntity> reportingFlowPanacheQuery;
    if (queryAnd.isEmpty()) {
      log.debugf("Get all FdrHistoryEntity");
      reportingFlowPanacheQuery = FdrHistoryEntity.findAll(sort);
    } else {
      log.debugf("Get all FdrHistoryEntity with pspId[%s]", pspId);
      reportingFlowPanacheQuery =
          FdrHistoryEntity.find(String.join(" and ", queryAnd), sort, parameters);
    }

    log.debug("Get paging FdrHistoryReportingFlowNameProjection");
    PanacheQuery<FdrHistoryReportingFlowNameProjection> reportingFlowNameProjectionPanacheQuery =
        reportingFlowPanacheQuery.page(page).project(FdrHistoryReportingFlowNameProjection.class);

    List<FdrHistoryReportingFlowNameProjection> reportingFlowIds =
        reportingFlowNameProjectionPanacheQuery.list();

    long totPage = reportingFlowNameProjectionPanacheQuery.pageCount();
    long countReportingFlow = reportingFlowNameProjectionPanacheQuery.count();

    log.debug("Building ReportingFlowInternalDto");
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
      String action, String reportingFlowName, Long rev, String pspId) {
    log.infof(AppMessageUtil.logExecute(action));

    FdrHistoryEntity reportingFlowEntity =
        FdrHistoryEntity.findByFlowNameAndRevAndPspId(reportingFlowName, rev, pspId)
            .project(FdrHistoryEntity.class)
            .firstResultOptional()
            .orElseThrow(
                () ->
                    new AppException(
                        AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, reportingFlowName));

    log.debug("Mapping ReportingFlowGetDto from FdrHistoryEntity");
    return mapper.toReportingFlowGetDto(reportingFlowEntity);
  }

  @WithSpan(kind = SERVER)
  public ReportingFlowGetPaymentDto findPaymentByReportingFlowNameInternals(
      String action,
      String reportingFlowName,
      Long rev,
      String pspId,
      long pageNumber,
      long pageSize) {
    log.infof(AppMessageUtil.logExecute(action));

    Page page = Page.of((int) pageNumber - 1, (int) pageSize);
    Sort sort = AppDBUtil.getSort(List.of("index,asc"));

    log.debugf(
        "Existence check FdrPaymentHistoryEntity by flowName[%s], rev[%d], psp[%s]",
        reportingFlowName, rev, pspId);
    PanacheQuery<FdrPaymentHistoryEntity> reportingFlowPaymentEntityPanacheQuery =
        FdrPaymentHistoryEntity.findByFlowNameAndRevAndPspId(reportingFlowName, rev, pspId, sort)
            .page(page);

    List<FdrPaymentHistoryEntity> list = reportingFlowPaymentEntityPanacheQuery.list();

    long totPage = reportingFlowPaymentEntityPanacheQuery.pageCount();
    long countReportingFlowPayment = reportingFlowPaymentEntityPanacheQuery.count();

    log.debug("Mapping ReportingFlowGetPaymentDto from FdrPaymentHistoryEntity");
    return ReportingFlowGetPaymentDto.builder()
        .metadata(
            MetadataDto.builder()
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .totPage(totPage)
                .build())
        .count(countReportingFlowPayment)
        .data(mapper.historyToPagamentoDtos(list))
        .build();
  }

  @WithSpan(kind = SERVER)
  public void changeInternalReadFlag(
      String action, String reportingFlowName, Long rev, String pspId) {
    log.infof(AppMessageUtil.logExecute(action));

    Instant now = Instant.now();
    log.debugf(
        "Existence check FdrHistoryEntity by flowName[%s], rev[%d], psp[%s]",
        reportingFlowName, rev, pspId);
    FdrHistoryEntity reportingFlowEntity =
        FdrHistoryEntity.findByFlowNameAndRevAndPspId(reportingFlowName, rev, pspId)
            .project(FdrHistoryEntity.class)
            .firstResultOptional()
            .orElseThrow(
                () ->
                    new AppException(
                        AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, reportingFlowName));
    reportingFlowEntity.setUpdated(now);
    reportingFlowEntity.setInternalNdpRead(Boolean.TRUE);
    reportingFlowEntity.update();
    log.debug("FdrHistoryEntity red");
  }
}
