package it.gov.pagopa.fdr.service;

import io.micrometer.core.annotation.Timed;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.controller.model.common.response.GenericResponse;
import it.gov.pagopa.fdr.controller.model.flow.request.CreateFlowRequest;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowCreatedResponse;
import it.gov.pagopa.fdr.controller.model.payment.Payment;
import it.gov.pagopa.fdr.controller.model.payment.request.AddPaymentRequest;
import it.gov.pagopa.fdr.controller.model.payment.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.repository.FlowRepository;
import it.gov.pagopa.fdr.repository.FlowToHistoryRepository;
import it.gov.pagopa.fdr.repository.PaymentRepository;
import it.gov.pagopa.fdr.repository.entity.FlowEntity;
import it.gov.pagopa.fdr.repository.entity.FlowToHistoryEntity;
import it.gov.pagopa.fdr.repository.entity.PaymentEntity;
import it.gov.pagopa.fdr.repository.enums.FlowStatusEnum;
import it.gov.pagopa.fdr.service.middleware.mapper.FlowMapper;
import it.gov.pagopa.fdr.service.middleware.mapper.FlowToHistoryMapper;
import it.gov.pagopa.fdr.service.middleware.mapper.PaymentMapper;
import it.gov.pagopa.fdr.service.model.arguments.FindFlowsByFiltersArgs;
import it.gov.pagopa.fdr.service.model.re.*;
import it.gov.pagopa.fdr.util.constant.MDCKeys;
import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.error.exception.common.AppException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.jboss.logging.Logger;
import org.slf4j.MDC;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.opentelemetry.api.trace.SpanKind.SERVER;
import static it.gov.pagopa.fdr.util.constant.MDCKeys.IS_RE_ENABLED_FOR_THIS_CALL;

@ApplicationScoped
public class InternalService {

  private final Logger log;

  private final ReService reService;

  private final Config cachedConfig;

  private final FlowRepository flowRepository;

  private final FlowToHistoryRepository flowToHistoryRepository;

  private final PaymentRepository paymentRepository;

  private final FlowMapper flowMapper;

  private final FlowToHistoryMapper flowToHistoryMapper;

  private final PaymentMapper paymentMapper;

  public InternalService(
      Logger log,
      ReService reService,
      Config cachedConfig,
      FlowRepository flowRepository,
      FlowToHistoryRepository flowToHistoryRepository,
      PaymentRepository paymentRepository,
      FlowMapper flowMapper,
      FlowToHistoryMapper flowToHistoryMapper,
      PaymentMapper paymentMapper) {

    this.log = log;
    this.reService = reService;
    this.cachedConfig = cachedConfig;
    this.flowRepository = flowRepository;
    this.flowToHistoryRepository = flowToHistoryRepository;
    this.paymentRepository = paymentRepository;
    this.flowMapper = flowMapper;
    this.flowToHistoryMapper = flowToHistoryMapper;
    this.paymentMapper = paymentMapper;
  }

  @WithSpan(kind = SERVER)
  @Transactional(rollbackOn = Exception.class)
  @Timed(value = "paymentService.createEmptyFlow.task", description = "Time taken to perform createEmptyFlow", percentiles = 0.95, histogram = true)
  public GenericResponse createEmptyFlow(String pspId, String flowName, CreateFlowRequest request) {

    log.debugf(
        "Saving new flows by organizationId [%s], pspId [%s], flowName [%s]",
        request.getReceiver().getOrganizationId(), pspId, flowName);

    // check if there is already another unpublished flow that is in progress
    Optional<FlowEntity> optPublishingFlow =
        flowRepository.findUnpublishedByPspIdAndNameReadOnly(pspId, flowName);
    if (optPublishingFlow.isPresent()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_ALREADY_EXIST,
          flowName,
          optPublishingFlow.get().getStatus());
    }

    // retrieve the last published flow, in order to take its revision and increment it
    Optional<FlowEntity> lastPublishedFlow =
        flowRepository.findLastPublishedByPspIdAndName(pspId, flowName);

    // incrementing revision value using the revision of the last flow
    Long revision = lastPublishedFlow.map(flowEntity -> (flowEntity.getRevision() + 1)).orElse(1L);

    // finally, persist the newly generated entity
    FlowEntity entity = flowMapper.toEntity(request, revision);
    this.flowRepository.createEntity(entity);

    // Send event to Registro Eventi for internal operation
    storeInternalREEvent(entity, FdrStatusEnum.CREATED, FdrActionEnum.CREATE_FLOW);

