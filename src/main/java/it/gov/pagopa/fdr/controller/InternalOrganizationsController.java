package it.gov.pagopa.fdr.controller;

import it.gov.pagopa.fdr.controller.interfaces.IInternalOrganizationsController;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowResponse;
import it.gov.pagopa.fdr.controller.model.payment.response.PaginatedPaymentsResponse;
import it.gov.pagopa.fdr.service.organizations.OrganizationsService;
import it.gov.pagopa.fdr.service.re.model.FdrActionEnum;
import it.gov.pagopa.fdr.util.Re;
import java.time.Instant;
import org.jboss.logging.Logger;

public class InternalOrganizationsController implements IInternalOrganizationsController {

  private final Logger log;
  private final OrganizationsService organizationsService;

  protected InternalOrganizationsController(Logger log, OrganizationsService service) {

    this.log = log;
    this.organizationsService = service;
  }

  @Re(action = FdrActionEnum.INTERNAL_GET_ALL_FDR)
  public PaginatedFlowsResponse getAllPublishedFlowsForInternalUse(
      String organizationId, String pspId, Instant publishedGt, long pageNumber, long pageSize) {

    return null;
    // return baseGetAll(organizationId, idPsp, publishedGt, pageNumber, pageSize, true);
  }

  @Re(action = FdrActionEnum.INTERNAL_GET_FDR)
  public SingleFlowResponse getSingleFlowForInternalUse(
      String organizationId, String fdrName, Long revision, String pspId) {

    return null;
    // return baseGet(organizationId, fdr, rev, psp, true);
  }

  @Re(action = FdrActionEnum.INTERNAL_GET_FDR_PAYMENT)
  public PaginatedPaymentsResponse getFlowPaymentsForInternalUse(
      String organizationId,
      String fdrName,
      Long revision,
      String pspId,
      long pageNumber,
      long pageSize) {

    return null;
    // return baseGetFdrPayment(organizationId, fdr, rev, psp, pageNumber, pageSize, true);
  }
}
