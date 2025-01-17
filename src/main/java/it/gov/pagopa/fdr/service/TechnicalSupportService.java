package it.gov.pagopa.fdr.service;

import static io.opentelemetry.api.trace.SpanKind.SERVER;
import static it.gov.pagopa.fdr.util.MDCKeys.IUR;
import static it.gov.pagopa.fdr.util.MDCKeys.IUV;
import static it.gov.pagopa.fdr.util.MDCKeys.PSP_ID;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import it.gov.pagopa.fdr.controller.model.common.Metadata;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsBySenderAndReceiverResponse;
import it.gov.pagopa.fdr.repository.entity.payment.FdrPaymentPublishEntity;
import it.gov.pagopa.fdr.service.middleware.mapper.TechnicalSupportMapper;
import it.gov.pagopa.fdr.service.model.FindPaymentsByFiltersArgs;
import it.gov.pagopa.fdr.util.AppDBUtil;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

@ApplicationScoped
public class TechnicalSupportService {

  private final TechnicalSupportMapper mapper;

  private final Logger log;

  public TechnicalSupportService(TechnicalSupportMapper mapper, Logger log) {
    this.mapper = mapper;
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

    Page page = Page.of(pageNumber - 1, pageSize);
    Sort sort = AppDBUtil.getSort(List.of("index,asc"));

    log.debugf(
        "Existence check fdr by pspId[%s], iuv[%s], iur[%s], createdFrom: [%s], createdTo: [%s]",
        pspId, iuv, iur, createdFrom, createdTo);
    PanacheQuery<FdrPaymentPublishEntity> fdrPaymentPublishPanacheQuery =
        FdrPaymentPublishEntity.findByPspAndIuvIur(pspId, iuv, iur, createdFrom, createdTo, sort)
            .page(page);

    List<FdrPaymentPublishEntity> list = fdrPaymentPublishPanacheQuery.list();

    log.debug("Mapping PaymentGetByPspIdIuvIurDTO from FdrPaymentPublishEntity");
    return PaginatedFlowsBySenderAndReceiverResponse.builder()
        .metadata(
            Metadata.builder()
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .totPage(fdrPaymentPublishPanacheQuery.pageCount())
                .build())
        .count(fdrPaymentPublishPanacheQuery.count())
        .data(mapper.toFlowBySenderAndReceiver(list))
        .build();
  }
}
