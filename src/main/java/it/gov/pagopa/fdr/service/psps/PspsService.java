package it.gov.pagopa.fdr.service.psps;

import static io.opentelemetry.api.trace.SpanKind.SERVER;
import static it.gov.pagopa.fdr.util.MDCKeys.ORGANIZATION_ID;
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
import it.gov.pagopa.fdr.repository.fdr.model.FdrStatusEnumEntity;
import it.gov.pagopa.fdr.repository.fdr.projection.FdrPublishRevisionProjection;
import it.gov.pagopa.fdr.service.conversion.ConversionService;
import it.gov.pagopa.fdr.service.conversion.message.FdrMessage;
import it.gov.pagopa.fdr.service.dto.AddPaymentDto;
import it.gov.pagopa.fdr.service.dto.DeletePaymentDto;
import it.gov.pagopa.fdr.service.dto.FdrDto;
import it.gov.pagopa.fdr.service.dto.PaymentDto;
import it.gov.pagopa.fdr.service.psps.mapper.PspsServiceServiceMapper;
import it.gov.pagopa.fdr.service.re.ReService;
import it.gov.pagopa.fdr.service.re.model.AppVersionEnum;
import it.gov.pagopa.fdr.service.re.model.EventTypeEnum;
import it.gov.pagopa.fdr.service.re.model.FdrActionEnum;
import it.gov.pagopa.fdr.service.re.model.FdrStatusEnum;
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
  public void save(String action, FdrDto fdrDto) {
    log.infof(AppMessageUtil.logExecute(action));

    Instant now = Instant.now();
    String fdr = fdrDto.getFdr();
    String pspId = fdrDto.getSender().getPspId();
    String ecId = fdrDto.getReceiver().getOrganizationId();

    log.debugf("Existence check FdrInsertEntity by fdr[%s], psp[%s]", fdr, pspId);

    Optional<FdrInsertEntity> byfdr =
        FdrInsertEntity.findByFdrAndPspId(fdr, pspId).firstResultOptional();

    if (byfdr.isPresent()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_ALREADY_EXIST, fdr, byfdr.get().getStatus());
    }

    log.debugf("Get FdrPublishRevision by fdr[%s], psp[%s]", fdr, pspId);
    Optional<FdrPublishRevisionProjection> fdrPublishedByfdr =
        FdrPublishEntity.findByFdrAndPspId(fdr, pspId)
            .project(FdrPublishRevisionProjection.class)
            .firstResultOptional();

    // sono stati tolti i check con il vecchio FDR, vale solo che se arriva stesso fdr con
    // stesso pspId si crea la rev2

    Long revision = fdrPublishedByfdr.map(r -> r.getRevision() + 1).orElse(1L);
    log.debug("Mapping FdrInsertEntity from reportingFlowDto");
    FdrInsertEntity fdrEntity = mapper.toFdrInsertEntity(fdrDto);

    fdrEntity.setCreated(now);
    fdrEntity.setUpdated(now);
    fdrEntity.setStatus(FdrStatusEnumEntity.CREATED);
    fdrEntity.setTotPayments(0L);
    fdrEntity.setSumPayments(0.0);
    fdrEntity.setRevision(revision);
    fdrEntity.persist();

    String sessionId = org.slf4j.MDC.get(TRX_ID);
    reService.sendEvent(
        ReInternal.builder()
            .appVersion(AppVersionEnum.FDR003)
            .created(Instant.now())
            .sessionId(sessionId)
            .eventType(EventTypeEnum.INTERNAL)
            .fdrPhysicalDelete(false)
            .fdrStatus(FdrStatusEnum.CREATED)
            //            .flowRead(false)
            .fdr(fdr)
            .pspId(pspId)
            .organizationId(ecId)
            .revision(revision)
            .fdrAction(FdrActionEnum.CREATE_FLOW)
            .build());
    log.debug("FdrInsertEntity CREATED");
  }

  @WithSpan(kind = SERVER)
  public void addPayment(String action, String pspId, String fdr, AddPaymentDto addPaymentDto) {
    log.infof(AppMessageUtil.logExecute(action));
    Instant now = Instant.now();

    FdrInsertEntity fdrEntity =
        checkFdrInsertEntity(
            fdr, pspId, new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, fdr));

    MDC.put(ORGANIZATION_ID, fdrEntity.getReceiver().getOrganizationId());

    log.debug("Check payments indexes");
    List<Long> indexList = addPaymentDto.getPayments().stream().map(PaymentDto::getIndex).toList();
    if (indexList.size() != indexList.stream().distinct().toList().size()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_SAME_INDEX_IN_SAME_REQUEST, fdr);
    }

    log.debugf("Existence check FdrPaymentInsertEntity by fdr[%s], indexList", fdr);
    List<FdrPaymentInsertEntity> paymentIndexAlreadyExist =
        FdrPaymentInsertEntity.findByFdrAndIndexes(fdr, indexList)
            .project(FdrPaymentInsertEntity.class)
            .list();

    if (paymentIndexAlreadyExist != null && !paymentIndexAlreadyExist.isEmpty()) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_DUPLICATE_INDEX, fdr);
    }

    log.debug("Mapping FdrPaymentInsertEntity from addPaymentDto.getPayments()");
    List<FdrPaymentInsertEntity> reportingFlowPaymentEntities =
        mapper.toFdrPaymentInsertEntityList(addPaymentDto.getPayments());

    fdrEntity.setTotPayments(addAndSumCount(fdrEntity, reportingFlowPaymentEntities));
    fdrEntity.setSumPayments(addAndSum(fdrEntity, reportingFlowPaymentEntities));

    fdrEntity.setUpdated(now);
    fdrEntity.setStatus(FdrStatusEnumEntity.INSERTED);
    fdrEntity.update();
    log.debug("FdrInsertEntity INSERTED");

    log.debug("Mapping FdrPaymentInsertEntity from addPaymentDto.getPayments()");
    FdrPaymentInsertEntity.persistFdrPaymentsInsert(
        reportingFlowPaymentEntities.stream()
            .map(
                reportingFlowPaymentEntity -> {
                  reportingFlowPaymentEntity.setCreated(now);
                  reportingFlowPaymentEntity.setUpdated(now);
                  reportingFlowPaymentEntity.setRefFdrId(fdrEntity.id);
                  reportingFlowPaymentEntity.setRefFdr(fdrEntity.getFdr());
                  reportingFlowPaymentEntity.setRefFdrSenderPspId(fdrEntity.getSender().getPspId());
                  reportingFlowPaymentEntity.setRefFdrRevision(fdrEntity.getRevision());
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
            .fdrPhysicalDelete(false)
            .fdrStatus(FdrStatusEnum.INSERTED)
            //            .flowRead(false)
            .fdr(fdr)
            .pspId(pspId)
            .organizationId(fdrEntity.getReceiver().getOrganizationId())
            .revision(fdrEntity.getRevision())
            .fdrAction(FdrActionEnum.ADD_PAYMENT)
            .build());
  }

  @WithSpan(kind = SERVER)
  public void deletePayment(
      String action, String pspId, String fdr, DeletePaymentDto deletePaymentDto) {
    log.infof(AppMessageUtil.logExecute(action));
    Instant now = Instant.now();

    FdrInsertEntity fdrEntity =
        checkFdrInsertEntity(
            fdr, pspId, new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, fdr));

    MDC.put(ORGANIZATION_ID, fdrEntity.getReceiver().getOrganizationId());

    if (FdrStatusEnumEntity.INSERTED != fdrEntity.getStatus()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION, fdr, fdrEntity.getStatus());
    }

    List<Long> indexList = deletePaymentDto.getIndexList();
    if (indexList.size() != indexList.stream().distinct().toList().size()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_SAME_INDEX_IN_SAME_REQUEST, fdr);
    }

    log.debugf("Existence check FdrPaymentInsertEntity to delete by fdr[%s], indexList", fdr);
    List<FdrPaymentInsertEntity> paymentToDelete =
        FdrPaymentInsertEntity.findByFdrAndIndexes(fdr, indexList)
            .project(FdrPaymentInsertEntity.class)
            .list();
    if (!paymentToDelete.stream()
        .map(FdrPaymentInsertEntity::getIndex)
        .collect(Collectors.toSet())
        .containsAll(indexList)) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_NO_MATCH_INDEX, fdr);
    }

    log.debugf("Delete FdrPaymentInsertEntity by fdr[%s], indexList", fdr);
    FdrPaymentInsertEntity.deleteByFdrAndIndexes(fdr, indexList);

    FdrStatusEnumEntity status =
        fdrEntity.getSumPayments() > 0 ? FdrStatusEnumEntity.INSERTED : FdrStatusEnumEntity.CREATED;
    fdrEntity.setTotPayments(deleteAndSumCount(fdrEntity, paymentToDelete));
    fdrEntity.setSumPayments(deleteAndSubtract(fdrEntity, paymentToDelete));
    fdrEntity.setUpdated(now);
    fdrEntity.setStatus(status);
    fdrEntity.update();
    log.debugf("FdrInsertEntity %s", fdrEntity.getStatus().name());

    String sessionId = org.slf4j.MDC.get(TRX_ID);
    reService.sendEvent(
        ReInternal.builder()
            .appVersion(AppVersionEnum.FDR003)
            .created(Instant.now())
            .sessionId(sessionId)
            .eventType(EventTypeEnum.INTERNAL)
            .fdrPhysicalDelete(false)
            .fdrStatus(
                FdrStatusEnumEntity.INSERTED == status
                    ? FdrStatusEnum.INSERTED
                    : FdrStatusEnum.CREATED)
            //            .flowRead(false)
            .fdr(fdr)
            .pspId(pspId)
            .organizationId(fdrEntity.getReceiver().getOrganizationId())
            .revision(fdrEntity.getRevision())
            .fdrAction(FdrActionEnum.DELETE_PAYMENT)
            .build());
  }

  @WithSpan(kind = SERVER)
  public void internalPublishByFdr(String action, String pspId, String fdr) {
    Consumer<FdrInsertEntity> consumer =
        fdrEntity -> log.debug("NOT Add FdrInsertEntity in queue fdr message");
    basePublishByfdr(action, pspId, fdr, consumer);
  }

  @WithSpan(kind = SERVER)
  public void publishByFdr(String action, String pspId, String fdr) {
    Consumer<FdrInsertEntity> consumer =
        fdrEntity -> {
          log.debug("Add FdrInsertEntity in queue fdr message");
          conversionQueue.addQueueFlowMessage(
              FdrMessage.builder()
                  .fdr(fdrEntity.getFdr())
                  .pspId(fdrEntity.getSender().getPspId())
                  .retry(0L)
                  .revision(fdrEntity.getRevision())
                  .build());
        };
    basePublishByfdr(action, pspId, fdr, consumer);
  }

  private void basePublishByfdr(
      String action, String pspId, String fdr, Consumer<FdrInsertEntity> funcConversionQueue) {
    log.infof(AppMessageUtil.logExecute(action));
    Instant now = Instant.now();

    FdrInsertEntity fdrEntity =
        checkFdrInsertEntity(
            fdr, pspId, new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, fdr));

    MDC.put(ORGANIZATION_ID, fdrEntity.getReceiver().getOrganizationId());

    if (FdrStatusEnumEntity.INSERTED != fdrEntity.getStatus()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION, fdr, fdrEntity.getStatus());
    }

    fdrEntity.setUpdated(now);
    fdrEntity.setStatus(FdrStatusEnumEntity.PUBLISHED);
    log.debug("FdrInsertEntity PUBLISHED");

    log.debugf("Existence check FdrPaymentInsertEntity by fdr[%s], pspId[%s]", fdr, pspId);
    List<FdrPaymentInsertEntity> paymentInsertEntities =
        FdrPaymentInsertEntity.findByFdrAndPspId(fdr, pspId)
            .project(FdrPaymentInsertEntity.class)
            .list();

    if (fdrEntity.getRevision() > 1L) {
      log.debugf(
          "Delete FdrPublishEntity for FdrInsertEntity in revision[%d] by fdr[%s], pspId[%s]",
          fdrEntity.getRevision(), fdr, pspId);
      FdrPublishEntity.deleteByFdrAndPspId(fdr, pspId);
      log.debugf(
          "Delete FdrPaymentPublishEntity for FdrInsertEntity in revision[%d] by fdr[%s],"
              + " pspId[%s]",
          fdrEntity.getRevision(), fdr, pspId);
      FdrPaymentPublishEntity.deleteByFdrAndPspId(fdr, pspId);
    }

    FdrPublishEntity fdrPublishEntity = mapper.toFdrPublishEntity(fdrEntity);
    fdrPublishEntity.persistEntity();
    List<FdrPaymentPublishEntity> fdrPaymentPublishEntities =
        mapper.toFdrPaymentPublishEntityList(paymentInsertEntities);
    FdrPaymentPublishEntity.persistFdrPaymentPublishEntities(fdrPaymentPublishEntities);

    log.debug("Mapping FdrHistoryEntity from fdrEntity");
    FdrHistoryEntity fdrHistoryEntity = mapper.toFdrHistoryEntity(fdrEntity);
    fdrHistoryEntity.persist();
    log.debug("Mapping FdrPaymentHistoryEntity from paymentInsertEntities");
    List<FdrPaymentHistoryEntity> fdrPaymentHistoryEntities =
        mapper.toFdrPaymentHistoryEntityList(paymentInsertEntities);
    FdrPaymentHistoryEntity.persistFdrPaymentHistoryEntities(fdrPaymentHistoryEntities);

    log.debug("Delete FdrInsertEntity");
    fdrEntity.delete();
    log.debugf(
        "Delete FdrPaymentInsertEntity by fdr[%s], pspId[%s]", fdrEntity.getRevision(), fdr, pspId);
    FdrPaymentInsertEntity.deleteByFdrAndPspId(fdr, pspId);

    // add to conversion queue
    funcConversionQueue.accept(fdrEntity);

    String sessionId = org.slf4j.MDC.get(TRX_ID);
    reService.sendEvent(
        ReInternal.builder()
            .appVersion(AppVersionEnum.FDR003)
            .created(Instant.now())
            .sessionId(sessionId)
            .eventType(EventTypeEnum.INTERNAL)
            .fdrPhysicalDelete(false)
            .fdrStatus(FdrStatusEnum.PUBLISHED)
            //            .flowRead(false)
            .fdr(fdr)
            .pspId(pspId)
            .organizationId(fdrEntity.getReceiver().getOrganizationId())
            .revision(fdrEntity.getRevision())
            .fdrAction(FdrActionEnum.PUBLISH)
            .build());
  }

  @WithSpan(kind = SERVER)
  public void deleteByFdr(String action, String pspId, String fdr) {
    log.infof(AppMessageUtil.logExecute(action));

    FdrInsertEntity fdrEntity =
        checkFdrInsertEntity(
            fdr, pspId, new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, fdr));

    MDC.put(ORGANIZATION_ID, fdrEntity.getReceiver().getOrganizationId());

    if (fdrEntity.getTotPayments() > 0L) {
      log.debugf("Delete FdrPaymentInsertEntity for FdrInsertEntity by fdr[%s]", fdr);
      FdrPaymentInsertEntity.deleteByFdr(fdr);
    }
    log.debug("Delete FdrInsertEntity");
    fdrEntity.delete();

    String sessionId = org.slf4j.MDC.get(TRX_ID);
    reService.sendEvent(
        ReInternal.builder()
            .appVersion(AppVersionEnum.FDR003)
            .created(Instant.now())
            .sessionId(sessionId)
            .eventType(EventTypeEnum.INTERNAL)
            .fdrPhysicalDelete(true)
            .fdrStatus(FdrStatusEnum.DELETED)
            //            .flowRead(false)
            .fdr(fdr)
            .pspId(pspId)
            .organizationId(fdrEntity.getReceiver().getOrganizationId())
            .revision(fdrEntity.getRevision())
            .fdrAction(FdrActionEnum.DELETE_FLOW)
            .build());
  }

  private static double addAndSum(
      FdrInsertEntity fdrEntity, List<FdrPaymentInsertEntity> reportingFlowPaymentEntities) {
    return Double.sum(
        Objects.requireNonNullElseGet(fdrEntity.getSumPayments(), () -> (double) 0),
        reportingFlowPaymentEntities.stream()
            .map(FdrPaymentInsertEntity::getPay)
            .mapToDouble(Double::doubleValue)
            .sum());
  }

  private static double deleteAndSubtract(
      FdrInsertEntity fdrEntity, List<FdrPaymentInsertEntity> paymentToDelete) {
    return BigDecimal.valueOf(
            Objects.requireNonNullElseGet(fdrEntity.getSumPayments(), () -> (double) 0))
        .subtract(
            BigDecimal.valueOf(
                paymentToDelete.stream()
                    .map(FdrPaymentInsertEntity::getPay)
                    .mapToDouble(Double::doubleValue)
                    .sum()))
        .doubleValue();
  }

  private static long addAndSumCount(
      FdrInsertEntity fdrEntity, List<FdrPaymentInsertEntity> reportingFlowPaymentEntities) {
    return Objects.requireNonNullElseGet(fdrEntity.getTotPayments(), () -> (long) 0)
        + reportingFlowPaymentEntities.size();
  }

  private static long deleteAndSumCount(
      FdrInsertEntity fdrEntity, List<FdrPaymentInsertEntity> reportingFlowPaymentEntities) {
    return Objects.requireNonNullElseGet(fdrEntity.getTotPayments(), () -> (long) 0)
        - reportingFlowPaymentEntities.size();
  }

  private FdrInsertEntity checkFdrInsertEntity(
      String fdr, String pspId, AppException appException) {
    log.debugf("Find FdrInsertEntity by fdr[%s], psp[%s]", fdr, pspId);
    return FdrInsertEntity.findByFdrAndPspId(fdr, pspId)
        .project(FdrInsertEntity.class)
        .firstResultOptional()
        .orElseThrow(() -> appException);
  }
}
