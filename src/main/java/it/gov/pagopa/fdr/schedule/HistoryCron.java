package it.gov.pagopa.fdr.schedule;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.runtime.configuration.DurationConverter;
import io.quarkus.scheduler.Scheduled;
import it.gov.pagopa.fdr.repository.FlowRepository;
import it.gov.pagopa.fdr.repository.FlowToHistoryRepository;
import it.gov.pagopa.fdr.repository.PaymentRepository;
import it.gov.pagopa.fdr.repository.entity.FlowEntity;
import it.gov.pagopa.fdr.repository.entity.FlowToHistoryEntity;
import it.gov.pagopa.fdr.repository.entity.PaymentEntity;
import it.gov.pagopa.fdr.storage.HistoryBlobStorageService;
import it.gov.pagopa.fdr.storage.middleware.FlowBlobMapper;
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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class HistoryCron {

  private final Logger log;
  private final HistoryBlobStorageService historyBlobStorageService;
  private final FlowToHistoryRepository flowToHistoryRepository;
  private final FlowRepository flowRepository;
  private final PaymentRepository paymentRepository;
  private final FlowBlobMapper flowBlobMapper;

  @ConfigProperty(name = "schedule.history.size")
  Integer size;

  @ConfigProperty(name = "schedule.history.retries")
  Integer maxRetries;

  @ConfigProperty(name = "schedule.history.lock-duration")
  String lockDuration;

  @Inject
  public HistoryCron(
      Logger log,
      FlowToHistoryRepository flowToHistoryRepository,
      FlowRepository flowRepository,
      PaymentRepository paymentRepository,
      HistoryBlobStorageService historyBlobStorageService,
      FlowBlobMapper flowBlobMapper) {

    this.log = log;
    this.flowToHistoryRepository = flowToHistoryRepository;
    this.flowRepository = flowRepository;
    this.paymentRepository = paymentRepository;
    this.historyBlobStorageService = historyBlobStorageService;
    this.flowBlobMapper = flowBlobMapper;
  }

  /**
   * Scheduled task to process flows for historicization every scheduled time.
   *
   * <p>This method retrieves the top n flows that have never been started, ordered by their
   * creation date, and processes each flow by invoking the {@link #handleFlow(FlowToHistoryEntity)}
   * method.
   *
   * <p>Each flow retrieved is intended to be marked for historicization and further handled as per
   * the logic defined in {@link #handleFlow(FlowToHistoryEntity)}.
   */
  @Scheduled(cron = "${schedule.history.cron}")
  void execute() {

    long startTime = System.currentTimeMillis();
    // retrieve the first n flows to historicize
    try {
      log.infof("Starting execution of historicization job. Analyzing [%s] elements.", size);
      PanacheQuery<FlowToHistoryEntity> flows = findAndLockFlowToHistory();
      log.debugf("Found execution of historicization job. Analyzing [%s] elements.", size);
      for (FlowToHistoryEntity flow : flows.list()) {
        handleFlow(flow);
      }
    } catch (Exception e) {
      throw new AppException(e, AppErrorCodeMessageEnum.ERROR);
    } finally {
      log.infof(
          "Ended execution of historicization job. Elapsed time [%d] ms.",
          System.currentTimeMillis() - startTime);
    }
  }

  /**
   * Finds and locks the first n flows to be processed, ordered by their creation date.
   *
   * <p>This method retrieves the top n flows that have never been started, ordered by their
   * creation date, and locks them with a lock until date set to the duration defined by the {@code
   * every} property plus some padding.
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
              Duration duration = DurationConverter.parseDuration(lockDuration);
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
      FlowEntity flow =
          flowRepository
              .findByPspIdAndNameAndRevision(
                  flowToHistory.getPspId(), flowToHistory.getName(), flowToHistory.getRevision())
              .firstResultOptional()
              .orElseThrow(
                  () ->
                      new AppException(
                          AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND,
                          flowToHistory.getName()));

      List<PaymentBlob> payments = handlePage(flow.getId());

      FlowBlob flowBlob = flowBlobMapper.toFlowBlob(flow, payments);
      historyBlobStorageService.saveJsonFile(flowBlob, flowToHistory);

      flowToHistoryRepository.deleteByIdTransactional(flowToHistory.getId());

    } catch (Exception e) {
      updateFlowToHistory(flowToHistory);
      if (flowToHistory.getRetries() >= maxRetries) {
        throw new ScheduleException("MAX RETRIES REACHED: " + maxRetries, e);
      }
      throw e;
    }
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
      List<PaymentBlob> paymentsBlob =
          payments.stream().map(flowBlobMapper::toPaymentBlob).toList();
      result.addAll(paymentsBlob);
      page++;
    } while (payments.hasNextPage());
    return result;
  }
}