    return GenericResponse.builder().message(String.format("Fdr [%s] saved", flowName)).build();
  }

  @WithSpan(kind = SERVER)
  @Transactional(rollbackOn = Exception.class)
  @Timed(value = "paymentService.publishFlow.task", description = "Time taken to perform publishFlow", percentiles = 0.95, histogram = true)
  public GenericResponse publishFlow(String pspId, String flowName, boolean isInternalCall) {

    log.debugf("Publishing existing flows by pspId [%s], flowName [%s]", pspId, flowName);

    // check if there is an unpublished flow that is in progress
    Optional<FlowEntity> optPublishingFlow =
        flowRepository.findUnpublishedByPspIdAndName(pspId, flowName);
    if (optPublishingFlow.isEmpty()) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, flowName);
    }
    FlowEntity publishingFlow = optPublishingFlow.get();
    if (!FlowStatusEnum.INSERTED.name().equals(publishingFlow.getStatus())) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION,
          flowName,
          publishingFlow.getStatus());
    }

    // publish new flow
    publishNewRevision(pspId, flowName, publishingFlow);

    FlowToHistoryEntity flowToHistoryEntity =
        flowToHistoryMapper.toEntity(publishingFlow, isInternalCall);
    this.flowToHistoryRepository.createEntity(flowToHistoryEntity);

    // Send event to Registro Eventi for internal operation
    storeInternalREEvent(publishingFlow, FdrStatusEnum.PUBLISHED, FdrActionEnum.PUBLISH);

    return GenericResponse.builder().message(String.format("Fdr [%s] published", flowName)).build();
  }

  @WithSpan(kind = SERVER)
  @Transactional(rollbackOn = Exception.class)
  public GenericResponse deleteExistingFlow(String pspId, String flowName) {

    log.debugf("Deleting existing flows by pspId [%s], flowName [%s]", pspId, flowName);

    // check if there is already another unpublished flow that is in progress
    Optional<FlowEntity> optPublishingFlow =
        flowRepository.findUnpublishedByPspIdAndName(pspId, flowName);
    if (optPublishingFlow.isEmpty()) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, flowName);
    }

    // delete flow and if there are multiple payments related to it yet, delete them in async mode
    FlowEntity publishingFlow = optPublishingFlow.get();
    this.flowRepository.deleteEntity(publishingFlow);

    // Send event to Registro Eventi for internal operation
    storeInternalREEvent(publishingFlow, FdrStatusEnum.DELETED, FdrActionEnum.DELETE_FLOW);

    return GenericResponse.builder().message(String.format("Fdr [%s] deleted", flowName)).build();
  }

  @WithSpan(kind = SERVER)
  public SingleFlowCreatedResponse getSingleFlowNotInPublishedStatus(FindFlowsByFiltersArgs args) {

    String organizationId = args.getOrganizationId();
    String pspId = args.getPspId();
    String flowName = args.getFlowName();

    log.debugf(
        "Executing query on unpublished flows by organizationId [%s], pspId [%s] flowName [%s]",
        organizationId, pspId, flowName);

    Optional<FlowEntity> result =
        this.flowRepository.findUnpublishedByOrganizationIdAndPspIdAndName(
            organizationId, pspId, flowName);
    if (result.isEmpty()) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, flowName);
    }

    log.debugf("Entity found. Mapping data to final response.");
    return this.flowMapper.toSingleFlowCreatedResponse(result.get());
  }

  @WithSpan(kind = SERVER)
  @Transactional(rollbackOn = Exception.class)
  @Timed(value = "paymentService.addPaymentToExistingFlow.task", description = "Time taken to perform addPaymentToExistingFlow", percentiles = 0.95, histogram = true)
  public GenericResponse addPaymentToExistingFlow(String pspId, String flowName, AddPaymentRequest request) {

      log.debugf(
              "Adding [%s] new payments on flow [%s], pspId [%s]",
              request.getPayments().size(), flowName, pspId
      );

      // check if there is an unpublished flow on which is possible to add payments
      Optional<FlowEntity> optPublishingFlow = flowRepository.findUnpublishedByPspIdAndNameReadOnly(pspId, flowName);
      if (optPublishingFlow.isEmpty()) {
          throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, flowName);
      }

      // check if there is any payment that uses at least one of passed indexes
      FlowEntity publishingFlow = optPublishingFlow.get();
      List<Payment> paymentsToAdd = request.getPayments();
      Set<Long> indexes = paymentsToAdd.stream().map(Payment::getIndex).collect(Collectors.toSet());
      // remove count -> execute only 1 query
      List<PaymentEntity> indexesAlreadyAdded = paymentRepository.findByFlowIdAndIndexes(publishingFlow.getId(), indexes);
      if (!indexesAlreadyAdded.isEmpty()) {
          List<Long> conflictingIndexes = indexesAlreadyAdded.stream().map(PaymentEntity::getIndex).toList();
          throw new AppException(
                  AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_DUPLICATE_INDEX,
                  conflictingIndexes,
                  flowName
          );
      }

      // create all entities in batch, from each payment to be added, in transactional way
      Instant now = Instant.now();
      List<PaymentEntity> paymentEntities = paymentMapper.toEntity(publishingFlow, paymentsToAdd, now);
      addPaymentToExistingFlowInTransaction(publishingFlow, paymentEntities, now);

      // Send event to Registro Eventi for internal operation
      storeInternalREEvent(publishingFlow, FdrStatusEnum.INSERTED, FdrActionEnum.ADD_PAYMENT);

      return GenericResponse.builder()
              .message(String.format("Fdr [%s] payment added", flowName))
              .build();
  }

  public void publishNewRevision(String pspId, String flowName, FlowEntity publishingFlow) {

    this.flowRepository.updateLastPublishedAsNotLatest(pspId, flowName);

    // update the publishing flow in order to set its status to PUBLISHED
    Instant now = Instant.now();
    publishingFlow.setUpdated(now);
    publishingFlow.setPublished(now);
    publishingFlow.setIsLatest(true);
    publishingFlow.setStatus(FlowStatusEnum.PUBLISHED.name());
    this.flowRepository.updateEntity(publishingFlow);
  }

  @SneakyThrows
  private void addPaymentToExistingFlowInTransaction(FlowEntity publishingFlow, List<PaymentEntity> paymentEntities, Instant now) {

    long paymentsToAdd = paymentEntities.size();

    BigDecimal amountToAdd = paymentEntities.stream()
        .map(PaymentEntity::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    this.paymentRepository.createEntityInBulk(paymentEntities);

    flowRepository.updateComputedValues(
        publishingFlow.getId(),
        paymentsToAdd,
        amountToAdd,
        now,
        FlowStatusEnum.INSERTED
    );
  }

  @WithSpan(kind = SERVER)
  @Transactional(rollbackOn = Exception.class)
  public GenericResponse deletePaymentFromExistingFlow(
      String pspId, String flowName, DeletePaymentRequest request) {

    log.debugf(
        "Deleting [%s] payments on flow [%s], pspId [%s]",
        request.getIndexList().size(), flowName, pspId);

    // check if there is an unpublished flow on which is possible to add payments
    Optional<FlowEntity> optPublishingFlow =
        this.flowRepository.findUnpublishedByPspIdAndNameReadOnly(pspId, flowName);
    if (optPublishingFlow.isEmpty()) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, flowName);
    }
    FlowEntity publishingFlow = optPublishingFlow.get();
    if (!FlowStatusEnum.INSERTED.name().equals(publishingFlow.getStatus())) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION,
          flowName,
          publishingFlow.getStatus());
    }

    // check if each passed index refers to an existing payment
    Set<Long> indexes = new HashSet<>(request.getIndexList());
    List<PaymentEntity> paymentEntities =
        this.paymentRepository.findByFlowIdAndIndexes(publishingFlow.getId(), indexes);
    boolean containsAllIndexes =
        paymentEntities.stream()
            .map(PaymentEntity::getIndex)
            .collect(Collectors.toSet())
            .containsAll(indexes);
    if (!containsAllIndexes) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_NO_MATCH_INDEX, flowName);
    }

    // create all entities in batch, from each payment to be added, in transactional way
    Instant now = Instant.now();
    deletePaymentToExistingFlowInTransaction(publishingFlow, paymentEntities, now);

    // send events to Registro Eventi for internal operation
    FdrStatusEnum status =
        FlowStatusEnum.INSERTED.name().equals(publishingFlow.getStatus())
            ? FdrStatusEnum.INSERTED
            : FdrStatusEnum.CREATED;
    storeInternalREEvent(publishingFlow, status, FdrActionEnum.DELETE_PAYMENT);

    return GenericResponse.builder()
        .message(String.format("Fdr [%s] payment deleted", flowName))
        .build();
  }

  @SneakyThrows
  private void deletePaymentToExistingFlowInTransaction(
      FlowEntity publishingFlow, List<PaymentEntity> paymentEntities, Instant now) {

    // generate quantity to subtract on computed values (evaluated as negative value)
    long paymentsToAdd = -1L * paymentEntities.size();
    BigDecimal amountToAdd = paymentEntities.stream()
        .map(PaymentEntity::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add).multiply(BigDecimal.valueOf(-1));

    // finally, update referenced flow: increment counters about computed total payments and
    // their total sum, define last update time and change status if needed
    FlowStatusEnum status =
        publishingFlow.getComputedTotPayments() > 0
            ? FlowStatusEnum.INSERTED
            : FlowStatusEnum.CREATED;
    this.paymentRepository.deleteEntityInBulk(paymentEntities);
    this.flowRepository.updateComputedValues(
        publishingFlow.getId(), paymentsToAdd, amountToAdd, now, status);
  }

  private void storeInternalREEvent(
      FlowEntity publishingFlow, FdrStatusEnum status, FdrActionEnum action) {

    MDC.put(MDCKeys.ORGANIZATION_ID, publishingFlow.getReceiverId());
    MDC.put(MDCKeys.FDR_STATUS, status.name());

    boolean canBeWrittenAsREEvent =
        "1".equals(Optional.ofNullable(MDC.get(IS_RE_ENABLED_FOR_THIS_CALL)).orElse("0"));
    if (canBeWrittenAsREEvent) {
      reService.sendEvent(
          ReEvent.builder()
              .serviceIdentifier(AppVersionEnum.FDR003)
              .created(Instant.now())
              .sessionId(MDC.get(MDCKeys.TRX_ID))
              .eventType(EventTypeEnum.INTERNAL)
              .fdrStatus(status)
              .fdr(publishingFlow.getName())
              .pspId(publishingFlow.getSenderId())
              .organizationId(publishingFlow.getReceiverId())
              .revision(publishingFlow.getRevision())
              .fdrAction(action)
              .build());
    }
  }
}
