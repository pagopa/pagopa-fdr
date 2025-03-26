package it.gov.pagopa.fdr.schedule;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
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
import jakarta.validation.ValidationException;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class HistoryCron {

  private final HistoryBlobStorageService historyBlobStorageService;
  private final FlowToHistoryRepository flowToHistoryRepository;
  private final FlowRepository flowRepository;
  private final PaymentRepository paymentRepository;
  private final FlowBlobMapper flowBlobMapper;

  @ConfigProperty(name = "schedule.history.size")
  Integer size;

  @ConfigProperty(name = "schedule.history.retries")
  Integer maxRetries;

  @Inject
  public HistoryCron(
      FlowToHistoryRepository flowToHistoryRepository,
      FlowRepository flowRepository,
      PaymentRepository paymentRepository,
      HistoryBlobStorageService historyBlobStorageService,
      FlowBlobMapper flowBlobMapper) {
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

    // retrieve the first n flows to historicize
    try {
      PanacheQuery<FlowToHistoryEntity> flows =
          flowToHistoryRepository.findTopNEntitiesOrderByCreated(size, maxRetries);
      for (FlowToHistoryEntity flow : flows.list()) {
        handleFlow(flow);
      }
    } catch (Exception e) {
      throw new AppException(e, AppErrorCodeMessageEnum.ERROR);
    }
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

  private void updateFlowToHistory(FlowToHistoryEntity flowToHistory) {
    flowToHistory.setLastExecution(Instant.now());
    flowToHistory.setRetries(flowToHistory.getRetries() + 1);
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
