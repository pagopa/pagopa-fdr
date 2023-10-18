package it.gov.pagopa.fdr.service.psps;

import static io.opentelemetry.api.trace.SpanKind.SERVER;
import static it.gov.pagopa.fdr.util.MDCKeys.ORGANIZATION_ID;
import static it.gov.pagopa.fdr.util.MDCKeys.TRX_ID;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.repository.fdr.FdrInsertEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentInsertEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentPublishEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPublishEntity;
import it.gov.pagopa.fdr.repository.fdr.model.FdrStatusEnumEntity;
import it.gov.pagopa.fdr.repository.fdr.projection.FdrInsertProjection;
import it.gov.pagopa.fdr.repository.fdr.projection.FdrPublishRevisionProjection;
import it.gov.pagopa.fdr.service.conversion.ConversionService;
import it.gov.pagopa.fdr.service.conversion.message.FdrMessage;
import it.gov.pagopa.fdr.service.dto.AddPaymentDto;
import it.gov.pagopa.fdr.service.dto.DeletePaymentDto;
import it.gov.pagopa.fdr.service.dto.FdrAllCreatedDto;
import it.gov.pagopa.fdr.service.dto.FdrDto;
import it.gov.pagopa.fdr.service.dto.FdrGetCreatedDto;
import it.gov.pagopa.fdr.service.dto.FdrGetPaymentDto;
import it.gov.pagopa.fdr.service.dto.FdrSimpleCreatedDto;
import it.gov.pagopa.fdr.service.dto.MetadataDto;
import it.gov.pagopa.fdr.service.dto.PaymentDto;
import it.gov.pagopa.fdr.service.psps.mapper.PspsServiceServiceMapper;
import it.gov.pagopa.fdr.service.re.ReService;
import it.gov.pagopa.fdr.service.re.model.AppVersionEnum;
import it.gov.pagopa.fdr.service.re.model.EventTypeEnum;
import it.gov.pagopa.fdr.service.re.model.FdrActionEnum;
import it.gov.pagopa.fdr.service.re.model.FdrStatusEnum;
import it.gov.pagopa.fdr.service.re.model.ReInternal;
import it.gov.pagopa.fdr.util.AppDBUtil;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.slf4j.MDC;

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
        FdrPublishEntity.findByFdrAndPspId(fdr, pspId, Sort.descending("revision"))
            .project(FdrPublishRevisionProjection.class)
            .firstResultOptional();

    // sono stati tolti i check con il vecchio FDR, vale solo che se arriva stesso fdr con
    // stesso pspId si crea la rev2

    Long revision = fdrPublishedByfdr.map(r -> r.getRevision() + 1).orElse(1L);
    log.debugf("Mapping FdrInsertEntity from reportingFlowDto for revision {}", revision);
    FdrInsertEntity fdrEntity = mapper.toFdrInsertEntity(fdrDto);

    fdrEntity.setCreated(now);
    fdrEntity.setUpdated(now);
    fdrEntity.setStatus(FdrStatusEnumEntity.CREATED);
    fdrEntity.setComputedTotPayments(0L);
    fdrEntity.setComputedSumPayments(0.0);
    fdrEntity.setTotPayments(fdrDto.getTotPayments());
    fdrEntity.setSumPayments(fdrDto.getSumPayments());
    fdrEntity.setRevision(revision);
    fdrEntity.persist();

    String sessionId = org.slf4j.MDC.get(TRX_ID);
    reService.sendEvent(
        ReInternal.builder()
            .serviceIdentifier(AppVersionEnum.FDR003)
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

    fdrEntity.setComputedTotPayments(addAndSumCount(fdrEntity, reportingFlowPaymentEntities));
    fdrEntity.setComputedSumPayments(addAndSum(fdrEntity, reportingFlowPaymentEntities));

    fdrEntity.setUpdated(now);
    fdrEntity.setStatus(FdrStatusEnumEntity.INSERTED);
    fdrEntity.update();
    log.debug("FdrInsertEntity INSERTED");

    log.debug("Mapping FdrPaymentInsertEntity from addPaymentDto.getPayments()");
    FdrPaymentInsertEntity.persistFdrPaymentsInsert(
        reportingFlowPaymentEntities.stream()
            .peek(
                reportingFlowPaymentEntity -> {
                  reportingFlowPaymentEntity.setCreated(now);
                  reportingFlowPaymentEntity.setUpdated(now);
                  reportingFlowPaymentEntity.setRefFdrId(fdrEntity.id);
                  reportingFlowPaymentEntity.setRefFdr(fdrEntity.getFdr());
                  reportingFlowPaymentEntity.setRefFdrSenderPspId(fdrEntity.getSender().getPspId());
                  reportingFlowPaymentEntity.setRefFdrReceiverOrganizationId(
                      fdrEntity.getReceiver().getOrganizationId());
                  reportingFlowPaymentEntity.setRefFdrRevision(fdrEntity.getRevision());
                })
            .toList());

    String sessionId = org.slf4j.MDC.get(TRX_ID);
    reService.sendEvent(
        ReInternal.builder()
            .serviceIdentifier(AppVersionEnum.FDR003)
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

    long tot = deleteAndSumCount(fdrEntity, paymentToDelete);
    double sum = deleteAndSubtract(fdrEntity, paymentToDelete);
    FdrStatusEnumEntity status =
        sum > 0 ? FdrStatusEnumEntity.INSERTED : FdrStatusEnumEntity.CREATED;
    fdrEntity.setComputedTotPayments(tot);
    fdrEntity.setComputedSumPayments(sum);
    fdrEntity.setUpdated(now);
    fdrEntity.setStatus(status);
    fdrEntity.update();
    log.debugf("FdrInsertEntity %s", fdrEntity.getStatus().name());

    String sessionId = org.slf4j.MDC.get(TRX_ID);
    reService.sendEvent(
        ReInternal.builder()
            .serviceIdentifier(AppVersionEnum.FDR003)
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
  public void publishByFdr(String action, String pspId, String fdr, boolean internalPublish) {
    log.infof(AppMessageUtil.logExecute(action));

    FdrInsertEntity fdrEntity =
        checkFdrInsertEntity(
            fdr, pspId, new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, fdr));

    MDC.put(ORGANIZATION_ID, fdrEntity.getReceiver().getOrganizationId());

    if (FdrStatusEnumEntity.INSERTED != fdrEntity.getStatus()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION, fdr, fdrEntity.getStatus());
    }

    if (!fdrEntity.getTotPayments().equals(fdrEntity.getComputedTotPayments())) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_TOT_PAYMENT,
          fdr,
          fdrEntity.getTotPayments(),
          fdrEntity.getComputedTotPayments());
    }

    if (!fdrEntity.getSumPayments().equals(fdrEntity.getComputedSumPayments())) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_SUM_PAYMENT,
          fdr,
          fdrEntity.getSumPayments(),
          fdrEntity.getComputedSumPayments());
    }

    log.debug("FdrInsertEntity PUBLISHED");

    log.debugf("Existence check FdrPaymentInsertEntity by fdr[%s], pspId[%s]", fdr, pspId);
    List<FdrPaymentInsertEntity> paymentInsertEntities =
        FdrPaymentInsertEntity.findByFdrAndPspId(fdr, pspId)
            .project(FdrPaymentInsertEntity.class)
            .list();

    FdrPublishEntity fdrPublishEntity = mapper.toFdrPublishEntity(fdrEntity);
    Instant now = Instant.now();
    fdrPublishEntity.setUpdated(now);
    fdrPublishEntity.setPublished(now);
    fdrPublishEntity.setStatus(FdrStatusEnumEntity.PUBLISHED);
    fdrPublishEntity.persistEntity();
    List<FdrPaymentPublishEntity> fdrPaymentPublishEntities =
        mapper.toFdrPaymentPublishEntityList(paymentInsertEntities);
    FdrPaymentPublishEntity.persistFdrPaymentPublishEntities(fdrPaymentPublishEntities);

    log.debug("Delete FdrInsertEntity");
    fdrEntity.delete();
    log.debugf(
        "Delete FdrPaymentInsertEntity by fdr[%s], pspId[%s]", fdrEntity.getRevision(), fdr, pspId);
    FdrPaymentInsertEntity.deleteByFdrAndPspId(fdr, pspId);

    // add to conversion queue
    if (internalPublish) {
      log.debug("NOT Add FdrInsertEntity in queue fdr message");
    } else {
      log.debug("Add FdrInsertEntity in queue fdr message");
      conversionQueue.addQueueFlowMessage(
          FdrMessage.builder()
              .fdr(fdrEntity.getFdr())
              .pspId(fdrEntity.getSender().getPspId())
              .retry(0L)
              .revision(fdrEntity.getRevision())
              .build());
    }

    String sessionId = MDC.get(TRX_ID);
    reService.sendEvent(
        ReInternal.builder()
            .serviceIdentifier(AppVersionEnum.FDR003)
            .created(Instant.now())
            .sessionId(sessionId)
            .eventType(EventTypeEnum.INTERNAL)
            .fdrPhysicalDelete(false)
            .fdrStatus(FdrStatusEnum.PUBLISHED)
            .fdr(fdr)
            .pspId(pspId)
            .organizationId(fdrPublishEntity.getReceiver().getOrganizationId())
            .revision(fdrPublishEntity.getRevision())
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

    if (fdrEntity.getComputedTotPayments() > 0L) {
      log.debugf("Delete FdrPaymentInsertEntity for FdrInsertEntity by fdr[%s]", fdr);
      FdrPaymentInsertEntity.deleteByFdr(fdr);
    }
    log.debug("Delete FdrInsertEntity");
    fdrEntity.delete();

    String sessionId = org.slf4j.MDC.get(TRX_ID);
    reService.sendEvent(
        ReInternal.builder()
            .serviceIdentifier(AppVersionEnum.FDR003)
            .created(Instant.now())
            .sessionId(sessionId)
            .eventType(EventTypeEnum.INTERNAL)
            .fdrPhysicalDelete(true)
            .fdrStatus(FdrStatusEnum.DELETED)
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
        Objects.requireNonNullElseGet(fdrEntity.getComputedSumPayments(), () -> (double) 0),
        reportingFlowPaymentEntities.stream()
            .map(FdrPaymentInsertEntity::getPay)
            .mapToDouble(Double::doubleValue)
            .sum());
  }

  private static double deleteAndSubtract(
      FdrInsertEntity fdrEntity, List<FdrPaymentInsertEntity> paymentToDelete) {
    return BigDecimal.valueOf(
            Objects.requireNonNullElseGet(fdrEntity.getComputedSumPayments(), () -> (double) 0))
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
    return Objects.requireNonNullElseGet(fdrEntity.getComputedTotPayments(), () -> (long) 0)
        + reportingFlowPaymentEntities.size();
  }

  private static long deleteAndSumCount(
      FdrInsertEntity fdrEntity, List<FdrPaymentInsertEntity> reportingFlowPaymentEntities) {
    return Objects.requireNonNullElseGet(fdrEntity.getComputedTotPayments(), () -> (long) 0)
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

  @WithSpan(kind = SERVER)
  public FdrAllCreatedDto find(
      String action, String pspId, Instant createdGt, long pageNumber, long pageSize) {
    log.infof(AppMessageUtil.logExecute(action));

    Page page = Page.of((int) pageNumber - 1, (int) pageSize);
    Sort sort = AppDBUtil.getSort(List.of("_id,asc"));

    List<String> queryAnd = new ArrayList<>();
    Parameters parameters = new Parameters();

    if (pspId != null && !pspId.isBlank()) {
      queryAnd.add("sender.psp_id = :pspId");
      parameters.and("pspId", pspId);
    }

    if (createdGt != null) {
      queryAnd.add("created > :createdGt");
      parameters.and("createdGt", createdGt);
    }

    log.debugf("Get all FdrInsertedEntity");
    PanacheQuery<FdrInsertEntity> fdrInsertEntityPanacheQuery;
    if (queryAnd.isEmpty()) {
      log.debugf("Get all FdrInsertedEntity");
      fdrInsertEntityPanacheQuery = FdrInsertEntity.findAll(sort);
    } else {
      log.debugf("Get all FdrInsertedEntity with pspId[%s]", pspId);
      fdrInsertEntityPanacheQuery =
          FdrInsertEntity.find(String.join(" and ", queryAnd), sort, parameters);
    }

    log.debug("Get paging FdrInsertedReportingFlowNameProjection");
    PanacheQuery<FdrInsertProjection> fdrProjectionPanacheQuery =
        fdrInsertEntityPanacheQuery.page(page).project(FdrInsertProjection.class);

    List<FdrInsertProjection> reportingFlowIds = fdrProjectionPanacheQuery.list();

    long totPage = fdrProjectionPanacheQuery.pageCount();
    long countReportingFlow = fdrProjectionPanacheQuery.count();

    log.debug("Building ReportingFlowByIdEcDto");
    return FdrAllCreatedDto.builder()
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
                        FdrSimpleCreatedDto.builder()
                            .fdr(rf.getFdr())
                            .created(rf.getCreated())
                            .revision(rf.getRevision())
                            .pspId(rf.getSender().getPspId())
                            .build())
                .toList())
        .build();
  }

  @WithSpan(kind = SERVER)
  public FdrGetCreatedDto findByReportingFlowName(String action, String fdr, String pspId) {
    log.infof(AppMessageUtil.logExecute(action));

    log.debugf("Existence check FdrInsertEntity by fdr[%s], psp[%s]", fdr, pspId);
    FdrInsertEntity fdrInsertPanacheQuery =
        FdrInsertEntity.findByFdrAndRevAndPspId(fdr, pspId)
            .project(FdrInsertEntity.class)
            .firstResultOptional()
            .orElseThrow(
                () -> new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, fdr));

    MDC.put(ORGANIZATION_ID, fdrInsertPanacheQuery.getReceiver().getOrganizationId());

    log.debug("Mapping ReportingFlowGetDto from FdrInsertEntity");
    return mapper.toFdrGetCreatedDto(fdrInsertPanacheQuery);
  }

  @WithSpan(kind = SERVER)
  public FdrGetPaymentDto findPaymentByReportingFlowName(
      String action, String fdr, String pspId, long pageNumber, long pageSize) {
    log.infof(AppMessageUtil.logExecute(action));

    Page page = Page.of((int) pageNumber - 1, (int) pageSize);
    Sort sort = AppDBUtil.getSort(List.of("index,asc"));

    log.debugf("Existence check fdr by fdr[%s], psp[%s]", fdr, pspId);
    PanacheQuery<FdrPaymentInsertEntity> fdrPaymentInsertPanacheQuery =
        FdrPaymentInsertEntity.findByFdrAndPspIdSort(fdr, pspId, sort).page(page);

    List<FdrPaymentInsertEntity> list = fdrPaymentInsertPanacheQuery.list();

    long totPage = fdrPaymentInsertPanacheQuery.pageCount();
    long countReportingFlowPayment = fdrPaymentInsertPanacheQuery.count();

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
