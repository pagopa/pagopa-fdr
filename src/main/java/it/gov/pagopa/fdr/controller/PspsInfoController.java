package it.gov.pagopa.fdr.controller;

import it.gov.pagopa.fdr.controller.interfaces.controller.IPspsInfoController;
import it.gov.pagopa.fdr.controller.model.common.response.InfoResponse;
import it.gov.pagopa.fdr.service.HealthCheckService;

public class PspsInfoController implements IPspsInfoController {

  private final HealthCheckService healthCheckService;

  public PspsInfoController(HealthCheckService healthCheckService) {
    this.healthCheckService = healthCheckService;
  }

  @Override
  public InfoResponse healthCheck() {
    return healthCheckService.getHealthInfo();
  }
}
