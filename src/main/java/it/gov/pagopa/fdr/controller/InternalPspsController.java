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
import it.gov.pagopa.fdr.service.psps.PspsService;
import it.gov.pagopa.fdr.service.re.model.FdrActionEnum;
import it.gov.pagopa.fdr.util.Re;
import it.gov.pagopa.fdr.util.constant.AppConstant;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import java.time.Instant;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;

@Tag(name = "Internal PSP", description = "PSP operations")
@Path("/internal/psps/{" + AppConstant.PSP + "}")
@Consumes("application/json")
@Produces("application/json")
public class InternalPspsController implements IInternalPspsController {

  private Logger log;
  private PspsService service;

  protected InternalPspsController(Logger log, PspsService service) {

    this.log = log;
    this.service = service;
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_CREATE_FLOW)
  public RestResponse<GenericResponse> createEmptyFlowForInternalUse(
      String pspId, String fdrName, CreateFlowRequest request) {

    return null;
    // return baseCreate(pspId, fdr, createRequest);
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_ADD_PAYMENT)
  public GenericResponse addPaymentToExistingFlowForInternalUse(
      String pspId, String fdrName, AddPaymentRequest request) {

    return null;
    // return baseAddPayment(pspId, fdr, addPaymentRequest);
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_DELETE_PAYMENT)
  public GenericResponse deletePaymentFromExistingFlowForInternalUse(
      String pspId, String fdrName, DeletePaymentRequest request) {

    return null;
    // return baseDeletePayment(pspId, fdr, deletePaymentRequest);
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_PUBLISH)
  public GenericResponse publishFlowForInternalUse(String pspId, String fdrName) {

    return null;
    // return basePublish(pspId, fdr, true);
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_DELETE_FLOW)
  public GenericResponse deleteExistingFlowForInternalUse(String pspId, String fdrName) {

    return null;
    // return baseDelete(pspId, fdr);
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_GET_ALL_CREATED_FDR)
  public PaginatedFlowsCreatedResponse getAllFlowsNotInPublishedStatusForInternalUse(
      String pspId, Instant createdGt, long pageNumber, long pageSize) {

    return null;
    // return baseGetAllCreated(pspId, createdGt, pageNumber, pageSize);
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_GET_CREATED_FDR)
  public SingleFlowCreatedResponse getSingleFlowNotInPublishedStatusForInternalUse(
      String pspId, String fdrName, String organizationId) {

    return null;
    // return baseGetCreated(fdr, psp, organizationId);
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_GET_CREATED_FDR_PAYMENT)
  public PaginatedPaymentsResponse getPaymentsForFlowNotInPublishedStatusForInternalUse(
      String pspId, String fdrName, String organizationId, long pageNumber, long pageSize) {

    return null;
    // return baseGetCreatedFdrPayment(fdr, psp, organizationId, pageNumber, pageSize);
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_GET_ALL_FDR_PUBLISHED_BY_PSP)
  public PaginatedFlowsPublishedResponse getAllFlowsInPublishedStatusForInternalUse(
      String pspId, String organizationId, Instant publishedGt, long pageNumber, long pageSize) {

    return null;
    // return baseGetAllPublished(idPsp, organizationId, publishedGt, pageNumber, pageSize);
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_GET_FDR_PUBLISHED_BY_PSP)
  public SingleFlowResponse getSingleFlowInPublishedStatusForInternalUse(
      String pspId, String fdrName, Long revision, String organizationId) {

    return null;
    // return baseGetPublished(organizationId, fdr, rev, psp);
  }

  @Override
  @Re(action = FdrActionEnum.INTERNAL_GET_FDR_PAYMENT_PUBLISHED_BY_PSP)
  public PaginatedPaymentsResponse getPaymentsForFlowInPublishedStatusForInternalUse(
      String pspId,
      String fdrName,
      Long revision,
      String organizationId,
      long pageNumber,
      long pageSize) {

    return null;
    // return baseGetFdrPaymentPublished(organizationId, fdr, rev, psp, pageNumber, pageSize);
  }
}
