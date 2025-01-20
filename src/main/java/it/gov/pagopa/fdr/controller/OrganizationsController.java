package it.gov.pagopa.fdr.controller;

import it.gov.pagopa.fdr.controller.interfaces.IOrganizationsController;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowResponse;
import it.gov.pagopa.fdr.controller.model.payment.response.PaginatedPaymentsResponse;
import it.gov.pagopa.fdr.service.organizations.OrganizationsService;
import it.gov.pagopa.fdr.service.re.model.FdrActionEnum;
import it.gov.pagopa.fdr.util.Re;
import java.time.Instant;
import org.jboss.logging.Logger;

public class OrganizationsController implements IOrganizationsController {

  private final Logger log;
  private final OrganizationsService organizationsService;

  protected OrganizationsController(Logger log, OrganizationsService service) {

    this.log = log;
    this.organizationsService = service;
  }

  @Override
  @Re(action = FdrActionEnum.GET_ALL_FDR)
  public PaginatedFlowsResponse getAllPublishedFlows(
      String organizationId, String pspId, Instant publishedGt, long pageNumber, long pageSize) {

    return null;
    // return baseGetAll(organizationId, idPsp, publishedGt, pageNumber, pageSize, false);
  }

  @Override
  @Re(action = FdrActionEnum.GET_FDR)
  public SingleFlowResponse getSingleFlow(
      String organizationId, String fdrName, Long revision, String pspId) {

    return null;
    // return baseGet(organizationId, fdr, rev, psp, true);
  }

  @Override
  @Re(action = FdrActionEnum.GET_FDR_PAYMENT)
  public PaginatedPaymentsResponse getFlowPayments(
      String organizationId,
      String fdrName,
      Long revision,
      String pspId,
      long pageNumber,
      long pageSize) {

    return null;
    // return baseGetFdrPayment(organizationId, fdr, rev, psp, pageNumber, pageSize, false);
  }
}
