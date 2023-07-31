package it.gov.pagopa.fdr.service.organizations;

import static io.opentelemetry.api.trace.SpanKind.SERVER;
import static it.gov.pagopa.fdr.util.MDCKeys.EC_ID;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentPublishEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPublishEntity;
import it.gov.pagopa.fdr.repository.fdr.projection.FdrPublishProjection;
import it.gov.pagopa.fdr.service.dto.FlowDto;
import it.gov.pagopa.fdr.service.dto.MetadataDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowByIdEcDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowGetDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowGetPaymentDto;
import it.gov.pagopa.fdr.service.organizations.mapper.OrganizationsServiceServiceMapper;
import it.gov.pagopa.fdr.util.AppDBUtil;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.jboss.logging.Logger;
import org.slf4j.MDC;

@ApplicationScoped
public class OrganizationsService {

  @Inject OrganizationsServiceServiceMapper mapper;

  @Inject Logger log;

  @WithSpan(kind = SERVER)
  public ReportingFlowByIdEcDto findByIdEc(
      String action, String ecId, String pspId, long pageNumber, long pageSize) {
    log.infof(AppMessageUtil.logExecute(action));

    Page page = Page.of((int) pageNumber - 1, (int) pageSize);
    Sort sort = AppDBUtil.getSort(List.of("_id,asc"));

    PanacheQuery<FdrPublishEntity> reportingFlowPanacheQuery;
    if (pspId == null || pspId.isBlank()) {
      log.debugf("Get all FdrPublishEntity by ecId[%s]", ecId);
      reportingFlowPanacheQuery = FdrPublishEntity.findByEcId(ecId, sort);
    } else {
      log.debugf("Get all FdrPublishEntity by ecId[%s], pspId[%s]", ecId, pspId);
      reportingFlowPanacheQuery = FdrPublishEntity.findByEcIdAndPspId(ecId, pspId, sort);
    }

    log.debug("Get paging FdrPublishReportingFlowNameProjection");
    PanacheQuery<FdrPublishProjection> reportingFlowNameProjectionPanacheQuery =
        reportingFlowPanacheQuery.page(page).project(FdrPublishProjection.class);

    List<FdrPublishProjection> reportingFlowIds = reportingFlowNameProjectionPanacheQuery.list();

    long totPage = reportingFlowNameProjectionPanacheQuery.pageCount();
    long countReportingFlow = reportingFlowNameProjectionPanacheQuery.count();

    log.debug("Building ReportingFlowByIdEcDto");
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
                            .name(rf.getReportingFlowName())
                            .pspId(rf.getSender().getPspId())
                            .build())
                .toList())
        .build();
  }

  @WithSpan(kind = SERVER)
  public ReportingFlowGetDto findByReportingFlowName(
      String action, String reportingFlowName, String pspId) {
    log.infof(AppMessageUtil.logExecute(action));

    log.debugf(
        "Existence check FdrPublishEntity by flowName[%s], psp[%s]", reportingFlowName, pspId);
    FdrPublishEntity reportingFlowEntity =
        FdrPublishEntity.findByFlowNameAndPspId(reportingFlowName, pspId)
            .project(FdrPublishEntity.class)
            .firstResultOptional()
            .orElseThrow(
                () ->
                    new AppException(
                        AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, reportingFlowName));

    MDC.put(EC_ID, reportingFlowEntity.getReceiver().getEcId());

    log.debug("Mapping ReportingFlowGetDto from FdrPublishEntity");
    return mapper.toReportingFlowGetDto(reportingFlowEntity);
  }

  @WithSpan(kind = SERVER)
  public ReportingFlowGetPaymentDto findPaymentByReportingFlowName(
      String action, String reportingFlowName, String pspId, long pageNumber, long pageSize) {
    log.infof(AppMessageUtil.logExecute(action));

    Page page = Page.of((int) pageNumber - 1, (int) pageSize);
    Sort sort = AppDBUtil.getSort(List.of("index,asc"));

    log.debugf(
        "Existence check FdrPaymentPublishEntity by flowName[%s], psp[%s]",
        reportingFlowName, pspId);
    PanacheQuery<FdrPaymentPublishEntity> reportingFlowPaymentEntityPanacheQuery =
        FdrPaymentPublishEntity.findByFlowNameAndPspId(reportingFlowName, pspId, sort).page(page);

    List<FdrPaymentPublishEntity> list = reportingFlowPaymentEntityPanacheQuery.list();

    long totPage = reportingFlowPaymentEntityPanacheQuery.pageCount();
    long countReportingFlowPayment = reportingFlowPaymentEntityPanacheQuery.count();

    log.debug("Mapping ReportingFlowGetPaymentDto from FdrPaymentPublishEntity");
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
}
