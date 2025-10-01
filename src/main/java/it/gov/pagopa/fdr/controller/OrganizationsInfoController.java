package it.gov.pagopa.fdr.controller;

import it.gov.pagopa.fdr.controller.interfaces.controller.IOrganizationsInfoController;
import it.gov.pagopa.fdr.controller.model.common.response.InfoResponse;
import it.gov.pagopa.fdr.service.HealthCheckService;

public class OrganizationsInfoController implements IOrganizationsInfoController {

  private final HealthCheckService healthCheckService;

  public OrganizationsInfoController(HealthCheckService healthCheckService) {
    this.healthCheckService = healthCheckService;
  }

  @Override
  public InfoResponse healthCheck() {
    return healthCheckService.getHealthInfo();
  }
}
