package it.gov.pagopa.fdr.controller;

import it.gov.pagopa.fdr.controller.interfaces.annotation.Re;
import it.gov.pagopa.fdr.controller.interfaces.controller.IInternalOperationsController;
import it.gov.pagopa.fdr.controller.model.common.response.GenericResponse;
import it.gov.pagopa.fdr.controller.model.flow.request.CreateFlowRequest;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowCreatedResponse;
import it.gov.pagopa.fdr.controller.model.payment.request.AddPaymentRequest;
import it.gov.pagopa.fdr.controller.model.payment.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.service.FlowService;
import it.gov.pagopa.fdr.service.InternalService;
import it.gov.pagopa.fdr.service.PaymentService;
import it.gov.pagopa.fdr.service.model.arguments.FindFlowsByFiltersArgs;
import it.gov.pagopa.fdr.service.model.re.FdrActionEnum;
import jakarta.ws.rs.core.Response.Status;
import org.jboss.resteasy.reactive.RestResponse;

public class InternalOperationsController implements IInternalOperationsController {

  private final InternalService internalService;

  protected InternalOperationsController(InternalService internalService) {

    this.internalService = internalService;
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_CREATE_FLOW)
  public RestResponse<GenericResponse> createEmptyFlowForInternalUse(
      String pspId, String flowName, CreateFlowRequest request) {

    GenericResponse response = this.internalService.createEmptyFlow(pspId, flowName, request);
    return RestResponse.status(Status.CREATED, response);
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_ADD_PAYMENT)
  public GenericResponse addPaymentToExistingFlowForInternalUse(
      String pspId, String flowName, AddPaymentRequest request) {

    return this.internalService.addPaymentToExistingFlow(pspId, flowName, request);
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_DELETE_PAYMENT)
  public GenericResponse deletePaymentFromExistingFlowForInternalUse(
      String pspId, String flowName, DeletePaymentRequest request) {

    return this.internalService.deletePaymentFromExistingFlow(pspId, flowName, request);
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_PUBLISH)
  public GenericResponse publishFlowForInternalUse(String pspId, String flowName) {

    return this.internalService.publishFlow(pspId, flowName, true);
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_DELETE_FLOW)
  public GenericResponse deleteExistingFlowForInternalUse(String pspId, String flowName) {

    return this.internalService.deleteExistingFlow(pspId, flowName);
  }

  @Override
  public SingleFlowCreatedResponse getSingleFlowNotInPublishedStatusForInternalUse(
      String pspId, String flowName, String organizationId) {

    return this.internalService.getSingleFlowNotInPublishedStatus(
        FindFlowsByFiltersArgs.builder()
            .pspId(pspId)
            .flowName(flowName)
            .organizationId(organizationId)
            .build());
  }
}
