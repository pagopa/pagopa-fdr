package it.gov.pagopa.fdr.service.organizations;

import static io.opentelemetry.api.trace.SpanKind.SERVER;
import static it.gov.pagopa.fdr.util.MDCKeys.ORGANIZATION_ID;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentPublishEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPublishEntity;
import it.gov.pagopa.fdr.repository.fdr.projection.FdrPublishProjection;
import it.gov.pagopa.fdr.service.dto.FdrAllDto;
import it.gov.pagopa.fdr.service.dto.FdrGetDto;
import it.gov.pagopa.fdr.service.dto.FdrGetPaymentDto;
import it.gov.pagopa.fdr.service.dto.FdrSimpleDto;
import it.gov.pagopa.fdr.service.dto.MetadataDto;
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
  public FdrAllDto findByIdEc(
      String action, String ecId, String pspId, long pageNumber, long pageSize) {
    log.infof(AppMessageUtil.logExecute(action));

    Page page = Page.of((int) pageNumber - 1, (int) pageSize);
    Sort sort = AppDBUtil.getSort(List.of("_id,asc"));

    PanacheQuery<FdrPublishEntity> fdrPublishPanacheQuery;
    if (pspId == null || pspId.isBlank()) {
      log.debugf("Get all FdrPublishEntity by ecId[%s]", ecId);
      fdrPublishPanacheQuery = FdrPublishEntity.findByOrganizationId(ecId, sort);
    } else {
      log.debugf("Get all FdrPublishEntity by ecId[%s], pspId[%s]", ecId, pspId);
      fdrPublishPanacheQuery = FdrPublishEntity.findByOrganizationIdAndPspId(ecId, pspId, sort);
    }

    log.debug("Get paging FdrPublishReportingFlowNameProjection");
    PanacheQuery<FdrPublishProjection> fdrProjectionPanacheQuery =
        fdrPublishPanacheQuery.page(page).project(FdrPublishProjection.class);

    List<FdrPublishProjection> reportingFlowIds = fdrProjectionPanacheQuery.list();

    long totPage = fdrProjectionPanacheQuery.pageCount();
    long countReportingFlow = fdrProjectionPanacheQuery.count();

    log.debug("Building ReportingFlowByIdEcDto");
    return FdrAllDto.builder()
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
                        FdrSimpleDto.builder()
                            .fdr(rf.getFdr())
                            .pspId(rf.getSender().getPspId())
                            .build())
                .toList())
        .build();
  }

  @WithSpan(kind = SERVER)
  public FdrGetDto findByReportingFlowName(String action, String fdr, String pspId) {
    log.infof(AppMessageUtil.logExecute(action));

    log.debugf("Existence check FdrPublishEntity by fdr[%s], psp[%s]", fdr, pspId);
    FdrPublishEntity fdrPublishPanacheQuery =
        FdrPublishEntity.findByFdrAndPspId(fdr, pspId)
            .project(FdrPublishEntity.class)
            .firstResultOptional()
            .orElseThrow(
                () -> new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, fdr));

    MDC.put(ORGANIZATION_ID, fdrPublishPanacheQuery.getReceiver().getOrganizationId());

    log.debug("Mapping ReportingFlowGetDto from FdrPublishEntity");
    return mapper.toFdrGetDto(fdrPublishPanacheQuery);
  }

  @WithSpan(kind = SERVER)
  public FdrGetPaymentDto findPaymentByReportingFlowName(
      String action, String fdr, String pspId, long pageNumber, long pageSize) {
    log.infof(AppMessageUtil.logExecute(action));

    Page page = Page.of((int) pageNumber - 1, (int) pageSize);
    Sort sort = AppDBUtil.getSort(List.of("index,asc"));

    log.debugf("Existence check fdr by fdr[%s], psp[%s]", fdr, pspId);
    PanacheQuery<FdrPaymentPublishEntity> fdrPaymentPublishPanacheQuery =
        FdrPaymentPublishEntity.findByFdrAndPspId(fdr, pspId, sort).page(page);

    List<FdrPaymentPublishEntity> list = fdrPaymentPublishPanacheQuery.list();

    long totPage = fdrPaymentPublishPanacheQuery.pageCount();
    long countReportingFlowPayment = fdrPaymentPublishPanacheQuery.count();

    log.debug("Mapping ReportingFlowGetPaymentDto from FdrPaymentPublishEntity");
    return FdrGetPaymentDto.builder()
        .metadata(
            MetadataDto.builder()
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .totPage(totPage)
                .build())
        .count(countReportingFlowPayment)
        .data(mapper.toPaymentDtoList(list))
        .build();
  }
}
