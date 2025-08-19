package it.gov.pagopa.fdr.controller;

import it.gov.pagopa.fdr.controller.interfaces.controller.ISupportController;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsBySenderAndReceiverResponse;
import it.gov.pagopa.fdr.service.TechnicalSupportService;
import it.gov.pagopa.fdr.service.model.arguments.FindPaymentsByFiltersArgs;
import java.time.Instant;

public class TechnicalSupportController implements ISupportController {

  private final TechnicalSupportService service;

  public TechnicalSupportController(TechnicalSupportService service) {
    this.service = service;
  }

  @Override
  public PaginatedFlowsBySenderAndReceiverResponse getByIuv(
      String pspId,
      String iuv,
      Instant createdFrom,
      Instant createdTo,
      String orgDomainId,
      long pageNumber,
      long pageSize) {

    return service.findPaymentsByFilters(
        FindPaymentsByFiltersArgs.builder()
            .pspId(pspId)
            .iuv(iuv)
            .iur(null)
            .orgDomainId(orgDomainId)
            .createdFrom(createdFrom)
            .createdTo(createdTo)
            .pageNumber(pageNumber)
            .pageSize(pageSize)
            .build());
  }

  @Override
  public PaginatedFlowsBySenderAndReceiverResponse getByIur(
      String pspId,
      String iur,
      Instant createdFrom,
      Instant createdTo,
      long pageNumber,
      long pageSize) {

    return service.findPaymentsByFilters(
        FindPaymentsByFiltersArgs.builder()
            .pspId(pspId)
            .iuv(null)
            .iur(iur)
            .createdFrom(createdFrom)
            .createdTo(createdTo)
            .pageNumber(pageNumber)
            .pageSize(pageSize)
            .build());
  }
}
