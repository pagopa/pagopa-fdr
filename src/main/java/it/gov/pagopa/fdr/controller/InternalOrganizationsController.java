package it.gov.pagopa.fdr.controller;

import it.gov.pagopa.fdr.controller.interfaces.IInternalOrganizationsController;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowResponse;
import it.gov.pagopa.fdr.controller.model.payment.response.PaginatedPaymentsResponse;
import it.gov.pagopa.fdr.service.FlowService;
import it.gov.pagopa.fdr.service.model.FindFlowsByFiltersArgs;
import it.gov.pagopa.fdr.service.re.model.FdrActionEnum;
import it.gov.pagopa.fdr.util.Re;
import java.time.Instant;

public class InternalOrganizationsController implements IInternalOrganizationsController {

  private final FlowService flowService;

  protected InternalOrganizationsController(FlowService flowService) {

    this.flowService = flowService;
  }

  @Re(action = FdrActionEnum.INTERNAL_GET_ALL_FDR)
  public PaginatedFlowsResponse getAllPublishedFlowsForInternalUse(
      String organizationId, String pspId, Instant publishedGt, long pageNumber, long pageSize) {

    return this.flowService.getPaginatedPublishedFlows(
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

    return this.flowService.getPaymentsFromPublishedFlow(
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
