package it.gov.pagopa.fdr.controller;

import it.gov.pagopa.fdr.controller.interfaces.IInternalPspsController;
import it.gov.pagopa.fdr.controller.model.common.response.GenericResponse;
import it.gov.pagopa.fdr.controller.model.flow.request.CreateFlowRequest;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsCreatedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsPublishedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowCreatedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowResponse;
import it.gov.pagopa.fdr.controller.model.payment.request.AddPaymentRequest;
import it.gov.pagopa.fdr.controller.model.payment.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.controller.model.payment.response.PaginatedPaymentsResponse;
import it.gov.pagopa.fdr.service.FlowService;
import it.gov.pagopa.fdr.service.PaymentService;
import it.gov.pagopa.fdr.service.model.arguments.FindFlowsByFiltersArgs;
import it.gov.pagopa.fdr.service.model.re.FdrActionEnum;
import it.gov.pagopa.fdr.util.Re;
import it.gov.pagopa.fdr.util.constant.ControllerConstants;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response.Status;
import java.time.Instant;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestResponse;

@Tag(name = "Internal PSP", description = "PSP operations")
@Path("/internal/psps/{" + ControllerConstants.PARAMETER_PSP + "}")
@Consumes("application/json")
@Produces("application/json")
public class InternalPspsController implements IInternalPspsController {

  private FlowService flowService;

  private PaymentService paymentService;

  protected InternalPspsController(FlowService flowService, PaymentService paymentService) {

    this.flowService = flowService;
    this.paymentService = paymentService;
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_CREATE_FLOW)
  public RestResponse<GenericResponse> createEmptyFlowForInternalUse(
      String pspId, String flowName, CreateFlowRequest request) {

    GenericResponse response = this.flowService.createEmptyFlow(pspId, flowName, request);
    return RestResponse.status(Status.CREATED, response);
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_ADD_PAYMENT)
  public GenericResponse addPaymentToExistingFlowForInternalUse(
      String pspId, String flowName, AddPaymentRequest request) {

    return this.paymentService.addPaymentToExistingFlow(pspId, flowName, request);
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_DELETE_PAYMENT)
  public GenericResponse deletePaymentFromExistingFlowForInternalUse(
      String pspId, String flowName, DeletePaymentRequest request) {

    return this.paymentService.deletePaymentFromExistingFlow(pspId, flowName, request);
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_PUBLISH)
  public GenericResponse publishFlowForInternalUse(String pspId, String flowName) {

    return this.flowService.publishFlow(pspId, flowName, true);
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_DELETE_FLOW)
  public GenericResponse deleteExistingFlowForInternalUse(String pspId, String flowName) {

    return this.flowService.deleteExistingFlow(pspId, flowName);
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_GET_ALL_CREATED_FDR)
  public PaginatedFlowsCreatedResponse getAllFlowsNotInPublishedStatusForInternalUse(
      String pspId, Instant createdGt, long pageNumber, long pageSize) {

    return this.flowService.getAllFlowsNotInPublishedStatus(
        FindFlowsByFiltersArgs.builder()
            .pspId(pspId)
            .createdGt(createdGt)
            .pageNumber(pageNumber)
            .pageSize(pageSize)
            .build());
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_GET_CREATED_FDR)
  public SingleFlowCreatedResponse getSingleFlowNotInPublishedStatusForInternalUse(
      String pspId, String flowName, String organizationId) {

    return this.flowService.getSingleFlowNotInPublishedStatus(
        FindFlowsByFiltersArgs.builder()
            .pspId(pspId)
            .flowName(flowName)
            .organizationId(organizationId)
            .build());
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_GET_CREATED_FDR_PAYMENT)
  public PaginatedPaymentsResponse getPaymentsForFlowNotInPublishedStatusForInternalUse(
      String pspId, String flowName, String organizationId, long pageNumber, long pageSize) {

    return this.paymentService.getPaymentsFromUnpublishedFlow(
        FindFlowsByFiltersArgs.builder()
            .organizationId(organizationId)
            .pspId(pspId)
            .flowName(flowName)
            .pageNumber(pageNumber)
            .pageSize(pageSize)
            .build());
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_GET_ALL_FDR_PUBLISHED_BY_PSP)
  public PaginatedFlowsPublishedResponse getAllFlowsInPublishedStatusForInternalUse(
      String pspId, String organizationId, Instant publishedGt, long pageNumber, long pageSize) {

    return this.flowService.getPaginatedPublishedFlowsForPSP(
        FindFlowsByFiltersArgs.builder()
            .pspId(pspId)
            .organizationId(organizationId)
            .publishedGt(publishedGt)
            .pageSize(pageSize)
            .pageNumber(pageNumber)
            .build());
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_GET_FDR_PUBLISHED_BY_PSP)
  public SingleFlowResponse getSingleFlowInPublishedStatusForInternalUse(
      String pspId, String flowName, Long revision, String organizationId) {

    return this.flowService.getSinglePublishedFlow(
        FindFlowsByFiltersArgs.builder()
            .organizationId(organizationId)
            .pspId(pspId)
            .flowName(flowName)
            .revision(revision)
            .build());
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_GET_FDR_PAYMENT_PUBLISHED_BY_PSP)
  public PaginatedPaymentsResponse getPaymentsForFlowInPublishedStatusForInternalUse(
      String pspId,
      String flowName,
      Long revision,
      String organizationId,
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
