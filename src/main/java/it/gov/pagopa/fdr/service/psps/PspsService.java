package it.gov.pagopa.fdr.service.psps;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.panache.common.Parameters;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.repository.fdr.FdrHistoryEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrInsertEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentHistoryEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentInsertEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentPublishEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPublishEntity;
import it.gov.pagopa.fdr.repository.fdr.model.AbstractReportingFlowPaymentEntity;
import it.gov.pagopa.fdr.repository.fdr.model.ReportingFlowStatusEnumEntity;
import it.gov.pagopa.fdr.repository.fdr.projection.FdrPublishRevisionProjection;
import it.gov.pagopa.fdr.service.dto.AddPaymentDto;
import it.gov.pagopa.fdr.service.dto.DeletePaymentDto;
import it.gov.pagopa.fdr.service.dto.PaymentDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowDto;
import it.gov.pagopa.fdr.service.psps.mapper.PspsServiceServiceMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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
    String pspId = reportingFlowDto.getSender().getPspId();

    // TODO rivedere index  con fdr+psp
    Optional<FdrInsertEntity> byReportingFlowName =
        FdrInsertEntity.find(
                "reporting_flow_name = :flowName and sender.psp_id = :pspId",
                Parameters.with("flowName", reportingFlowName).and("pspId", pspId).map())
            .firstResultOptional();

    if (byReportingFlowName.isPresent()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_ALREADY_EXIST,
          reportingFlowName,
          byReportingFlowName.get().getStatus());
    }

    Optional<FdrPublishRevisionProjection> fdrPublishedByReportingFlowName =
        FdrPublishEntity.find(
                "reporting_flow_name = :flowName and sender.psp_id = :pspId",
                Parameters.with("flowName", reportingFlowName).and("pspId", pspId).map())
            .project(FdrPublishRevisionProjection.class)
            .firstResultOptional();

    // sono stati tolti i check con il vecchio FDR, vale solo che se arriva stesso flowNAme con
    // stesso pspId si crea la rev2

    FdrInsertEntity reportingFlowEntity = mapper.toReportingFlow(reportingFlowDto);

    reportingFlowEntity.setCreated(now);
    reportingFlowEntity.setUpdated(now);
    reportingFlowEntity.setStatus(ReportingFlowStatusEnumEntity.CREATED);
    reportingFlowEntity.setTot_payments(0L);
    reportingFlowEntity.setSum_paymnents(0.0);
    reportingFlowEntity.setRevision(
        fdrPublishedByReportingFlowName.map(r -> r.revision + 1).orElse(1L));
    reportingFlowEntity.persist();
  }

  @WithSpan(kind = SERVER)
  public void addPayment(String pspId, String reportingFlowName, AddPaymentDto addPaymentDto) {
    log.debugf("Save add payment on DB");
    Instant now = Instant.now();

    FdrInsertEntity reportingFlowEntity =
        FdrInsertEntity.find(
                "reporting_flow_name = :flowName and sender.psp_id = :pspId",
                Parameters.with("flowName", reportingFlowName).and("pspId", pspId).map())
            .project(FdrInsertEntity.class)
            .firstResultOptional()
            .orElseThrow(
                () ->
                    new AppException(
                        AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, reportingFlowName));

    if (!(reportingFlowEntity.getStatus() == ReportingFlowStatusEnumEntity.CREATED
        || reportingFlowEntity.getStatus() == ReportingFlowStatusEnumEntity.INSERTED)) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION,
          reportingFlowName,
          reportingFlowEntity.getStatus());
    }

    // TODO revedere con iuv+iur
    List<Long> indexList = addPaymentDto.getPayments().stream().map(PaymentDto::getIndex).toList();
    if (indexList.size() != indexList.stream().distinct().toList().size()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_SAME_INDEX_IN_SAME_REQUEST,
          reportingFlowName);
    }

    List<FdrPaymentInsertEntity> paymentIndexAlreadyExist =
        FdrPaymentInsertEntity.find(
                "ref_fdr_reporting_flow_name = :flowName and index in :indexes",
                Parameters.with("flowName", reportingFlowName).and("indexes", indexList).map())
            .project(FdrPaymentInsertEntity.class)
            .list();

    if (paymentIndexAlreadyExist != null && paymentIndexAlreadyExist.size() > 0) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_DUPLICATE_INDEX, reportingFlowName);
    }

    List<FdrPaymentInsertEntity> reportingFlowPaymentEntities =
        mapper.toReportingFlowPaymentEntityList(addPaymentDto.getPayments());

    reportingFlowEntity.setTot_payments(
        addAndSumCount(reportingFlowEntity, reportingFlowPaymentEntities));
    reportingFlowEntity.setSum_paymnents(
        addAndSum(reportingFlowEntity, reportingFlowPaymentEntities));

    reportingFlowEntity.setUpdated(now);
    reportingFlowEntity.setStatus(ReportingFlowStatusEnumEntity.INSERTED);
    reportingFlowEntity.update();

    FdrPaymentInsertEntity.persist(
        reportingFlowPaymentEntities.stream()
            .map(
                reportingFlowPaymentEntity -> {
                  reportingFlowPaymentEntity.setCreated(now);
                  reportingFlowPaymentEntity.setUpdated(now);
                  reportingFlowPaymentEntity.setRef_fdr_id(reportingFlowEntity.id);
                  reportingFlowPaymentEntity.setRef_fdr_reporting_flow_name(
                      reportingFlowEntity.getReporting_flow_name());
                  reportingFlowPaymentEntity.setRef_fdr_reporting_sender_psp_id(
                      reportingFlowEntity.getSender().getPspId());
                  return reportingFlowPaymentEntity;
                })
            .collect(Collectors.toList()));
  }

  @WithSpan(kind = SERVER)
  public void deletePayment(
      String pspId, String reportingFlowName, DeletePaymentDto deletePaymentDto) {
    log.debugf("Delete payment on DB");
    Instant now = Instant.now();

    FdrInsertEntity reportingFlowEntity =
        FdrInsertEntity.find(
                "reporting_flow_name = :flowName and sender.psp_id = :pspId",
                Parameters.with("flowName", reportingFlowName).and("pspId", pspId).map())
            .project(FdrInsertEntity.class)
            .firstResultOptional()
            .orElseThrow(
                () ->
                    new AppException(
                        AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, reportingFlowName));

    if (!(reportingFlowEntity.getStatus() == ReportingFlowStatusEnumEntity.INSERTED)) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION,
          reportingFlowName,
          reportingFlowEntity.getStatus());
    }

    // TODO rivedere con iuv+iur
    List<Long> indexList = deletePaymentDto.getIndexPayments();
    if (indexList.size() != indexList.stream().distinct().toList().size()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_SAME_INDEX_IN_SAME_REQUEST,
          reportingFlowName);
    }

    List<FdrPaymentInsertEntity> paymentToDelete =
        FdrPaymentInsertEntity.find(
                "ref_fdr_reporting_flow_name = :flowName and index in :indexes",
                Parameters.with("flowName", reportingFlowName).and("indexes", indexList).map())
            .project(FdrPaymentInsertEntity.class)
            .list();
    if (!paymentToDelete.stream()
        .map(AbstractReportingFlowPaymentEntity::getIndex)
        .collect(Collectors.toSet())
        .containsAll(indexList)) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_NO_MATCH_INDEX, reportingFlowName);
    }

    FdrPaymentInsertEntity.delete(
        "ref_fdr_reporting_flow_name = :flowName and index in :indexes",
        Parameters.with("flowName", reportingFlowName).and("indexes", indexList).map());

    reportingFlowEntity.setTot_payments(deleteAndSumCount(reportingFlowEntity, paymentToDelete));
    reportingFlowEntity.setSum_paymnents(deleteAndSubtract(reportingFlowEntity, paymentToDelete));
    reportingFlowEntity.setUpdated(now);
    reportingFlowEntity.setStatus(
        (reportingFlowEntity.getSum_paymnents() > 0)
            ? ReportingFlowStatusEnumEntity.INSERTED
            : ReportingFlowStatusEnumEntity.CREATED);
    reportingFlowEntity.update();
  }

  @WithSpan(kind = SERVER)
  public void publishByReportingFlowName(String pspId, String reportingFlowName) {
    log.debugf("Confirm reporting flow");
    Instant now = Instant.now();

    FdrInsertEntity reportingFlowEntity =
        FdrInsertEntity.find(
                "reporting_flow_name = :flowName and sender.psp_id = :pspId",
                Parameters.with("flowName", reportingFlowName).and("pspId", pspId).map())
            .project(FdrInsertEntity.class)
            .firstResultOptional()
            .orElseThrow(
                () ->
                    new AppException(
                        AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, reportingFlowName));

    if (!(reportingFlowEntity.getStatus() == ReportingFlowStatusEnumEntity.INSERTED)) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION,
          reportingFlowName,
          reportingFlowEntity.getStatus());
    }

    reportingFlowEntity.setUpdated(now);
    reportingFlowEntity.setStatus(ReportingFlowStatusEnumEntity.PUBLISHED);

    List<FdrPaymentInsertEntity> paymentInsertEntities =
        FdrPaymentInsertEntity.find(
                "ref_fdr_reporting_flow_name = :flowName and ref_fdr_reporting_sender_psp_id ="
                    + " :pspId",
                Parameters.with("flowName", reportingFlowName).and("pspId", pspId).map())
            .project(FdrPaymentInsertEntity.class)
            .list();

    if (reportingFlowEntity.getRevision() > 1L) {
      FdrPublishEntity.delete(
          "reporting_flow_name = :flowName and sender.psp_id = :pspId",
          Parameters.with("flowName", reportingFlowName).and("pspId", pspId).map());
      FdrPaymentPublishEntity.delete(
          "ref_fdr_reporting_flow_name = :flowName and ref_fdr_reporting_sender_psp_id = :pspId",
          Parameters.with("flowName", reportingFlowName).and("pspId", pspId).map());
    }

    FdrPublishEntity fdrPublishEntity = mapper.toFdrPublishEntity(reportingFlowEntity);
    fdrPublishEntity.setInternal_ndp_read(Boolean.FALSE);
    fdrPublishEntity.setRead(Boolean.FALSE);
    fdrPublishEntity.persist();
    List<FdrPaymentPublishEntity> fdrPaymentPublishEntities =
        mapper.toFdrPaymentPublishEntityList(paymentInsertEntities);
    FdrPaymentPublishEntity.persist(fdrPaymentPublishEntities);

    FdrHistoryEntity fdrHistoryEntity = mapper.toFdrHistoryEntity(reportingFlowEntity);
    fdrHistoryEntity.persist();
    List<FdrPaymentHistoryEntity> fdrPaymentHistoryEntities =
        mapper.toFdrPaymentHistoryEntityList(paymentInsertEntities);
    FdrPaymentHistoryEntity.persist(fdrPaymentHistoryEntities);

    reportingFlowEntity.delete();
    FdrPaymentInsertEntity.delete(
        "ref_fdr_reporting_flow_name = :flowName and ref_fdr_reporting_sender_psp_id = :pspId",
        Parameters.with("flowName", reportingFlowName).and("pspId", pspId).map());
  }

  @WithSpan(kind = SERVER)
  public void deleteByReportingFlowName(String pspId, String reportingFlowName) {
    log.debugf("Delete reporting flow");
    Instant now = Instant.now();

    FdrInsertEntity reportingFlowEntity =
        FdrInsertEntity.find(
                "reporting_flow_name = :flowName and sender.psp_id = :pspId",
                Parameters.with("flowName", reportingFlowName).and("pspId", pspId).map())
            .project(FdrInsertEntity.class)
            .firstResultOptional()
            .orElseThrow(
                () ->
                    new AppException(
                        AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, reportingFlowName));

    if (!(reportingFlowEntity.getStatus() == ReportingFlowStatusEnumEntity.CREATED
        || reportingFlowEntity.getStatus() == ReportingFlowStatusEnumEntity.INSERTED)) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION,
          reportingFlowName,
          reportingFlowEntity.getStatus());
    }

    if (reportingFlowEntity.getTot_payments() > 0L) {
      FdrPaymentInsertEntity.delete(
          "ref_fdr_reporting_flow_name = :flowName",
          Parameters.with("flowName", reportingFlowName).map());
    }
    reportingFlowEntity.delete();
  }

  private static double addAndSum(
      FdrInsertEntity reportingFlowEntity,
      List<FdrPaymentInsertEntity> reportingFlowPaymentEntities) {
    return Double.sum(
        Objects.requireNonNullElseGet(reportingFlowEntity.getSum_paymnents(), () -> (double) 0),
        reportingFlowPaymentEntities.stream()
            .map(AbstractReportingFlowPaymentEntity::getPay)
            .mapToDouble(Double::doubleValue)
            .sum());
  }

  private static double deleteAndSubtract(
      FdrInsertEntity reportingFlowEntity, List<FdrPaymentInsertEntity> paymentToDelete) {
    return BigDecimal.valueOf(
            Objects.requireNonNullElseGet(reportingFlowEntity.getSum_paymnents(), () -> (double) 0))
        .subtract(
            BigDecimal.valueOf(
                paymentToDelete.stream()
                    .map(AbstractReportingFlowPaymentEntity::getPay)
                    .mapToDouble(Double::doubleValue)
                    .sum()))
        .doubleValue();
  }

  private static long addAndSumCount(
      FdrInsertEntity reportingFlowEntity,
      List<FdrPaymentInsertEntity> reportingFlowPaymentEntities) {
    return Objects.requireNonNullElseGet(reportingFlowEntity.getTot_payments(), () -> (long) 0)
        + reportingFlowPaymentEntities.size();
  }

  private static long deleteAndSumCount(
      FdrInsertEntity reportingFlowEntity,
      List<FdrPaymentInsertEntity> reportingFlowPaymentEntities) {
    return Objects.requireNonNullElseGet(reportingFlowEntity.getTot_payments(), () -> (long) 0)
        - reportingFlowPaymentEntities.size();
  }
}
