package it.gov.pagopa.fdr.controller;

import it.gov.pagopa.fdr.controller.interfaces.controller.IInternalOrganizationsOperationsController;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsResponse;
import it.gov.pagopa.fdr.service.FlowService;
import it.gov.pagopa.fdr.service.model.arguments.FindFlowsByFiltersArgs;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;

public class InternalOrganizationsOperationsController implements IInternalOrganizationsOperationsController {

  private final FlowService flowService;

  protected InternalOrganizationsOperationsController(FlowService flowService) {
    this.flowService = flowService;
  }


  @Override
  public PaginatedFlowsResponse getAllPublishedFlowsForInternalUse(String organizationId, String pspId, Optional<Instant> publishedGt, Optional<Instant> flowDate, long pageNumber, long pageSize) {

    Instant defaultDate = Instant.now().atZone(ZoneOffset.UTC).minusDays(30)
            .toLocalDate()
            .atTime(LocalTime.MIN).atZone(ZoneOffset.UTC).toInstant();

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
