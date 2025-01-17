package it.gov.pagopa.fdr.service.support;

import static io.opentelemetry.api.trace.SpanKind.SERVER;
import static it.gov.pagopa.fdr.util.MDCKeys.IUR;
import static it.gov.pagopa.fdr.util.MDCKeys.IUV;
import static it.gov.pagopa.fdr.util.MDCKeys.PSP_ID;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import it.gov.pagopa.fdr.repository.entity.payment.FdrPaymentPublishEntity;
import it.gov.pagopa.fdr.service.dto.MetadataDto;
import it.gov.pagopa.fdr.service.dto.PaymentGetByPspIdIuvIurDTO;
import it.gov.pagopa.fdr.service.support.mapper.SupportServiceServiceMapper;
import it.gov.pagopa.fdr.util.AppDBUtil;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

@ApplicationScoped
public class SupportService {

  private final SupportServiceServiceMapper mapper;

  private final Logger log;

  public SupportService(SupportServiceServiceMapper mapper, Logger log) {
    this.mapper = mapper;
    this.log = log;
  }

  @WithSpan(kind = SERVER)
  public PaymentGetByPspIdIuvIurDTO findPaymentsByPspIdAndIuvIur(
      FindPaymentsByPspIdAndIuvIurArgs args) {
    MDC.put(PSP_ID, args.getPspId());
    Optional.ofNullable(args.getIuv()).ifPresent(iuvz -> MDC.put(IUV, iuvz));
    Optional.ofNullable(args.getIur()).ifPresent(iurz -> MDC.put(IUR, iurz));
    log.infof(
        AppMessageUtil.logProcess("%s with id:[%s] - page:[%s], pageSize:[%s]"),
        args.getAction(),
        args.getPspId(),
        args.getPageNumber(),
        args.getPageSize());

    Page page = Page.of((int) args.getPageNumber() - 1, (int) args.getPageSize());
    Sort sort = AppDBUtil.getSort(List.of("index,asc"));

    log.debugf(
        "Existence check fdr by pspId[%s], iuv[%s], iur[%s], createdFrom: [%s], createdTo: [%s]",
        args.getPspId(), args.getIuv(), args.getIur(), args.getCreatedFrom(), args.getCreatedTo());
    PanacheQuery<FdrPaymentPublishEntity> fdrPaymentPublishPanacheQuery =
        FdrPaymentPublishEntity.findByPspAndIuvIur(
                args.getPspId(),
                args.getIuv(),
                args.getIur(),
                args.getCreatedFrom(),
                args.getCreatedTo(),
                sort)
            .page(page);

    List<FdrPaymentPublishEntity> list = fdrPaymentPublishPanacheQuery.list();

    long totPage = fdrPaymentPublishPanacheQuery.pageCount();
    long countReportingFlowPayment = fdrPaymentPublishPanacheQuery.count();

    log.debug("Mapping PaymentGetByPspIdIuvIurDTO from FdrPaymentPublishEntity");
    return PaymentGetByPspIdIuvIurDTO.builder()
        .metadata(
            MetadataDto.builder()
                .pageSize(args.getPageSize())
                .pageNumber(args.getPageNumber())
                .totPage(totPage)
                .build())
        .count(countReportingFlowPayment)
        .data(mapper.toPaymentByPspIdIuvIurList(list))
        .build();
  }
}
