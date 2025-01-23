package it.gov.pagopa.fdr.controller;

import it.gov.pagopa.fdr.controller.interfaces.annotation.Re;
import it.gov.pagopa.fdr.controller.interfaces.controller.IInternalOrganizationsController;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowResponse;
import it.gov.pagopa.fdr.controller.model.payment.response.PaginatedPaymentsResponse;
import it.gov.pagopa.fdr.service.FlowService;
import it.gov.pagopa.fdr.service.PaymentService;
import it.gov.pagopa.fdr.service.model.arguments.FindFlowsByFiltersArgs;
import it.gov.pagopa.fdr.service.model.re.FdrActionEnum;
import java.time.Instant;

public class InternalOrganizationsController implements IInternalOrganizationsController {

  private final FlowService flowService;

  private final PaymentService paymentService;

  protected InternalOrganizationsController(
      FlowService flowService, PaymentService paymentService) {

    this.flowService = flowService;
    this.paymentService = paymentService;
  }

  @Re(action = FdrActionEnum.INTERNAL_GET_ALL_FDR)
  public PaginatedFlowsResponse getAllPublishedFlowsForInternalUse(
      String organizationId, String pspId, Instant publishedGt, long pageNumber, long pageSize) {

    return this.flowService.getPaginatedPublishedFlowsForCI(
        FindFlowsByFiltersArgs.builder()
            .organizationId(organizationId)
            .pspId(pspId)
            .publishedGt(publishedGt)
            .pageNumber(pageNumber)
            .pageSize(pageSize)
            .build());
  }

  @Re(action = FdrActionEnum.INTERNAL_GET_FDR)
  public SingleFlowResponse getSingleFlowForInternalUse(
      String organizationId, String flowName, Long revision, String pspId) {

    return this.flowService.getSinglePublishedFlow(
        FindFlowsByFiltersArgs.builder()
            .organizationId(organizationId)
            .pspId(pspId)
            .flowName(flowName)
            .revision(revision)
            .build());
  }

  @Re(action = FdrActionEnum.INTERNAL_GET_FDR_PAYMENT)
  public PaginatedPaymentsResponse getFlowPaymentsForInternalUse(
      String organizationId,
      String flowName,
      Long revision,
      String pspId,
      long pageNumber,
      long pageSize) {

    return this.paymentService.getPaymentsFromPublishedFlow(
        FindFlowsByFiltersArgs.builder()
            .organizationId(organizationId)
            .pspId(pspId)
            .flowName(flowName)
            .revision(revision)
            .pageNumber(pageNumber)
            .pageSize(pageSize)
            .build());
  }
}
