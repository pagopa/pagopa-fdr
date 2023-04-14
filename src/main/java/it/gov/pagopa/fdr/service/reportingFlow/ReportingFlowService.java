package it.gov.pagopa.fdr.service.reportingFlow;

import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlow;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowStatusEnum;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowDto;
import it.gov.pagopa.fdr.service.reportingFlow.mapper.ReportingFlowServiceMapper;
import java.time.Instant;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReportingFlowService {

  @Inject ReportingFlowServiceMapper mapper;

  @Inject Logger log;

  public String save(ReportingFlowDto reportingFlowDto) {
    log.debugf("Save data on DB");

    Instant now = Instant.now();

    Optional<ReportingFlow> byIdOptional =
        ReportingFlow.findByIdOptional(reportingFlowDto.getReportingFlow());
    ReportingFlow flow =
        byIdOptional.orElseGet(
            () -> {
              ReportingFlow reportingFlow = mapper.toReportingFlow(reportingFlowDto);
              reportingFlow.created = now;
              reportingFlow.updated = now;
              return reportingFlow;
            });
    flow.updated = now;

    flow.persist();
    return flow.id.toString();
  }

  public void confirm(String id) {
    log.debugf("Update status id: [%s]", id);

    Optional<ReportingFlow> byIdOptional = ReportingFlow.findByIdOptional(id);
    ReportingFlow flow =
        byIdOptional.orElseThrow(
            () -> new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, id));

    flow.status = ReportingFlowStatusEnum.TO_VALIDATE;

    flow.persist();
  }
}
