package it.gov.pagopa.fdr.controller;

import it.gov.pagopa.fdr.controller.interfaces.annotation.Re;
import it.gov.pagopa.fdr.controller.interfaces.controller.IInternalPspsOperationsController;
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

public class InternalPspsOperationsController implements IInternalPspsOperationsController {

  private final FlowService flowService;

  private final PaymentService paymentService;

  protected InternalPspsOperationsController(FlowService flowService, PaymentService paymentService) {

    this.flowService = flowService;
    this.paymentService = paymentService;
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_CREATE_FLOW)
  public RestResponse<GenericResponse> createEmptyFlowForInternalUse(
      String pspId, String flowName, CreateFlowRequest request) {

    GenericResponse response = this.flowService.createEmptyFlowForInternalUse(pspId, flowName, request);
    return RestResponse.status(Status.CREATED, response);
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_ADD_PAYMENT)
  public GenericResponse addPaymentToExistingFlowForInternalUse(
      String pspId, String flowName, InternalAddPaymentRequest request) {

    return this.paymentService.addPaymentsToUnpublishedFlow(pspId, flowName, request.getPayments());
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_DELETE_PAYMENT)
  public GenericResponse deletePaymentFromExistingFlowForInternalUse(
      String pspId, String flowName, InternalDeletePaymentRequest request) {

    return this.paymentService.deletePaymentFromUnpublishedFlow(pspId, flowName, request.getIndexList());
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_PUBLISH)
  public GenericResponse publishFlowForInternalUse(String pspId, String flowName) {

    return this.flowService.publishFlowForInternalUse(pspId, flowName, true);
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_DELETE_FLOW)
  public GenericResponse deleteExistingFlowForInternalUse(String pspId, String flowName) {

    return this.flowService.deleteUnpublishedFlow(pspId, flowName);
  }

  @Override
  public SingleFlowCreatedResponse getSingleFlowNotInPublishedStatusForInternalUse(
      String pspId, String flowName, String organizationId) {

    return this.flowService.retrieveSingleUnpublishedFlow(organizationId, pspId, flowName);
  }

//  @Override
//  public PaginatedFlowsResponse getAllPublishedFlowsForInternalUse(String organizationId, String pspId, Optional<Instant> publishedGt, Optional<Instant> flowDate, long pageNumber, long pageSize) {
//
//    Instant defaultDate = Instant.now().atZone(ZoneOffset.UTC).minusMonths(1).toInstant();
//    return this.flowService.getPaginatedPublishedFlowsForCI(
//            FindFlowsByFiltersArgs.builder()
//                    .organizationId(organizationId)
//                    .pspId(pspId)
//                    .publishedGt(publishedGt.orElse(defaultDate))
//                    .flowDate(flowDate.orElse(defaultDate))
//                    .pageNumber(pageNumber)
//                    .pageSize(pageSize)
//                    .build());
//  }
}
