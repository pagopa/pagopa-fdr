package it.gov.pagopa.fdr.service.psps;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowPaymentEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowPaymentRevisionEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowRevisionEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.model.ReportingFlowPaymentStatusEnumEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.model.ReportingFlowStatusEnumEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.projection.ReportingFlowNameProjection;
import it.gov.pagopa.fdr.service.dto.AddPaymentDto;
import it.gov.pagopa.fdr.service.dto.DeletePaymentDto;
import it.gov.pagopa.fdr.service.dto.MetadataDto;
import it.gov.pagopa.fdr.service.dto.PaymentDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowByIdEcDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowGetDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowGetPaymentDto;
import it.gov.pagopa.fdr.service.psps.mapper.PspsServiceServiceMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class PspsService {

  @Inject PspsServiceServiceMapper mapper;

  @Inject Logger log;

  @WithSpan(kind = SERVER)
  public void save(ReportingFlowDto reportingFlowDto) {
    log.debugf("Save data on DB");
    Instant now = Instant.now();
    String reportingFlowName = reportingFlowDto.getReportingFlowName();

    Optional<ReportingFlowEntity> byReportingFlowName =
        getByReportingFlowName(reportingFlowName, ReportingFlowEntity.class);
    if (byReportingFlowName.isPresent()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_ALREADY_EXIST,
          reportingFlowName,
          byReportingFlowName.get().status);
    }

    ReportingFlowEntity reportingFlowEntity = mapper.toReportingFlow(reportingFlowDto);
    setReportingFlowEntity(reportingFlowEntity, ReportingFlowStatusEnumEntity.CREATED, now);
    reportingFlowEntity.persist();

    ReportingFlowRevisionEntity reportingFlowRevision =
        mapper.toReportingFlowRevision(reportingFlowEntity);
    reportingFlowRevision.persist();
  }

  @WithSpan(kind = SERVER)
  public void addPayment(String pspId, String reportingFlowName, AddPaymentDto addPaymentDto) {
    log.debugf("Save add payment on DB");
    Instant now = Instant.now();

    ReportingFlowEntity reportingFlowEntity = retrieve(reportingFlowName);
    if (!(reportingFlowEntity.status == ReportingFlowStatusEnumEntity.CREATED
        || reportingFlowEntity.status == ReportingFlowStatusEnumEntity.INSERTED)) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION,
          reportingFlowName,
          reportingFlowEntity.status);
    }

    List<Long> indexList = addPaymentDto.getPayments().stream().map(PaymentDto::getIndex).toList();
    if (indexList.size() != indexList.stream().distinct().toList().size()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_SAME_INDEX_IN_SAME_REQUEST,
          reportingFlowName);
    }

    List<ReportingFlowPaymentEntity> paymentIndexAlreadyExist =
        ReportingFlowPaymentEntity.find(
                "ref_reporting_flow_reporting_flow_name = :flowName and index in :indexes",
                Parameters.with("flowName", reportingFlowName).and("indexes", indexList).map())
            .project(ReportingFlowPaymentEntity.class)
            .list();
    if (paymentIndexAlreadyExist != null && paymentIndexAlreadyExist.size() > 0) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_DUPLICATE_INDEX, reportingFlowName);
    }

    List<ReportingFlowPaymentEntity> reportingFlowPaymentEntities =
        mapper.toReportingFlowPaymentEntityList(addPaymentDto.getPayments());

    setReportingFlowEntity(reportingFlowEntity, ReportingFlowStatusEnumEntity.INSERTED, now);
    reportingFlowEntity.totPayments =
        reportingFlowEntity.totPayments + reportingFlowPaymentEntities.size();
    reportingFlowEntity.sumPaymnents =
        Double.sum(
            reportingFlowEntity.sumPaymnents,
            reportingFlowPaymentEntities.stream()
                .map(a -> a.pay)
                .mapToDouble(Double::doubleValue)
                .sum());
    reportingFlowEntity.update();

    setReportingFlowPaymentEntityList(
        reportingFlowPaymentEntities,
        ReportingFlowPaymentStatusEnumEntity.SUM,
        now,
        reportingFlowEntity);
    ReportingFlowPaymentEntity.persist(reportingFlowPaymentEntities);

    List<ReportingFlowPaymentRevisionEntity> reportingFlowPaymentRevisionEntity =
        mapper.toReportingFlowPaymentRevisionEntityList(reportingFlowPaymentEntities);
    ReportingFlowPaymentRevisionEntity.persist(reportingFlowPaymentRevisionEntity);

    ReportingFlowRevisionEntity reportingFlowRevision =
        mapper.toReportingFlowRevision(reportingFlowEntity);
    reportingFlowRevision.persist();
  }

  @WithSpan(kind = SERVER)
  public void deletePayment(
      String pspId, String reportingFlowName, DeletePaymentDto deletePaymentDto) {
    log.debugf("Delete payment on DB");
    Instant now = Instant.now();

    ReportingFlowEntity reportingFlowEntity = retrieve(reportingFlowName);
    if (!(reportingFlowEntity.status == ReportingFlowStatusEnumEntity.INSERTED)) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION,
          reportingFlowName,
          reportingFlowEntity.status);
    }

    List<Long> indexList = deletePaymentDto.getIndexPayments();
    if (indexList.size() != indexList.stream().distinct().toList().size()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_SAME_INDEX_IN_SAME_REQUEST,
          reportingFlowName);
    }

    List<ReportingFlowPaymentEntity> paymentToDelete =
        ReportingFlowPaymentEntity.find(
                "ref_reporting_flow_reporting_flow_name = :flowName and index in :indexes",
                Parameters.with("flowName", reportingFlowName).and("indexes", indexList).map())
            .project(ReportingFlowPaymentEntity.class)
            .list();
    if (!paymentToDelete.stream()
        .map(a -> a.index)
        .collect(Collectors.toSet())
        .containsAll(indexList)) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_NO_MATCH_INDEX, reportingFlowName);
    }

    ReportingFlowPaymentEntity.delete(
        "ref_reporting_flow_reporting_flow_name = :flowName and index in :indexes",
        Parameters.with("flowName", reportingFlowName).and("indexes", indexList).map());

    reportingFlowEntity.totPayments = reportingFlowEntity.totPayments - paymentToDelete.size();
    reportingFlowEntity.sumPaymnents =
        BigDecimal.valueOf(reportingFlowEntity.sumPaymnents)
            .subtract(
                BigDecimal.valueOf(
                    paymentToDelete.stream()
                        .map(a -> a.pay)
                        .mapToDouble(Double::doubleValue)
                        .sum()))
            .doubleValue();
    setReportingFlowEntity(
        reportingFlowEntity,
        (reportingFlowEntity.sumPaymnents > 0)
            ? ReportingFlowStatusEnumEntity.INSERTED
            : ReportingFlowStatusEnumEntity.CREATED,
        now);
    reportingFlowEntity.update();

    setReportingFlowPaymentEntityList(
        paymentToDelete, ReportingFlowPaymentStatusEnumEntity.SUBTRACT, now, reportingFlowEntity);
    List<ReportingFlowPaymentRevisionEntity> reportingFlowPaymentRevisionEntity =
        mapper.toReportingFlowPaymentRevisionEntityList(paymentToDelete);
    ReportingFlowPaymentRevisionEntity.persist(reportingFlowPaymentRevisionEntity);

    ReportingFlowRevisionEntity reportingFlowRevision =
        mapper.toReportingFlowRevision(reportingFlowEntity);
    reportingFlowRevision.persist();
  }

  @WithSpan(kind = SERVER)
  public void publishByReportingFlowName(String pspId, String reportingFlowName) {
    log.debugf("Confirm reporting flow");
    Instant now = Instant.now();

    ReportingFlowEntity reportingFlowEntity = retrieve(reportingFlowName);
    if (!(reportingFlowEntity.status == ReportingFlowStatusEnumEntity.INSERTED)) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION,
          reportingFlowName,
          reportingFlowEntity.status);
    }

    setReportingFlowEntity(reportingFlowEntity, ReportingFlowStatusEnumEntity.PUBLISHED, now);
    reportingFlowEntity.update();

    ReportingFlowRevisionEntity reportingFlowRevision =
        mapper.toReportingFlowRevision(reportingFlowEntity);
    reportingFlowRevision.persist();
  }

  @WithSpan(kind = SERVER)
  public void deleteByReportingFlowName(String pspId, String reportingFlowName) {
    log.debugf("Delete reporting flow");
    Instant now = Instant.now();

    ReportingFlowEntity reportingFlowEntity = retrieve(reportingFlowName);
    reportingFlowEntity.delete();

    //    setReportingFlowPaymentEntityList(
    //        paymentToDelete, ReportingFlowPaymentStatusEnumEntity.DELETE, now,
    // reportingFlowEntity);
    //    List<ReportingFlowPaymentRevisionEntity> reportingFlowPaymentRevisionEntity =
    //        mapper.toReportingFlowPaymentRevisionEntityList(paymentToDelete);
    //    ReportingFlowPaymentRevisionEntity.persist(reportingFlowPaymentRevisionEntity);
    //
    //    setReportingFlowEntity(reportingFlowEntity, ReportingFlowStatusEnumEntity.DELETED, now);
    //    ReportingFlowRevisionEntity reportingFlowRevision =
    //        mapper.toReportingFlowRevision(reportingFlowEntity);
    //    reportingFlowRevision.persist();
  }

  @WithSpan(kind = SERVER)
  public ReportingFlowByIdEcDto findByIdEc(
      String ecId, String pspId, long pageNumber, long pageSize) {
    log.debugf("Get all data from DB");

    Page page = Page.of((int) pageNumber - 1, (int) pageSize);
    Sort sort = getSort(List.of("_id,asc"));

    PanacheQuery<ReportingFlowEntity> reportingFlowPanacheQuery;
    if (pspId == null || pspId.isBlank()) {
      reportingFlowPanacheQuery =
          ReportingFlowEntity.find(
              "status = :status and receiver.ec_id = :ecId",
              sort,
              Parameters.with("status", ReportingFlowStatusEnumEntity.PUBLISHED)
                  .and("ecId", ecId)
                  .map());
    } else {
      reportingFlowPanacheQuery =
          ReportingFlowEntity.find(
              "status = :status and receiver.ec_id = :ecId and sender.psp_id = :pspId",
              sort,
              Parameters.with("status", ReportingFlowStatusEnumEntity.PUBLISHED)
                  .and("ecId", ecId)
                  .and("pspId", pspId)
                  .map());
    }
    PanacheQuery<ReportingFlowNameProjection> reportingFlowNameProjectionPanacheQuery =
        reportingFlowPanacheQuery.page(page).project(ReportingFlowNameProjection.class);

    List<ReportingFlowNameProjection> reportingFlowIds =
        reportingFlowNameProjectionPanacheQuery.list();

    long totPage = Long.valueOf(reportingFlowNameProjectionPanacheQuery.pageCount());
    long countReportingFlow = reportingFlowNameProjectionPanacheQuery.count();

    return ReportingFlowByIdEcDto.builder()
        .metadata(
            MetadataDto.builder()
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .totPage(totPage)
                .build())
        .count(countReportingFlow)
        .data(reportingFlowIds.stream().map(rf -> rf.reporting_flow_name).toList())
        .build();
  }

  @WithSpan(kind = SERVER)
  public ReportingFlowGetDto findByReportingFlowName(String reportingFlowName) {
    log.debugf("Get data from DB");

    return mapper.toReportingFlowGetDto(retrieve(reportingFlowName));
  }

  @WithSpan(kind = SERVER)
  public ReportingFlowGetPaymentDto findPaymentByReportingFlowName(
      String reportingFlowName, long pageNumber, long pageSize) {
    log.debugf("Get data from DB");

    Page page = Page.of((int) pageNumber - 1, (int) pageSize);
    Sort sort = getSort(List.of("index,asc"));

    PanacheQuery<ReportingFlowPaymentEntity> reportingFlowPaymentEntityPanacheQuery =
        ReportingFlowPaymentEntity.find(
                "ref_reporting_flow_reporting_flow_name = ?1 and status = ?2",
                sort,
                reportingFlowName,
                ReportingFlowPaymentStatusEnumEntity.SUM)
            .page(page);

    List<ReportingFlowPaymentEntity> list = reportingFlowPaymentEntityPanacheQuery.list();

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

  private Sort getSort(List<String> sortColumn) {
    Sort sort = Sort.empty();
    if (sortColumn != null && sortColumn.size() > 0) {
      sortColumn.stream()
          .filter(s -> s.replace(",", "").isBlank())
          .forEach(
              a -> {
                String[] split = a.split(",");
                String column = split[0].trim();
                String direction = split[1].trim();
                if (!column.isBlank()) {
                  if (direction.equalsIgnoreCase("asc")) {
                    sort.and(column, Direction.Ascending);
                  } else if (direction.equalsIgnoreCase("desc")) {
                    sort.and(column, Direction.Descending);
                  } else {
                    sort.and(column);
                  }
                }
              });
    }
    return sort;
  }

  private ReportingFlowEntity retrieve(String reportingFlowName) {
    return getByReportingFlowName(reportingFlowName, ReportingFlowEntity.class)
        .orElseThrow(
            () ->
                new AppException(
                    AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, reportingFlowName));
  }

  private <T> Optional<T> getByReportingFlowName(String reportingFlowName, Class<T> clazz) {
    return ReportingFlowEntity.find(
            "reporting_flow_name = :flowName", Parameters.with("flowName", reportingFlowName).map())
        .project(clazz)
        .firstResultOptional();
  }

  private void setReportingFlowEntity(
      ReportingFlowEntity reportingFlowEntity, ReportingFlowStatusEnumEntity status, Instant now) {
    reportingFlowEntity.created =
        (reportingFlowEntity.created == null) ? now : reportingFlowEntity.created;
    reportingFlowEntity.updated = now;
    reportingFlowEntity.status = status;
    reportingFlowEntity.revision =
        (reportingFlowEntity.revision == null) ? 1L : reportingFlowEntity.revision + 1;
    reportingFlowEntity.totPayments =
        (reportingFlowEntity.totPayments == null) ? 0L : reportingFlowEntity.totPayments;
    reportingFlowEntity.sumPaymnents =
        (reportingFlowEntity.sumPaymnents == null)
            ? Double.valueOf(0)
            : reportingFlowEntity.sumPaymnents;
  }

  private void setReportingFlowPaymentEntityList(
      List<ReportingFlowPaymentEntity> reportingFlowPaymentEntities,
      ReportingFlowPaymentStatusEnumEntity status,
      Instant now,
      ReportingFlowEntity reportingFlowEntity) {
    reportingFlowPaymentEntities.stream()
        .forEach(
            reportingFlowPaymentEntity -> {
              reportingFlowPaymentEntity.created =
                  (reportingFlowPaymentEntity.created == null)
                      ? now
                      : reportingFlowPaymentEntity.created;
              reportingFlowPaymentEntity.updated = now;
              reportingFlowPaymentEntity.status = status;
              reportingFlowPaymentEntity.revision =
                  (reportingFlowPaymentEntity.revision == null)
                      ? 1L
                      : reportingFlowPaymentEntity.revision + 1;
              reportingFlowPaymentEntity.ref_reporting_flow_id = reportingFlowEntity.id;
              reportingFlowPaymentEntity.ref_reporting_flow_reporting_flow_name =
                  reportingFlowEntity.reporting_flow_name;
              reportingFlowPaymentEntity.ref_reporting_flow_revision = reportingFlowEntity.revision;
            });
  }
}
