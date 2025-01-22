package it.gov.pagopa.fdr.service;

import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.controller.model.common.response.GenericResponse;
import it.gov.pagopa.fdr.controller.model.payment.Payment;
import it.gov.pagopa.fdr.controller.model.payment.request.AddPaymentRequest;
import it.gov.pagopa.fdr.controller.model.payment.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.controller.model.payment.response.PaginatedPaymentsResponse;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.repository.FdrFlowRepository;
import it.gov.pagopa.fdr.repository.FdrPaymentRepository;
import it.gov.pagopa.fdr.repository.entity.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.flow.FdrFlowEntity;
import it.gov.pagopa.fdr.repository.entity.flow.projection.FdrFlowIdProjection;
import it.gov.pagopa.fdr.repository.entity.payment.FdrPaymentEntity;
import it.gov.pagopa.fdr.repository.enums.FlowStatusEnum;
import it.gov.pagopa.fdr.repository.exception.TransactionRollbackException;
import it.gov.pagopa.fdr.service.middleware.mapper.PaymentMapper;
import it.gov.pagopa.fdr.service.middleware.validator.SemanticValidator;
import it.gov.pagopa.fdr.service.model.FindFlowsByFiltersArgs;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;

@ApplicationScoped
public class PaymentService {

  private Logger log;

  private final Config cachedConfig;

  private final FdrFlowRepository flowRepository;

  private final FdrPaymentRepository paymentRepository;

  private final PaymentMapper paymentMapper;

  public PaymentService(
      Logger log,
      Config cachedConfig,
      FdrFlowRepository flowRepository,
      FdrPaymentRepository paymentRepository,
      PaymentMapper paymentMapper) {

    this.log = log;
    this.cachedConfig = cachedConfig;
    this.flowRepository = flowRepository;
    this.paymentRepository = paymentRepository;
    this.paymentMapper = paymentMapper;
  }

