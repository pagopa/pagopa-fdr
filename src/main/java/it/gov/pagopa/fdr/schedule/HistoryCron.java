package it.gov.pagopa.fdr.schedule;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.runtime.configuration.DurationConverter;
import io.quarkus.scheduler.Scheduled;
import it.gov.pagopa.fdr.controller.model.flow.Receiver;
import it.gov.pagopa.fdr.controller.model.flow.Sender;
import it.gov.pagopa.fdr.controller.model.flow.enums.SenderTypeEnum;
import it.gov.pagopa.fdr.repository.FlowRepository;
import it.gov.pagopa.fdr.repository.FlowToHistoryRepository;
import it.gov.pagopa.fdr.repository.PaymentRepository;
import it.gov.pagopa.fdr.repository.entity.FlowEntity;
import it.gov.pagopa.fdr.repository.entity.FlowToHistoryEntity;
import it.gov.pagopa.fdr.repository.entity.PaymentEntity;
import it.gov.pagopa.fdr.storage.HistoryBlobStorageService;
import it.gov.pagopa.fdr.storage.model.FlowBlob;
import it.gov.pagopa.fdr.storage.model.PaymentBlob;
import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.error.exception.common.AppException;
import it.gov.pagopa.fdr.util.error.exception.common.ScheduleException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class HistoryCron {
  private final FlowToHistoryRepository flowToHistoryRepository;
  private final FlowRepository flowRepository;
  private final PaymentRepository paymentRepository;
  private final HistoryBlobStorageService historyBlobStorageService;

  @ConfigProperty(name = "schedule.history.size")
  Integer size;

  @ConfigProperty(name = "schedule.history.retries")
  Integer maxRetries;

  @ConfigProperty(name = "schedule.history.every")
  String every;

  @Inject
  public HistoryCron(
      FlowToHistoryRepository flowToHistoryRepository,
      FlowRepository flowRepository,
      PaymentRepository paymentRepository,
      HistoryBlobStorageService historyBlobStorageService) {
    this.flowToHistoryRepository = flowToHistoryRepository;
    this.flowRepository = flowRepository;
    this.paymentRepository = paymentRepository;
    this.historyBlobStorageService = historyBlobStorageService;
  }

  /**
   * Scheduled task to process flows for historicization every hour.
   *
   * <p>This method retrieves the top n flows that have never been started, ordered by their
   * creation date, and processes each flow by invoking the {@link #handleFlow(FlowToHistoryEntity)}
   * method.
   *
   * <p>Each flow retrieved is intended to be marked for historicization and further handled as per
   * the logic defined in {@link #handleFlow(FlowToHistoryEntity)}.
   */
  @Scheduled(every = "${schedule.history.every}")
  void execute() {

    // retrieve the first n flows to historicize
    try {
      PanacheQuery<FlowToHistoryEntity> flows = findAndLockFlowToHistory();
      for (var flow : flows.list()) {
        handleFlow(flow);
      }
    } catch (Exception e) {
      throw new AppException(e, AppErrorCodeMessageEnum.ERROR);
    }
  }

  /**
   * Finds and locks the first n flows that have never been started, ordered by their creation date.
   *
   * <p>This method retrieves the top n flows that have never been started, ordered by their
   * creation date, and locks them with a lock until date set to the current time plus the duration
   * defined by the {@code every} property.
   *
   * @return the PanacheQuery containing the locked flows
   */
  @Transactional
  public PanacheQuery<FlowToHistoryEntity> findAndLockFlowToHistory() {
    // retrieve the first n flows to historicize
    PanacheQuery<FlowToHistoryEntity> flows =
        flowToHistoryRepository.findTopNEntitiesOrderByCreated(size, maxRetries);

    // lock the flows with a lock until date set to the current time plus the duration defined
    // by the every property
    flows.stream()
        .forEach(
            flowToHistory -> {
              var duration = DurationConverter.parseDuration(every);
              long secondsToAdd = 30L * flows.list().size(); // add padding of 30 seconds per flow
              duration = duration.plusSeconds(secondsToAdd);
              flowToHistory.setLockUntil(Instant.now().plus(duration));
              flowToHistoryRepository.persist(flowToHistory);
            });

    return flows;
  }

  /**
   * Process a flow and related payments for history cron.
   *
   * <p>This method retrieves the payments associated with the given flow and processes them. If an
   * exception is thrown during the processing, the flow retry count is add by 1 and the exception
   * is re-thrown.
   *
   * @param flowToHistory the flow to process
   */
  private void handleFlow(FlowToHistoryEntity flowToHistory)
      throws ValidationException, IOException {
    try {
      var flow =
          flowRepository
              .findByPspIdAndNameAndRevision(
                  flowToHistory.getPspId(), flowToHistory.getName(), flowToHistory.getRevision())
              .firstResultOptional()
              .orElseThrow(
                  () ->
                      new AppException(
                          AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND,
                          flowToHistory.getName()));

      var payments = handlePage(flow.getId());

      var flowBlob = mapToFlowBlob(flow, payments);
      historyBlobStorageService.saveJsonFile(flowBlob);

      flowToHistoryRepository.deleteByIdTransactional(flowToHistory.getId());

    } catch (Exception e) {
      updateFlowToHistory(flowToHistory);
      if (flowToHistory.getRetries() >= maxRetries) {
        throw new ScheduleException("MAX RETRIES REACHED: " + maxRetries, e);
      }
      throw e;
    }
  }

  private static FlowBlob mapToFlowBlob(FlowEntity flow, List<PaymentBlob> payments) {
    return FlowBlob.builder()
        .fdr(flow.name)
        .fdrDate(flow.date)
        .revision(flow.revision)
        .created(flow.created)
        .published(flow.published)
        .updated(flow.updated)
        .status(flow.status)
        .sender(
            Sender.builder()
                .type(SenderTypeEnum.valueOf(flow.senderType))
                .id(flow.senderId)
                .pspId(flow.pspDomainId)
                .pspName(flow.senderPspName)
                .pspBrokerId(flow.senderPspBrokerId)
                .channelId(flow.senderChannelId)
                .password(flow.senderPassword)
                .build())
        .receiver(
            Receiver.builder()
                .id(flow.receiverId)
                .organizationId(flow.orgDomainId)
                .organizationName(flow.receiverOrganizationName)
                .build())
        .regulation(flow.regulation)
        .regulationDate(flow.regulationDate.toString())
        .bicCodePouringBank(flow.bicCodePouringBank)
        .computedTotPayments(flow.computedTotPayments)
        .computedSumPayments(flow.computedTotAmount)
        .payments(payments)
        .build();
  }

  private void updateFlowToHistory(FlowToHistoryEntity flowToHistory) {
    flowToHistory.setLastExecution(Instant.now());
    flowToHistory.setRetries(flowToHistory.getRetries() + 1);
    flowToHistory.setLockUntil(null);
    flowToHistoryRepository.persist(flowToHistory);
  }

  private List<PaymentBlob> handlePage(Long flowId) {
    int page = 0;
    List<PaymentBlob> result = new ArrayList<>();
    PanacheQuery<PaymentEntity> payments;
    do {
      payments = paymentRepository.findPageByFlowId(flowId, page, 100);
      var paymentsBlob = payments.stream().map(HistoryCron::mapPaymentBlob).toList();
      result.addAll(paymentsBlob);
      page++;
    } while (payments.hasNextPage());
    return result;
  }

  private static PaymentBlob mapPaymentBlob(PaymentEntity elem) {
    return PaymentBlob.builder()
        .index(elem.getIndex())
        .iur(elem.getIur())
        .iuv(elem.getIuv())
        .idTransfer(elem.getTransferId())
        .pay(elem.getAmount())
        .payDate(elem.getPayDate().toString())
        .payStatus(elem.getPayStatus())
        .build();
  }
}
