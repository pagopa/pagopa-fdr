package it.gov.pagopa.fdr.service;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.controller.model.common.Metadata;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsBySenderAndReceiverResponse;
import it.gov.pagopa.fdr.repository.PaymentRepository;
import it.gov.pagopa.fdr.repository.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.PaymentEntity;
import it.gov.pagopa.fdr.service.middleware.mapper.TechnicalSupportMapper;
import it.gov.pagopa.fdr.service.model.arguments.FindPaymentsByFiltersArgs;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TechnicalSupportService {

  private final PaymentRepository paymentRepository;

  private final TechnicalSupportMapper mapper;

  private final Logger log;

  public TechnicalSupportService(
      TechnicalSupportMapper mapper, PaymentRepository paymentRepository, Logger log) {

    this.mapper = mapper;
    this.paymentRepository = paymentRepository;
    this.log = log;
  }

  @WithSpan(kind = SERVER)
  public PaginatedFlowsBySenderAndReceiverResponse findPaymentsByFilters(
      FindPaymentsByFiltersArgs args) {

    // Extracting field data from argument object
    String pspId = args.getPspId();
    String iuv = args.getIuv();
    String iur = args.getIur();
    String orgDomainId = args.getOrgDomainId();
    int pageNumber = (int) args.getPageNumber();
    int pageSize = (int) args.getPageSize();
    Instant createdFrom = args.getCreatedFrom();
    Instant createdTo = args.getCreatedTo();

    // Executing query with passed fields as filters
    log.debugf(
        "Executing query by: pspId [%s], iuv [%s], iur [%s], createdFrom: [%s], createdTo: [%s]",
        pspId, iuv, iur, createdFrom, createdTo);
    RepositoryPagedResult<PaymentEntity> result =
        paymentRepository.findByPspAndIuvAndIur(
            pspId, iuv, iur, createdFrom, createdTo, orgDomainId, pageNumber, pageSize);
    log.debugf(
        "Found [%s] entities in [%s] pages. Mapping data to final response.",
        result.getTotalElements(), result.getTotalPages());

    // Finally, map found element in the final response
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