  public PaginatedPaymentsResponse getPaymentsFromPublishedFlow(FindFlowsByFiltersArgs args) {

    /*
    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
    String action = (String) MDC.get(ACTION);
    MDC.put(ORGANIZATION_ID, organizationId);
    MDC.put(FDR, fdr);
    MDC.put(PSP_ID, psp);
     */

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

    FdrFlowIdProjection flowIdProjection =
        this.flowRepository.findIdByOrganizationIdAndPspIdAndNameAndRevision(
            organizationId, pspId, flowName, revision, FlowStatusEnum.PUBLISHED);
    if (flowIdProjection == null) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, flowName);
    }

    RepositoryPagedResult<FdrPaymentEntity> paginatedResult =
        this.paymentRepository.findByFlowObjectId(
            flowIdProjection.getId(), (int) pageNumber, (int) pageSize);

    return paymentMapper.toPaginatedPaymentsResponse(paginatedResult, pageSize, pageNumber);
  }

  public PaginatedPaymentsResponse getPaymentsFromUnpublishedFlow(FindFlowsByFiltersArgs args) {

    /*
    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
    String action = (String) MDC.get(ACTION);
    MDC.put(ORGANIZATION_ID, organizationId);
    MDC.put(FDR, fdr);
    MDC.put(PSP_ID, psp);
     */

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

    FdrFlowIdProjection flowIdProjection =
        this.flowRepository.findUnpublishedIdByPspIdAndNameAndOrganization(
            pspId, flowName, organizationId);
    if (flowIdProjection == null) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, flowName);
    }

    RepositoryPagedResult<FdrPaymentEntity> paginatedResult =
        this.paymentRepository.findByFlowObjectId(
            flowIdProjection.getId(), (int) pageNumber, (int) pageSize);

    return paymentMapper.toPaginatedPaymentsResponse(paginatedResult, pageSize, pageNumber);
  }

  public GenericResponse addPaymentToExistingFlow(
      String pspId, String flowName, AddPaymentRequest request) {

    /*
    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
    String action = (String) MDC.get(ACTION);
    MDC.put(FDR, fdr);
    MDC.put(PSP_ID, pspId);
     */

    log.debugf(
        "Adding [%s] new payments on flow [%s], pspId [%s]",
        request.getPayments().size(), flowName, pspId);

    ConfigDataV1 configData = cachedConfig.getClonedCache();
    SemanticValidator.validateAddPaymentRequest(configData, pspId, flowName, request);

    // check if there is an unpublished flow on which is possible to add payments
    FdrFlowEntity publishingFlow = flowRepository.findUnpublishedByPspIdAndName(pspId, flowName);
    if (publishingFlow == null) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, flowName);
    }

    // check if there is any payment that uses at least one of passed indexes
    List<Payment> paymentsToAdd = request.getPayments();
    Set<Long> indexes = paymentsToAdd.stream().map(Payment::getIndex).collect(Collectors.toSet());
    long numberOfAlreadyUsedIndexes =
        paymentRepository.countByFlowObjectIdAndIndexes(publishingFlow.id, indexes);
    if (numberOfAlreadyUsedIndexes > 0) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_DUPLICATE_INDEX, flowName);
    }

    // create all entities in batch, from each payment to be added, in transactional way
    Instant now = Instant.now();
    List<FdrPaymentEntity> paymentEntities =
        paymentMapper.toEntity(publishingFlow, paymentsToAdd, now);
    addPaymentToExistingFlowInTransaction(publishingFlow, paymentEntities, now);

    /*
    String sessionId = org.slf4j.MDC.get(TRX_ID);
    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
    reService.sendEvent(
        ReInternal.builder()
            .serviceIdentifier(AppVersionEnum.FDR003).created(Instant.now()).sessionId(sessionId)
            .eventType(EventTypeEnum.INTERNAL).fdrPhysicalDelete(false)
            .fdrStatus(it.gov.pagopa.fdr.service.re.model.FdrStatusEnum.INSERTED).flowRead(false)
            .fdr(fdr).pspId(pspId).organizationId(fdrEntity.getReceiver().getOrganizationId())
            .revision(fdrEntity.getRevision()).fdrAction(FdrActionEnum.ADD_PAYMENT).build());
    }
     */

    return GenericResponse.builder()
        .message(String.format("Fdr [%s] payment added", flowName))
        .build();
  }

  public GenericResponse deletePaymentFromExistingFlow(
      String pspId, String flowName, DeletePaymentRequest request) {

    /*
    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
    String action = (String) MDC.get(ACTION);
    MDC.put(FDR, fdr);
    MDC.put(PSP_ID, pspId);
     */

    log.debugf(
        "Deleting [%s] payments on flow [%s], pspId [%s]",
        request.getIndexList().size(), flowName, pspId);

    ConfigDataV1 configData = cachedConfig.getClonedCache();
    SemanticValidator.validateDeletePaymentRequest(configData, pspId, flowName, request);

    // check if there is an unpublished flow on which is possible to add payments
    FdrFlowEntity publishingFlow =
        this.flowRepository.findUnpublishedByPspIdAndName(pspId, flowName);
    if (publishingFlow == null) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, flowName);
    }
    if (publishingFlow.getStatus() != FlowStatusEnum.INSERTED) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION,
          flowName,
          publishingFlow.getStatus());
    }

    // check if each passed index refers to an existing payment
    Set<Long> indexes = new HashSet<>(request.getIndexList());
    List<FdrPaymentEntity> paymentEntities =
        this.paymentRepository.findByFlowObjectIdAndIndexes(publishingFlow.id, indexes);
    boolean containsAllIndexes =
        paymentEntities.stream()
            .map(FdrPaymentEntity::getIndex)
            .collect(Collectors.toSet())
            .containsAll(indexes);
    if (!containsAllIndexes) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_NO_MATCH_INDEX, flowName);
    }

    // create all entities in batch, from each payment to be added, in transactional way
    Instant now = Instant.now();
    deletePaymentToExistingFlowInTransaction(publishingFlow, paymentEntities, now);

    /*
     String sessionId = org.slf4j.MDC.get(TRX_ID);
     MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERNAL.name());
     reService.sendEvent(
         ReInternal.builder()
             .serviceIdentifier(AppVersionEnum.FDR003).created(Instant.now()).sessionId(sessionId)
             .eventType(EventTypeEnum.INTERNAL).fdrPhysicalDelete(false)
             .fdrStatus(
                 FlowStatusEnum.INSERTED == status
                     ? it.gov.pagopa.fdr.service.re.model.FdrStatusEnum.INSERTED
                     : it.gov.pagopa.fdr.service.re.model.FdrStatusEnum.CREATED)
             .flowRead(false).fdr(fdr).pspId(pspId).organizationId(fdrEntity.getReceiver().getOrganizationId())
             .revision(fdrEntity.getRevision()).fdrAction(FdrActionEnum.DELETE_PAYMENT).build());
    */

    return GenericResponse.builder()
        .message(String.format("Fdr [%s] payment deleted", flowName))
        .build();
  }

  private void addPaymentToExistingFlowInTransaction(
      FdrFlowEntity publishingFlow, List<FdrPaymentEntity> paymentEntities, Instant now)
      throws TransactionRollbackException {

    // making a backup of previous data, to be used for rollback operation
    Instant oldUpdateDate = Instant.from(publishingFlow.getUpdated());
    FlowStatusEnum oldStatus = FlowStatusEnum.valueOf(publishingFlow.getStatus().name());

    // generate quantity to add on computed values
    int paymentsToAdd = paymentEntities.size();
    double amountToAdd = paymentEntities.stream().mapToDouble(FdrPaymentEntity::getAmount).sum();

    // finally, update referenced flow: increment counters about computed total payments and
    // their total sum, define last update time and change status if needed
    publishingFlow.addOnComputedTotPayments(paymentsToAdd);
    publishingFlow.addOnComputedTotAmount(amountToAdd);
    publishingFlow.setUpdated(now);
    publishingFlow.setStatus(FlowStatusEnum.INSERTED);
    flowRepository.updateEntity(publishingFlow);

    // try to persist payments using a transaction: if it does not end successfully, it throws an
    // accepted Exception that will cause a compensation operation in order to execute the
    // rollback.
    try {

      paymentRepository.createEntityInTransaction(paymentEntities);

    } catch (TransactionRollbackException e) {

      String pspId = publishingFlow.getSender().getPspId();
      String flowName = publishingFlow.getName();
      compensateFlowChanges(pspId, flowName, paymentsToAdd, amountToAdd, oldUpdateDate, oldStatus);
      throw e;
    }
  }

  private void deletePaymentToExistingFlowInTransaction(
      FdrFlowEntity publishingFlow, List<FdrPaymentEntity> paymentEntities, Instant now)
      throws TransactionRollbackException {

    // making a backup of previous data, to be used for rollback operation
    Instant oldUpdateDate = Instant.from(publishingFlow.getUpdated());
    FlowStatusEnum oldStatus = FlowStatusEnum.valueOf(publishingFlow.getStatus().name());

    // generate quantity to subtract on computed values (evaluated as negative value)
    int paymentsToAdd = -1 * paymentEntities.size();
    double amountToAdd =
        -1 * paymentEntities.stream().mapToDouble(FdrPaymentEntity::getAmount).sum();

    // finally, update referenced flow: increment counters about computed total payments and
    // their total sum, define last update time and change status if needed
    publishingFlow.addOnComputedTotPayments(paymentsToAdd);
    publishingFlow.addOnComputedTotAmount(amountToAdd);
    publishingFlow.setUpdated(now);
    publishingFlow.setStatus(
        publishingFlow.getComputedTotPayments() > 0
            ? FlowStatusEnum.INSERTED
            : FlowStatusEnum.CREATED);
    flowRepository.updateEntity(publishingFlow);

    // try to delete payments using a transaction: if it does not end successfully, it throws an
    // accepted Exception that will cause a compensation operation in order to execute the
    // rollback.
    try {

      paymentRepository.deleteEntityInTransaction(paymentEntities);

    } catch (TransactionRollbackException e) {

      String pspId = publishingFlow.getSender().getPspId();
      String flowName = publishingFlow.getName();
      compensateFlowChanges(pspId, flowName, paymentsToAdd, amountToAdd, oldUpdateDate, oldStatus);
      throw e;
    }
  }

  /**
   * ... it is required to rollback changes: due to impossibility to operate a multi-collection
   * transaction, the 'rollback' on FdrFlowEntity must be executed with a compensation operation, on
   * which the changed fields are subtracted with previously added values
   *
   * @param pspId
   * @param flowName
   * @param paymentsCountAdded
   * @param amountAdded
   * @param oldUpdateDate
   * @param oldStatus
   */
  private void compensateFlowChanges(
      String pspId,
      String flowName,
      int paymentsCountAdded,
      double amountAdded,
      Instant oldUpdateDate,
      FlowStatusEnum oldStatus) {

    // search current value of the flow
    FdrFlowEntity publishingFlowToCompensate =
        flowRepository.findUnpublishedByPspIdAndName(pspId, flowName);

    // update flow computed amounts
    publishingFlowToCompensate.addOnComputedTotPayments(-1 * paymentsCountAdded);
    publishingFlowToCompensate.addOnComputedTotAmount(-1 * amountAdded);

    // update flow status
    if (oldUpdateDate.isBefore(publishingFlowToCompensate.getUpdated())) {
      publishingFlowToCompensate.setUpdated(oldUpdateDate);
      publishingFlowToCompensate.setStatus(oldStatus);
    }

    // finally, update it
    flowRepository.updateEntity(publishingFlowToCompensate);
  }
}
