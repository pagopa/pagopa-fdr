package it.gov.pagopa.fdr.controller;

import it.gov.pagopa.fdr.controller.interfaces.controller.IInternalInfoController;
import it.gov.pagopa.fdr.controller.model.common.response.InfoResponse;
import it.gov.pagopa.fdr.service.HealthCheckService;

public class InternalInfoController implements IInternalInfoController {

  private final HealthCheckService healthCheckService;

  public InternalInfoController(HealthCheckService healthCheckService) {
    this.healthCheckService = healthCheckService;
  }


  @Override
  public InfoResponse healthCheck() {
    return healthCheckService.getHealthInfo();
  }
}
