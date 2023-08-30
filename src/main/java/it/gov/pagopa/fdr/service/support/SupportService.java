package it.gov.pagopa.fdr.service.support;

import static io.opentelemetry.api.trace.SpanKind.SERVER;
import static it.gov.pagopa.fdr.util.MDCKeys.IUR;
import static it.gov.pagopa.fdr.util.MDCKeys.IUV;
import static it.gov.pagopa.fdr.util.MDCKeys.PSP_ID;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentPublishEntity;
import it.gov.pagopa.fdr.service.dto.MetadataDto;
import it.gov.pagopa.fdr.service.dto.PaymentGetByPspIdIurDTO;
import it.gov.pagopa.fdr.service.dto.PaymentGetByPspIdIuvDTO;
import it.gov.pagopa.fdr.service.support.mapper.SupportServiceServiceMapper;
import it.gov.pagopa.fdr.util.AppDBUtil;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.List;
import org.jboss.logging.Logger;
import org.slf4j.MDC;

@ApplicationScoped
public class SupportService{
  @Inject
  Config config;

  @Inject
  SupportServiceServiceMapper mapper;

  @Inject
  Logger log;

  @WithSpan(kind = SERVER)
  public PaymentGetByPspIdIuvDTO findPaymentsByPspIdAndIuv(String action, String pspId, String iuv, Instant createdGt, long pageNumber, long pageSize) {
    MDC.put(PSP_ID, pspId);
    MDC.put(IUV, iuv);
    log.infof(
        AppMessageUtil.logProcess("%s with id:[%s] - page:[%s], pageSize:[%s]"),
        action,
        pspId,
        pageNumber,
        pageSize);

    Page page = Page.of((int) pageNumber - 1, (int) pageSize);
    Sort sort = AppDBUtil.getSort(List.of("index,asc"));

    log.debugf("Existence check fdr by pspId[%s], iuv[%s]", pspId, iuv);
    PanacheQuery<FdrPaymentPublishEntity> fdrPaymentPublishPanacheQuery =
        FdrPaymentPublishEntity.findByPspAndIuv(pspId, iuv, createdGt, sort).page(page);

    List<FdrPaymentPublishEntity> list = fdrPaymentPublishPanacheQuery.list();

    long totPage = fdrPaymentPublishPanacheQuery.pageCount();
    long countReportingFlowPayment = fdrPaymentPublishPanacheQuery.count();

    log.debug("Mapping PaymentGetByPspIdIuvDTO from FdrPaymentPublishEntity");
    return PaymentGetByPspIdIuvDTO.builder()
        .metadata(
            MetadataDto.builder()
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .totPage(totPage)
                .build())
        .count(countReportingFlowPayment)
        .data(mapper.toPaymentByPspIdIuvList(list))
        .build();
  }

  @WithSpan(kind = SERVER)
  public PaymentGetByPspIdIurDTO findPaymentsPspIdAndIur(String action, String pspId, String iur, Instant createdGt, long pageNumber, long pageSize) {
    MDC.put(PSP_ID, pspId);
    MDC.put(IUR, iur);
    log.infof(
        AppMessageUtil.logProcess("%s with id:[%s] - page:[%s], pageSize:[%s]"),
        action,
        pspId,
        pageNumber,
        pageSize);

    Page page = Page.of((int) pageNumber - 1, (int) pageSize);
    Sort sort = AppDBUtil.getSort(List.of("index,asc"));


    log.debugf("Existence check fdr by pspId[%s], iur[%s]", pspId, iur);
    PanacheQuery<FdrPaymentPublishEntity> fdrPaymentPublishPanacheQuery =
        FdrPaymentPublishEntity.findByPspAndIur(pspId, iur, createdGt, sort).page(page);

    List<FdrPaymentPublishEntity> list = fdrPaymentPublishPanacheQuery.list();

    long totPage = fdrPaymentPublishPanacheQuery.pageCount();
    long countReportingFlowPayment = fdrPaymentPublishPanacheQuery.count();

    log.debug("Mapping PaymentGetByPspIdIurDTO from FdrPaymentPublishEntity");
    return PaymentGetByPspIdIurDTO.builder()
        .metadata(
            MetadataDto.builder()
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .totPage(totPage)
                .build())
        .count(countReportingFlowPayment)
        .data(mapper.toPaymentByPspIdIurList(list))
        .build();
  }
}