package it.gov.pagopa.fdr.controller;

import it.gov.pagopa.fdr.controller.interfaces.annotation.Re;
import it.gov.pagopa.fdr.controller.interfaces.controller.IInternalOrganizationsOperationsController;
import it.gov.pagopa.fdr.controller.model.common.response.GenericResponse;
import it.gov.pagopa.fdr.controller.model.flow.request.CreateFlowRequest;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowCreatedResponse;
import it.gov.pagopa.fdr.controller.model.payment.request.InternalAddPaymentRequest;
import it.gov.pagopa.fdr.controller.model.payment.request.InternalDeletePaymentRequest;
import it.gov.pagopa.fdr.service.FlowService;
import it.gov.pagopa.fdr.service.PaymentService;
import it.gov.pagopa.fdr.service.model.arguments.FindFlowsByFiltersArgs;
import it.gov.pagopa.fdr.service.model.re.FdrActionEnum;
import jakarta.ws.rs.core.Response.Status;
import org.jboss.resteasy.reactive.RestResponse;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

public class InternalOrganizationsOperationsController implements IInternalOrganizationsOperationsController {

  private final FlowService flowService;

  private final PaymentService paymentService;

  protected InternalOrganizationsOperationsController(FlowService flowService, PaymentService paymentService) {

    this.flowService = flowService;
    this.paymentService = paymentService;
  }

//  @Override
//  public SingleFlowCreatedResponse getSingleFlowNotInPublishedStatusForInternalUse(
//      String pspId, String flowName, String organizationId) {
//
//    return this.flowService.retrieveSingleUnpublishedFlow(organizationId, pspId, flowName);
//  }

  @Override
  public PaginatedFlowsResponse getAllPublishedFlowsForInternalUse(String organizationId, String pspId, Optional<Instant> publishedGt, Optional<Instant> flowDate, long pageNumber, long pageSize) {

    Instant defaultDate = Instant.now().atZone(ZoneOffset.UTC).minusMonths(1).toInstant();
    return this.flowService.getPaginatedPublishedFlowsForCI(
            FindFlowsByFiltersArgs.builder()
                    .organizationId(organizationId)
                    .pspId(pspId)
                    .publishedGt(publishedGt.orElse(defaultDate))
                    .flowDate(flowDate.orElse(defaultDate))
                    .pageNumber(pageNumber)
                    .pageSize(pageSize)
                    .build());
  }
}
