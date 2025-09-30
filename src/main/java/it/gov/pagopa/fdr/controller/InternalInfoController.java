package it.gov.pagopa.fdr.controller;

import it.gov.pagopa.fdr.controller.interfaces.controller.IInfoController;
import it.gov.pagopa.fdr.controller.model.common.response.InfoResponse;
import it.gov.pagopa.fdr.service.HealthCheckService;
import jakarta.ws.rs.Path;

public class InternalInfoController implements IInfoController {

  private final HealthCheckService healthCheckService;

  public InternalInfoController(HealthCheckService healthCheckService) {
    this.healthCheckService = healthCheckService;
  }

  @Path("/internal/info")
  @Override
  public InfoResponse healthCheck() {
    return healthCheckService.getHealthInfo();
  }
}
