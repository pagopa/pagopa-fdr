package it.gov.pagopa.fdr.controller;

import it.gov.pagopa.fdr.controller.interfaces.IOrganizationsController;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowResponse;
import it.gov.pagopa.fdr.controller.model.payment.response.PaginatedPaymentsResponse;
import it.gov.pagopa.fdr.service.FlowService;
import it.gov.pagopa.fdr.service.PaymentService;
import it.gov.pagopa.fdr.service.model.arguments.FindFlowsByFiltersArgs;
import it.gov.pagopa.fdr.service.model.re.FdrActionEnum;
import it.gov.pagopa.fdr.util.Re;
import java.time.Instant;

public class OrganizationsController implements IOrganizationsController {

  private final FlowService flowService;

  private final PaymentService paymentService;

  protected OrganizationsController(FlowService flowService, PaymentService paymentService) {

    this.flowService = flowService;
    this.paymentService = paymentService;
  }

  @Override
  @Re(action = FdrActionEnum.GET_ALL_FDR)
  public PaginatedFlowsResponse getAllPublishedFlows(
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

  @Override
  @Re(action = FdrActionEnum.GET_FDR)
  public SingleFlowResponse getSinglePublishedFlow(
      String organizationId, String flowName, Long revision, String pspId) {

    return this.flowService.getSinglePublishedFlow(
        FindFlowsByFiltersArgs.builder()
            .organizationId(organizationId)
            .pspId(pspId)
            .flowName(flowName)
            .revision(revision)
            .build());
  }

  @Override
  @Re(action = FdrActionEnum.GET_FDR_PAYMENT)
  public PaginatedPaymentsResponse getPaymentsFromPublishedFlow(
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
