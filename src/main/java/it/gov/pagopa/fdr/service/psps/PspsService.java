package it.gov.pagopa.fdr.service.psps;

import static io.opentelemetry.api.trace.SpanKind.SERVER;
import static it.gov.pagopa.fdr.util.MDCKeys.EC_ID;
import static it.gov.pagopa.fdr.util.MDCKeys.TRX_ID;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.repository.fdr.FdrHistoryEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrInsertEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentHistoryEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentInsertEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentPublishEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPublishEntity;
import it.gov.pagopa.fdr.repository.fdr.model.ReportingFlowStatusEnumEntity;
import it.gov.pagopa.fdr.repository.fdr.projection.FdrPublishRevisionProjection;
import it.gov.pagopa.fdr.service.conversion.ConversionService;
import it.gov.pagopa.fdr.service.conversion.message.FlowMessage;
import it.gov.pagopa.fdr.service.dto.AddPaymentDto;
import it.gov.pagopa.fdr.service.dto.DeletePaymentDto;
import it.gov.pagopa.fdr.service.dto.PaymentDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowDto;
import it.gov.pagopa.fdr.service.psps.mapper.PspsServiceServiceMapper;
import it.gov.pagopa.fdr.service.re.ReService;
import it.gov.pagopa.fdr.service.re.model.AppVersionEnum;
import it.gov.pagopa.fdr.service.re.model.EventTypeEnum;
import it.gov.pagopa.fdr.service.re.model.FlowActionEnum;
import it.gov.pagopa.fdr.service.re.model.FlowStatusEnum;
import it.gov.pagopa.fdr.service.re.model.ReInternal;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

@ApplicationScoped
public class PspsService {

  @Inject PspsServiceServiceMapper mapper;

  @Inject Logger log;

  @Inject ConversionService conversionQueue;

  @Inject ReService reService;

  @WithSpan(kind = SERVER)
  public void save(String action, ReportingFlowDto reportingFlowDto) {
    log.infof(AppMessageUtil.logExecute(action));

    Instant now = Instant.now();
    String reportingFlowName = reportingFlowDto.getReportingFlowName();
    String pspId = reportingFlowDto.getSender().getPspId();
    String ecId = reportingFlowDto.getReceiver().getEcId();

    log.debugf(
        "Existence check FdrInsertEntity by flowName[%s], psp[%s]", reportingFlowName, pspId);
    // TODO rivedere index  con fdr+psp
    Optional<FdrInsertEntity> byReportingFlowName =
        FdrInsertEntity.findByFlowNameAndPspId(reportingFlowName, pspId).firstResultOptional();

    if (byReportingFlowName.isPresent()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_ALREADY_EXIST,
          reportingFlowName,
          byReportingFlowName.get().getStatus());
    }

    log.debugf("Get FdrPublishRevision by flowName[%s], psp[%s]", reportingFlowName, pspId);
    Optional<FdrPublishRevisionProjection> fdrPublishedByReportingFlowName =
        FdrPublishEntity.findByFlowNameAndPspId(reportingFlowName, pspId)
            .project(FdrPublishRevisionProjection.class)
            .firstResultOptional();

    // sono stati tolti i check con il vecchio FDR, vale solo che se arriva stesso flowName con
    // stesso pspId si crea la rev2

    Long revision = fdrPublishedByReportingFlowName.map(r -> r.getRevision() + 1).orElse(1L);
    log.debug("Mapping FdrInsertEntity from reportingFlowDto");
    FdrInsertEntity reportingFlowEntity = mapper.toReportingFlow(reportingFlowDto);

    reportingFlowEntity.setCreated(now);
    reportingFlowEntity.setUpdated(now);
    reportingFlowEntity.setStatus(ReportingFlowStatusEnumEntity.CREATED);
    reportingFlowEntity.setTotPayments(0L);
    reportingFlowEntity.setSumPayments(0.0);
    reportingFlowEntity.setRevision(revision);
    reportingFlowEntity.persist();

