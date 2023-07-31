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
import it.gov.pagopa.fdr.repository.fdr.projection.FdrHistoryProjection;
import it.gov.pagopa.fdr.service.dto.FdrAllInternalDto;
import it.gov.pagopa.fdr.service.dto.FdrGetDto;
import it.gov.pagopa.fdr.service.dto.FdrGetPaymentDto;
import it.gov.pagopa.fdr.service.dto.FdrSimpleInternalDto;
import it.gov.pagopa.fdr.service.dto.MetadataDto;
import it.gov.pagopa.fdr.service.organizations.mapper.InternalOrganizationsServiceServiceMapper;
import it.gov.pagopa.fdr.util.AppDBUtil;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
public class InternalOrganizationsService {

  @Inject InternalOrganizationsServiceServiceMapper mapper;

  @Inject Logger log;

  @WithSpan(kind = SERVER)
  public FdrAllInternalDto findByInternals(
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

    PanacheQuery<FdrHistoryEntity> fdrPanacheQuery;
    if (queryAnd.isEmpty()) {
      log.debugf("Get all FdrHistoryEntity");
      fdrPanacheQuery = FdrHistoryEntity.findAll(sort);
    } else {
      log.debugf("Get all FdrHistoryEntity with pspId[%s]", pspId);
      fdrPanacheQuery = FdrHistoryEntity.find(String.join(" and ", queryAnd), sort, parameters);
    }

    log.debug("Get paging FdrHistoryReportingFlowNameProjection");
    PanacheQuery<FdrHistoryProjection> fdrProjectionPanacheQuery =
        fdrPanacheQuery.page(page).project(FdrHistoryProjection.class);

    List<FdrHistoryProjection> reportingFlowIds = fdrProjectionPanacheQuery.list();

    long totPage = fdrProjectionPanacheQuery.pageCount();
    long countReportingFlow = fdrProjectionPanacheQuery.count();

    log.debug("Building ReportingFlowInternalDto");
    return FdrAllInternalDto.builder()
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
                        FdrSimpleInternalDto.builder()
                            .fdr(rf.getFdr())
                            .pspId(rf.getSender().getPspId())
                            .revision(rf.getRevision())
                            .build())
                .toList())
        .build();
  }

  @WithSpan(kind = SERVER)
  public FdrGetDto findByReportingFlowNameInternals(
      String action, String fdr, Long rev, String pspId) {
    log.infof(AppMessageUtil.logExecute(action));

    FdrHistoryEntity fdrEntity =
        FdrHistoryEntity.findByFdrAndRevAndPspId(fdr, rev, pspId)
            .project(FdrHistoryEntity.class)
            .firstResultOptional()
            .orElseThrow(
                () -> new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, fdr));

    log.debug("Mapping ReportingFlowGetDto from FdrHistoryEntity");
    return mapper.toFdrGetDtoByHistory(fdrEntity);
  }

  @WithSpan(kind = SERVER)
  public FdrGetPaymentDto findPaymentByFdrInternals(
      String action, String fdr, Long rev, String pspId, long pageNumber, long pageSize) {
    log.infof(AppMessageUtil.logExecute(action));

    Page page = Page.of((int) pageNumber - 1, (int) pageSize);
    Sort sort = AppDBUtil.getSort(List.of("index,asc"));

    log.debugf(
        "Existence check FdrPaymentHistoryEntity by fdr[%s], rev[%d], psp[%s]", fdr, rev, pspId);
    PanacheQuery<FdrPaymentHistoryEntity> fdrPaymentHistoryEntityPanacheQuery =
        FdrPaymentHistoryEntity.findByFdrAndRevAndPspId(fdr, rev, pspId, sort).page(page);

    List<FdrPaymentHistoryEntity> list = fdrPaymentHistoryEntityPanacheQuery.list();

    long totPage = fdrPaymentHistoryEntityPanacheQuery.pageCount();
    long countReportingFlowPayment = fdrPaymentHistoryEntityPanacheQuery.count();

    log.debug("Mapping ReportingFlowGetPaymentDto from FdrPaymentHistoryEntity");
    return FdrGetPaymentDto.builder()
        .metadata(
            MetadataDto.builder()
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .totPage(totPage)
                .build())
        .count(countReportingFlowPayment)
        .data(mapper.historyToPaymentDtoList(list))
        .build();
  }
}
