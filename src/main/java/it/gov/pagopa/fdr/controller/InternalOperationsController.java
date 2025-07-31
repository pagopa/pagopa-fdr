package it.gov.pagopa.fdr.controller;

import it.gov.pagopa.fdr.controller.interfaces.controller.IInternalOperationsController;
import it.gov.pagopa.fdr.controller.model.common.response.GenericResponse;
import it.gov.pagopa.fdr.controller.model.flow.request.CreateFlowRequest;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowCreatedResponse;
import it.gov.pagopa.fdr.controller.model.payment.request.AddPaymentRequest;
import it.gov.pagopa.fdr.controller.model.payment.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.service.FlowService;
import it.gov.pagopa.fdr.service.PaymentService;
import it.gov.pagopa.fdr.service.model.arguments.FindFlowsByFiltersArgs;
import jakarta.ws.rs.core.Response.Status;
import org.jboss.resteasy.reactive.RestResponse;

public class InternalOperationsController implements IInternalOperationsController {

  private final FlowService flowService;

  private final PaymentService paymentService;

  protected InternalOperationsController(FlowService flowService, PaymentService paymentService) {

    this.flowService = flowService;
    this.paymentService = paymentService;
  }

  @Override
  // @Re(action = FdrActionEnum.INTERNAL_CREATE_FLOW)
  public RestResponse<GenericResponse> createEmptyFlowForInternalUse(
      String pspId, String flowName, CreateFlowRequest request) {

    GenericResponse response = this.flowService.createEmptyFlow(pspId, flowName, request);
    return RestResponse.status(Status.CREATED, response);
  }

  @Override
  // @Re(action = FdrActionEnum.INTERNAL_ADD_PAYMENT)
  public GenericResponse addPaymentToExistingFlowForInternalUse(
      String pspId, String flowName, AddPaymentRequest request) {

    return this.paymentService.addPaymentToExistingFlow(pspId, flowName, request);
  }

  @Override
  // @Re(action = FdrActionEnum.INTERNAL_DELETE_PAYMENT)
  public GenericResponse deletePaymentFromExistingFlowForInternalUse(
      String pspId, String flowName, DeletePaymentRequest request) {

    return this.paymentService.deletePaymentFromExistingFlow(pspId, flowName, request);
  }

  @Override
  // @Re(action = FdrActionEnum.INTERNAL_PUBLISH)
  public GenericResponse publishFlowForInternalUse(String pspId, String flowName) {

    return this.flowService.publishFlow(pspId, flowName, true);
  }

  @Override
  // @Re(action = FdrActionEnum.INTERNAL_DELETE_FLOW)
  public GenericResponse deleteExistingFlowForInternalUse(String pspId, String flowName) {

    return this.flowService.deleteExistingFlow(pspId, flowName);
  }

  @Override
  public SingleFlowCreatedResponse getSingleFlowNotInPublishedStatusForInternalUse(
      String pspId, String flowName, String organizationId) {

    return this.flowService.getSingleFlowNotInPublishedStatus(
        FindFlowsByFiltersArgs.builder()
            .pspId(pspId)
            .flowName(flowName)
            .organizationId(organizationId)
            .build());
  }
}
