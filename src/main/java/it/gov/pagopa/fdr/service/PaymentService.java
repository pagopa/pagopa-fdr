package it.gov.pagopa.fdr.service;

import static io.opentelemetry.api.trace.SpanKind.SERVER;
import static it.gov.pagopa.fdr.util.constant.MDCKeys.IS_RE_ENABLED_FOR_THIS_CALL;

import io.micrometer.core.annotation.Timed;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.controller.model.common.response.GenericResponse;
import it.gov.pagopa.fdr.controller.model.payment.Payment;
import it.gov.pagopa.fdr.controller.model.payment.request.AddPaymentRequest;
import it.gov.pagopa.fdr.controller.model.payment.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.controller.model.payment.response.PaginatedPaymentsResponse;
import it.gov.pagopa.fdr.repository.FlowRepository;
import it.gov.pagopa.fdr.repository.PaymentRepository;
import it.gov.pagopa.fdr.repository.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.FlowEntity;
import it.gov.pagopa.fdr.repository.entity.PaymentEntity;
import it.gov.pagopa.fdr.repository.enums.FlowStatusEnum;
import it.gov.pagopa.fdr.service.middleware.mapper.PaymentMapper;
import it.gov.pagopa.fdr.service.middleware.validator.SemanticValidator;
import it.gov.pagopa.fdr.service.model.arguments.FindFlowsByFiltersArgs;
import it.gov.pagopa.fdr.service.model.re.*;
import it.gov.pagopa.fdr.util.constant.MDCKeys;
import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.error.exception.common.AppException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;
import org.slf4j.MDC;

@ApplicationScoped
public class PaymentService {

  private final Logger log;

  private final ReService reService;

  private final Config cachedConfig;

  private final FlowRepository flowRepository;

  private final PaymentRepository paymentRepository;

  private final PaymentMapper paymentMapper;

  public PaymentService(
      Logger log,
      Config cachedConfig,
      FlowRepository flowRepository,
      PaymentRepository paymentRepository,
      ReService reService,
      PaymentMapper paymentMapper) {

    this.log = log;
    this.cachedConfig = cachedConfig;
    this.flowRepository = flowRepository;
    this.paymentRepository = paymentRepository;
    this.reService = reService;
    this.paymentMapper = paymentMapper;
  }

  @WithSpan(kind = SERVER)
  public PaginatedPaymentsResponse getPaymentsFromPublishedFlow(FindFlowsByFiltersArgs args) {

    String organizationId = args.getOrganizationId();
    String pspId = args.getPspId();
    String flowName = args.getFlowName();
    long revision = args.getRevision();
    long pageNumber = args.getPageNumber();
    long pageSize = args.getPageSize();

    log.debugf(
        "Executing query on payments related on published flow by organizationId [%s], pspId [%s],"
            + " flowName [%s], revision:[%s]",
        organizationId, pspId, flowName, revision);

    ConfigDataV1 configData = cachedConfig.getClonedCache();
    SemanticValidator.validateGetSingleFlowFilters(configData, args);

    Optional<Long> optFlowId =
        this.flowRepository.findIdByOrganizationIdAndPspIdAndNameAndRevision(
            organizationId, pspId, flowName, revision, FlowStatusEnum.PUBLISHED);
    if (optFlowId.isEmpty()) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, flowName);
    }

    Long flowId = optFlowId.get();
    RepositoryPagedResult<PaymentEntity> paginatedResult =
        this.paymentRepository.findByFlowId(flowId, (int) pageNumber, (int) pageSize);

    return paymentMapper.toPaginatedPaymentsResponse(paginatedResult, pageSize, pageNumber);
  }

  @WithSpan(kind = SERVER)
  public PaginatedPaymentsResponse getPaymentsFromUnpublishedFlow(FindFlowsByFiltersArgs args) {

    String organizationId = args.getOrganizationId();
    String pspId = args.getPspId();
    String flowName = args.getFlowName();
    long pageNumber = args.getPageNumber();
    long pageSize = args.getPageSize();

    log.debugf(
        "Executing query on payments related on unpublished flow by organizationId [%s], pspId"
            + " [%s], flowName [%s]",
        organizationId, pspId, flowName);

    ConfigDataV1 configData = cachedConfig.getClonedCache();
    SemanticValidator.validateGetSingleFlowFilters(configData, args);

    Optional<Long> optFlowId =
        this.flowRepository.findUnpublishedIdByPspIdAndNameAndOrganization(
            pspId, flowName, organizationId);
    if (optFlowId.isEmpty()) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, flowName);
    }

    RepositoryPagedResult<PaymentEntity> paginatedResult =
        this.paymentRepository.findByFlowId(optFlowId.get(), (int) pageNumber, (int) pageSize);

    return paymentMapper.toPaginatedPaymentsResponse(paginatedResult, pageSize, pageNumber);
  }

  @WithSpan(kind = SERVER)
  @Timed(value = "paymentService.addPaymentToExistingFlow.task", description = "Time taken to perform addPaymentToExistingFlow", percentiles = 0.95, histogram = true)
  public GenericResponse addPaymentToExistingFlow(String pspId, String flowName, AddPaymentRequest request) {

    log.debugf(
        "Adding [%s] new payments on flow [%s], pspId [%s]",
        request.getPayments().size(), flowName, pspId
    );

    ConfigDataV1 configData = cachedConfig.getClonedCache();
    SemanticValidator.validateAddPaymentRequest(configData, pspId, flowName, request);

    return addPaymentsToUnpublishedFlow(pspId, flowName, request);
  }

  @WithSpan(kind = SERVER)
  public GenericResponse deletePaymentFromExistingFlow(
      String pspId, String flowName, DeletePaymentRequest request) {

    log.debugf(
        "Deleting [%s] payments on flow [%s], pspId [%s]",
        request.getIndexList().size(), flowName, pspId);

    ConfigDataV1 configData = cachedConfig.getClonedCache();
    SemanticValidator.validateDeletePaymentRequest(configData, pspId, flowName, request);

    return deletePaymentFromUnpublishedFlow(pspId, flowName, request);
  }

  @Transactional(rollbackOn = Exception.class)
  public GenericResponse addPaymentsToUnpublishedFlow(String pspId, String flowName, AddPaymentRequest request) {

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

  @Transactional(rollbackOn = Exception.class)
  public GenericResponse deletePaymentFromUnpublishedFlow(
      String pspId, String flowName, DeletePaymentRequest request) {

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

  private void storeInternalREEvent(
      FlowEntity publishingFlow, FdrStatusEnum status, FdrActionEnum action) {

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

  @SneakyThrows
  private void addPaymentToExistingFlowInTransaction(FlowEntity publishingFlow, List<PaymentEntity> paymentEntities, Instant now) {

    long paymentsToAdd = paymentEntities.size();

    BigDecimal amountToAdd = paymentEntities.stream()
              .map(PaymentEntity::getAmount)
              .reduce(BigDecimal.ZERO, BigDecimal::add);

//    this.paymentRepository.createEntityInBulk(paymentEntities);
    this.paymentRepository.createEntityInBulkCopy(paymentEntities);

    flowRepository.updateComputedValues(
            publishingFlow.getId(),
            paymentsToAdd,
            amountToAdd,
            now,
            FlowStatusEnum.INSERTED
    );
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
}
