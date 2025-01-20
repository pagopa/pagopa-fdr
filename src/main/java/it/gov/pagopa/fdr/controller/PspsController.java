package it.gov.pagopa.fdr.controller;

import it.gov.pagopa.fdr.controller.interfaces.IPspsController;
import it.gov.pagopa.fdr.controller.model.common.response.GenericResponse;
import it.gov.pagopa.fdr.controller.model.flow.request.CreateFlowRequest;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsCreatedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsPublishedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowCreatedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowResponse;
import it.gov.pagopa.fdr.controller.model.payment.request.AddPaymentRequest;
import it.gov.pagopa.fdr.controller.model.payment.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.controller.model.payment.response.PaginatedPaymentsResponse;
import it.gov.pagopa.fdr.service.psps.PspsService;
import it.gov.pagopa.fdr.service.re.model.FdrActionEnum;
import it.gov.pagopa.fdr.util.Re;
import java.time.Instant;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;

public class PspsController implements IPspsController {

  private Logger log;
  private PspsService service;

  protected PspsController(Logger log, PspsService service) {

    this.log = log;
    this.service = service;
  }

  @Override
  @Re(action = FdrActionEnum.CREATE_FLOW)
  public RestResponse<GenericResponse> createEmptyFlow(
      String pspId, String fdrName, CreateFlowRequest request) {

    return null;
    // return baseCreate(pspId, fdr, createRequest);
  }

  @Override
  @Re(action = FdrActionEnum.ADD_PAYMENT)
  public GenericResponse addPaymentToExistingFlow(
      String pspId, String fdrName, AddPaymentRequest request) {

    return null;
    // return baseAddPayment(pspId, fdr, addPaymentRequest);
  }

  @Override
  @Re(action = FdrActionEnum.DELETE_PAYMENT)
  public GenericResponse deletePaymentFromExistingFlow(
      String pspId, String fdrName, DeletePaymentRequest request) {

    return null;
    // return baseDeletePayment(pspId, fdr, deletePaymentRequest);
  }

  @Override
  @Re(action = FdrActionEnum.PUBLISH)
  public GenericResponse publishFlow(String pspId, String fdrName) {

    return null;
    // return basePublish(pspId, fdr, false);
  }

  @Override
  @Re(action = FdrActionEnum.DELETE_FLOW)
  public GenericResponse deleteExistingFlow(String pspId, String fdrName) {

    return null;
    // return baseDelete(pspId, fdr);
  }

  @Override
  @Re(action = FdrActionEnum.GET_ALL_CREATED_FDR)
  public PaginatedFlowsCreatedResponse getAllFlowsNotInPublishedStatus(
      String pspId, Instant createdGt, long pageNumber, long pageSize) {

    return null;
    // return baseGetAllCreated(pspId, createdGt, pageNumber, pageSize);
  }

  @Override
  @Re(action = FdrActionEnum.GET_CREATED_FDR)
  public SingleFlowCreatedResponse getSingleFlowNotInPublishedStatus(
      String pspId, String fdrName, String organizationId) {

    return null;
    // return baseGetCreated(fdr, psp, organizationId);
  }

  @Override
  @Re(action = FdrActionEnum.GET_CREATED_FDR_PAYMENT)
  public PaginatedPaymentsResponse getPaymentsForFlowNotInPublishedStatus(
      String pspId, String fdrName, String organizationId, long pageNumber, long pageSize) {

    return null;
    // return baseGetCreatedFdrPayment(fdr, psp, organizationId, pageNumber, pageSize);
  }

  @Override
  @Re(action = FdrActionEnum.GET_ALL_FDR_PUBLISHED_BY_PSP)
  public PaginatedFlowsPublishedResponse getAllFlowsInPublishedStatus(
      String pspId, String organizationId, Instant publishedGt, long pageNumber, long pageSize) {

    return null;
    // return baseGetAllPublished(idPsp, organizationId, publishedGt, pageNumber, pageSize);
  }

  @Override
  @Re(action = FdrActionEnum.GET_FDR_PUBLISHED_BY_PSP)
  public SingleFlowResponse getSingleFlowInPublishedStatus(
      String pspId, String fdrName, Long revision, String organizationId) {

    return null;
    // return baseGetPublished(psp, fdr, rev, organizationId);
  }

  @Override
  @Re(action = FdrActionEnum.GET_FDR_PAYMENT_PUBLISHED_BY_PSP)
  public PaginatedPaymentsResponse getPaymentsForFlowInPublishedStatus(
      String pspId,
      String fdrName,
      Long revision,
      String organizationId,
      long pageNumber,
      long pageSize) {

    return null;
    // return baseGetFdrPaymentPublished(psp, fdr, rev, organizationId, pageNumber, pageSize);
  }
}
