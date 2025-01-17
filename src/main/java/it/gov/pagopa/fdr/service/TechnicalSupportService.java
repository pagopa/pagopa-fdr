package it.gov.pagopa.fdr.service;

import static io.opentelemetry.api.trace.SpanKind.SERVER;
import static it.gov.pagopa.fdr.util.MDCKeys.IUR;
import static it.gov.pagopa.fdr.util.MDCKeys.IUV;
import static it.gov.pagopa.fdr.util.MDCKeys.PSP_ID;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.controller.model.common.Metadata;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsBySenderAndReceiverResponse;
import it.gov.pagopa.fdr.repository.FdrPaymentRepository;
import it.gov.pagopa.fdr.repository.entity.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.payment.FdrPaymentEntity;
import it.gov.pagopa.fdr.service.middleware.mapper.TechnicalSupportMapper;
import it.gov.pagopa.fdr.service.model.FindPaymentsByFiltersArgs;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.Optional;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

@ApplicationScoped
public class TechnicalSupportService {

  private final FdrPaymentRepository paymentRepository;

  private final TechnicalSupportMapper mapper;

  private final Logger log;

  public TechnicalSupportService(
      TechnicalSupportMapper mapper, FdrPaymentRepository paymentRepository, Logger log) {
    this.mapper = mapper;
    this.paymentRepository = paymentRepository;
    this.log = log;
  }

  @WithSpan(kind = SERVER)
  public PaginatedFlowsBySenderAndReceiverResponse findPaymentsByFilters(
      FindPaymentsByFiltersArgs args) {

    String pspId = args.getPspId();
    String iuv = args.getIuv();
    String iur = args.getIur();
    int pageNumber = (int) args.getPageNumber();
    int pageSize = (int) args.getPageSize();
    Instant createdFrom = args.getCreatedFrom();
    Instant createdTo = args.getCreatedTo();

    MDC.put(PSP_ID, pspId);
    Optional.ofNullable(iuv).ifPresent(value -> MDC.put(IUV, value));
    Optional.ofNullable(iur).ifPresent(value -> MDC.put(IUR, value));

    log.debugf(
        "Existence check fdr by pspId[%s], iuv[%s], iur[%s], createdFrom: [%s], createdTo: [%s]",
        pspId, iuv, iur, createdFrom, createdTo);
    RepositoryPagedResult<FdrPaymentEntity> result =
        paymentRepository.executeQueryByPspAndIuvAndIur(
            pspId, iuv, iur, createdFrom, createdTo, pageNumber, pageSize);

    log.debug("Mapping PaymentGetByPspIdIuvIurDTO from FdrPaymentPublishEntity");
    return PaginatedFlowsBySenderAndReceiverResponse.builder()
        .metadata(
            Metadata.builder()
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .totPage(result.getTotalPages())
                .build())
        .count(result.getTotalElements())
        .data(mapper.toFlowBySenderAndReceiver(result.getData()))
        .build();
  }
}