    String sessionId = org.slf4j.MDC.get(TRX_ID);
    reService.sendEvent(
        ReInternal.builder()
            .appVersion(AppVersionEnum.FDR003)
            .created(Instant.now())
            .sessionId(sessionId)
            .eventType(EventTypeEnum.INTERNAL)
            .flowPhisicalDelete(false)
            .flowStatus(FlowStatusEnum.CREATED)
            //            .flowRead(false)
            .flowName(reportingFlowName)
            .pspId(pspId)
            .organizationId(ecId)
            .revision(revision)
            .flowAction(FlowActionEnum.CREATE_FLOW)
            .build());
    log.debug("FdrInsertEntity CREATED");
  }

  @WithSpan(kind = SERVER)
  public void addPayment(
      String action, String pspId, String reportingFlowName, AddPaymentDto addPaymentDto) {
    log.infof(AppMessageUtil.logExecute(action));
    Instant now = Instant.now();

    FdrInsertEntity reportingFlowEntity =
        checkFdrInsertEntity(
            reportingFlowName,
            pspId,
            new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, reportingFlowName));

    MDC.put(EC_ID, reportingFlowEntity.getReceiver().getEcId());

    // TODO revedere con iuv+iur
    log.debug("Check payments indexes");
    List<Long> indexList = addPaymentDto.getPayments().stream().map(PaymentDto::getIndex).toList();
    if (indexList.size() != indexList.stream().distinct().toList().size()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_SAME_INDEX_IN_SAME_REQUEST,
          reportingFlowName);
    }

    log.debugf(
        "Existence check FdrPaymentInsertEntity by flowName[%s], indexList", reportingFlowName);
    List<FdrPaymentInsertEntity> paymentIndexAlreadyExist =
        FdrPaymentInsertEntity.findByFlowNameAndIndexes(reportingFlowName, indexList)
            .project(FdrPaymentInsertEntity.class)
            .list();

    if (paymentIndexAlreadyExist != null && !paymentIndexAlreadyExist.isEmpty()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_DUPLICATE_INDEX, reportingFlowName);
    }

    log.debug("Mapping FdrPaymentInsertEntity from addPaymentDto.getPayments()");
    List<FdrPaymentInsertEntity> reportingFlowPaymentEntities =
        mapper.toReportingFlowPaymentEntityList(addPaymentDto.getPayments());

    reportingFlowEntity.setTotPayments(
        addAndSumCount(reportingFlowEntity, reportingFlowPaymentEntities));
    reportingFlowEntity.setSumPayments(
        addAndSum(reportingFlowEntity, reportingFlowPaymentEntities));

    reportingFlowEntity.setUpdated(now);
    reportingFlowEntity.setStatus(ReportingFlowStatusEnumEntity.INSERTED);
    reportingFlowEntity.update();
    log.debug("FdrInsertEntity INSERTED");

    log.debug("Mapping FdrPaymentInsertEntity from addPaymentDto.getPayments()");
    FdrPaymentInsertEntity.persistFdrPaymentsInsert(
        reportingFlowPaymentEntities.stream()
            .map(
                reportingFlowPaymentEntity -> {
                  reportingFlowPaymentEntity.setCreated(now);
                  reportingFlowPaymentEntity.setUpdated(now);
                  reportingFlowPaymentEntity.setRefFdrId(reportingFlowEntity.id);
                  reportingFlowPaymentEntity.setRefFdrReportingFlowName(
                      reportingFlowEntity.getReportingFlowName());
                  reportingFlowPaymentEntity.setRefFdrReportingSenderPspId(
                      reportingFlowEntity.getSender().getPspId());
                  reportingFlowPaymentEntity.setRefFdrRevision(reportingFlowEntity.getRevision());
                  return reportingFlowPaymentEntity;
                })
            .toList());

    String sessionId = org.slf4j.MDC.get(TRX_ID);
    reService.sendEvent(
        ReInternal.builder()
            .appVersion(AppVersionEnum.FDR003)
            .created(Instant.now())
            .sessionId(sessionId)
            .eventType(EventTypeEnum.INTERNAL)
            .flowPhisicalDelete(false)
            .flowStatus(FlowStatusEnum.INSERTED)
            //            .flowRead(false)
            .flowName(reportingFlowName)
            .pspId(pspId)
            .organizationId(reportingFlowEntity.getReceiver().getEcId())
            .revision(reportingFlowEntity.getRevision())
            .flowAction(FlowActionEnum.ADD_PAYMENT)
            .build());
  }

  @WithSpan(kind = SERVER)
  public void deletePayment(
      String action, String pspId, String reportingFlowName, DeletePaymentDto deletePaymentDto) {
    log.infof(AppMessageUtil.logExecute(action));
    Instant now = Instant.now();

    FdrInsertEntity reportingFlowEntity =
        checkFdrInsertEntity(
            reportingFlowName,
            pspId,
            new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, reportingFlowName));

    MDC.put(EC_ID, reportingFlowEntity.getReceiver().getEcId());

    if (ReportingFlowStatusEnumEntity.INSERTED != reportingFlowEntity.getStatus()) {
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

    log.debugf(
        "Existence check FdrPaymentInsertEntity to delete by flowName[%s], indexList",
        reportingFlowName);
    List<FdrPaymentInsertEntity> paymentToDelete =
        FdrPaymentInsertEntity.findByFlowNameAndIndexes(reportingFlowName, indexList)
            .project(FdrPaymentInsertEntity.class)
            .list();
    if (!paymentToDelete.stream()
        .map(FdrPaymentInsertEntity::getIndex)
        .collect(Collectors.toSet())
        .containsAll(indexList)) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_NO_MATCH_INDEX, reportingFlowName);
    }

    log.debugf("Delete FdrPaymentInsertEntity by flowName[%s], indexList", reportingFlowName);
    FdrPaymentInsertEntity.deleteByFlowNameAndIndexes(reportingFlowName, indexList);

    ReportingFlowStatusEnumEntity status =
        reportingFlowEntity.getSumPayments() > 0
            ? ReportingFlowStatusEnumEntity.INSERTED
            : ReportingFlowStatusEnumEntity.CREATED;
    reportingFlowEntity.setTotPayments(deleteAndSumCount(reportingFlowEntity, paymentToDelete));
    reportingFlowEntity.setSumPayments(deleteAndSubtract(reportingFlowEntity, paymentToDelete));
    reportingFlowEntity.setUpdated(now);
    reportingFlowEntity.setStatus(status);
    reportingFlowEntity.update();
    log.debugf("FdrInsertEntity %s", reportingFlowEntity.getStatus().name());

    String sessionId = org.slf4j.MDC.get(TRX_ID);
    reService.sendEvent(
        ReInternal.builder()
            .appVersion(AppVersionEnum.FDR003)
            .created(Instant.now())
            .sessionId(sessionId)
            .eventType(EventTypeEnum.INTERNAL)
            .flowPhisicalDelete(false)
            .flowStatus(
                ReportingFlowStatusEnumEntity.INSERTED == status
                    ? FlowStatusEnum.INSERTED
                    : FlowStatusEnum.CREATED)
            //            .flowRead(false)
            .flowName(reportingFlowName)
            .pspId(pspId)
            .organizationId(reportingFlowEntity.getReceiver().getEcId())
            .revision(reportingFlowEntity.getRevision())
            .flowAction(FlowActionEnum.DELETE_PAYMENT)
            .build());
  }

  @WithSpan(kind = SERVER)
  public void internalPublishByReportingFlowName(
      String action, String pspId, String reportingFlowName) {
    Consumer<FdrInsertEntity> consumer =
        reportingFlowEntity -> log.debug("NOT Add FdrInsertEntity in queue flow message");
    basePublishByReportingFlowName(action, pspId, reportingFlowName, consumer);
  }

  @WithSpan(kind = SERVER)
  public void publishByReportingFlowName(String action, String pspId, String reportingFlowName) {
    Consumer<FdrInsertEntity> consumer =
        reportingFlowEntity -> {
          log.debug("Add FdrInsertEntity in queue flow message");
          conversionQueue.addQueueFlowMessage(
              FlowMessage.builder()
                  .name(reportingFlowEntity.getReportingFlowName())
                  .pspId(reportingFlowEntity.getSender().getPspId())
                  .retry(0L)
                  .revision(reportingFlowEntity.getRevision())
                  .build());
        };
    basePublishByReportingFlowName(action, pspId, reportingFlowName, consumer);
  }

  private void basePublishByReportingFlowName(
      String action,
      String pspId,
      String reportingFlowName,
      Consumer<FdrInsertEntity> funcConversionQueue) {
    log.infof(AppMessageUtil.logExecute(action));
    Instant now = Instant.now();

    FdrInsertEntity reportingFlowEntity =
        checkFdrInsertEntity(
            reportingFlowName,
            pspId,
            new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, reportingFlowName));

    MDC.put(EC_ID, reportingFlowEntity.getReceiver().getEcId());

    if (ReportingFlowStatusEnumEntity.INSERTED != reportingFlowEntity.getStatus()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION,
          reportingFlowName,
          reportingFlowEntity.getStatus());
    }

    reportingFlowEntity.setUpdated(now);
    reportingFlowEntity.setStatus(ReportingFlowStatusEnumEntity.PUBLISHED);
    log.debug("FdrInsertEntity PUBLISHED");

    log.debugf(
        "Existence check FdrPaymentInsertEntity by flowName[%s], pspId[%s]",
        reportingFlowName, pspId);
    List<FdrPaymentInsertEntity> paymentInsertEntities =
        FdrPaymentInsertEntity.findByFlowNameAndPspId(reportingFlowName, pspId)
            .project(FdrPaymentInsertEntity.class)
            .list();

    if (reportingFlowEntity.getRevision() > 1L) {
      log.debugf(
          "Delete FdrPublishEntity for FdrInsertEntity in revision[%d] by flowName[%s], pspId[%s]",
          reportingFlowEntity.getRevision(), reportingFlowName, pspId);
      FdrPublishEntity.deleteByFlowNameAndPspId(reportingFlowName, pspId);
      log.debugf(
          "Delete FdrPaymentPublishEntity for FdrInsertEntity in revision[%d] by flowName[%s],"
              + " pspId[%s]",
          reportingFlowEntity.getRevision(), reportingFlowName, pspId);
      FdrPaymentPublishEntity.deleteByFlowNameAndPspId(reportingFlowName, pspId);
    }

    FdrPublishEntity fdrPublishEntity = mapper.toFdrPublishEntity(reportingFlowEntity);
    fdrPublishEntity.persistEntity();
    List<FdrPaymentPublishEntity> fdrPaymentPublishEntities =
        mapper.toFdrPaymentPublishEntityList(paymentInsertEntities);
    FdrPaymentPublishEntity.persistFdrPaymentPublishEntities(fdrPaymentPublishEntities);

    log.debug("Mapping FdrHistoryEntity from reportingFlowEntity");
    FdrHistoryEntity fdrHistoryEntity = mapper.toFdrHistoryEntity(reportingFlowEntity);
    fdrHistoryEntity.persist();
    log.debug("Mapping FdrPaymentHistoryEntity from paymentInsertEntities");
    List<FdrPaymentHistoryEntity> fdrPaymentHistoryEntities =
        mapper.toFdrPaymentHistoryEntityList(paymentInsertEntities);
    FdrPaymentHistoryEntity.persistFdrPaymentHistoryEntities(fdrPaymentHistoryEntities);

    log.debug("Delete FdrInsertEntity");
    reportingFlowEntity.delete();
    log.debugf(
        "Delete FdrPaymentInsertEntity by flowName[%s], pspId[%s]",
        reportingFlowEntity.getRevision(), reportingFlowName, pspId);
    FdrPaymentInsertEntity.deleteByFlowNameAndPspId(reportingFlowName, pspId);

    // add to conversion queue
    funcConversionQueue.accept(reportingFlowEntity);

    String sessionId = org.slf4j.MDC.get(TRX_ID);
    reService.sendEvent(
        ReInternal.builder()
            .appVersion(AppVersionEnum.FDR003)
            .created(Instant.now())
            .sessionId(sessionId)
            .eventType(EventTypeEnum.INTERNAL)
            .flowPhisicalDelete(false)
            .flowStatus(FlowStatusEnum.PUBLISHED)
            //            .flowRead(false)
            .flowName(reportingFlowName)
            .pspId(pspId)
            .organizationId(reportingFlowEntity.getReceiver().getEcId())
            .revision(reportingFlowEntity.getRevision())
            .flowAction(FlowActionEnum.PUBLISH)
            .build());
  }

  @WithSpan(kind = SERVER)
  public void deleteByReportingFlowName(String action, String pspId, String reportingFlowName) {
    log.infof(AppMessageUtil.logExecute(action));

    FdrInsertEntity reportingFlowEntity =
        checkFdrInsertEntity(
            reportingFlowName,
            pspId,
            new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, reportingFlowName));

    MDC.put(EC_ID, reportingFlowEntity.getReceiver().getEcId());

    if (reportingFlowEntity.getTotPayments() > 0L) {
      log.debugf(
          "Delete FdrPaymentInsertEntity for FdrInsertEntity by flowName[%s]", reportingFlowName);
      FdrPaymentInsertEntity.deleteByFlowName(reportingFlowName);
    }
    log.debug("Delete FdrInsertEntity");
    reportingFlowEntity.delete();

    String sessionId = org.slf4j.MDC.get(TRX_ID);
    reService.sendEvent(
        ReInternal.builder()
            .appVersion(AppVersionEnum.FDR003)
            .created(Instant.now())
            .sessionId(sessionId)
            .eventType(EventTypeEnum.INTERNAL)
            .flowPhisicalDelete(true)
            .flowStatus(FlowStatusEnum.DELETED)
            //            .flowRead(false)
            .flowName(reportingFlowName)
            .pspId(pspId)
            .organizationId(reportingFlowEntity.getReceiver().getEcId())
            .revision(reportingFlowEntity.getRevision())
            .flowAction(FlowActionEnum.DELETE_FLOW)
            .build());
  }

  private static double addAndSum(
      FdrInsertEntity reportingFlowEntity,
      List<FdrPaymentInsertEntity> reportingFlowPaymentEntities) {
    return Double.sum(
        Objects.requireNonNullElseGet(reportingFlowEntity.getSumPayments(), () -> (double) 0),
        reportingFlowPaymentEntities.stream()
            .map(FdrPaymentInsertEntity::getPay)
            .mapToDouble(Double::doubleValue)
            .sum());
  }

  private static double deleteAndSubtract(
      FdrInsertEntity reportingFlowEntity, List<FdrPaymentInsertEntity> paymentToDelete) {
    return BigDecimal.valueOf(
            Objects.requireNonNullElseGet(reportingFlowEntity.getSumPayments(), () -> (double) 0))
        .subtract(
            BigDecimal.valueOf(
                paymentToDelete.stream()
                    .map(FdrPaymentInsertEntity::getPay)
                    .mapToDouble(Double::doubleValue)
                    .sum()))
        .doubleValue();
  }

  private static long addAndSumCount(
      FdrInsertEntity reportingFlowEntity,
      List<FdrPaymentInsertEntity> reportingFlowPaymentEntities) {
    return Objects.requireNonNullElseGet(reportingFlowEntity.getTotPayments(), () -> (long) 0)
        + reportingFlowPaymentEntities.size();
  }

  private static long deleteAndSumCount(
      FdrInsertEntity reportingFlowEntity,
      List<FdrPaymentInsertEntity> reportingFlowPaymentEntities) {
    return Objects.requireNonNullElseGet(reportingFlowEntity.getTotPayments(), () -> (long) 0)
        - reportingFlowPaymentEntities.size();
  }

  private FdrInsertEntity checkFdrInsertEntity(
      String reportingFlowName, String pspId, AppException appException) {
    log.debugf("Find FdrInsertEntity by flowName[%s], psp[%s]", reportingFlowName, pspId);
    return FdrInsertEntity.findByFlowNameAndPspId(reportingFlowName, pspId)
        .project(FdrInsertEntity.class)
        .firstResultOptional()
        .orElseThrow(() -> appException);
  }
}
