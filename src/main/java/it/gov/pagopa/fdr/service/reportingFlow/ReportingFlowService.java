package it.gov.pagopa.fdr.service.reportingFlow;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlow;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowStatusEnum;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowGetDto;
import it.gov.pagopa.fdr.service.reportingFlow.mapper.ReportingFlowServiceMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReportingFlowService {

  @Inject ReportingFlowServiceMapper mapper;

  @Inject Logger log;

  @WithSpan(kind = SERVER)
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

  @WithSpan(kind = SERVER)
  public ReportingFlowGetDto findById(String id) {
    log.debugf("Get data from DB");

    if (!ObjectId.isValid(id)) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_ID_INVALID, id);
    }
    ObjectId reportingFlowId = new ObjectId(id);
    Optional<ReportingFlow> reportingFlow = ReportingFlow.findByIdOptional(reportingFlowId);
    return reportingFlow
        .map(rf -> mapper.toReportingFlowGetDto(rf))
        .orElseThrow(() -> new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, id));
  }

  @WithSpan(kind = SERVER)
  public List<ReportingFlowGetDto> findByIdEc(String idEc) {
    log.debugf("Get all data from DB");

    List<ReportingFlow> reportingFlow = ReportingFlow.list("receiver.id", idEc);
    return reportingFlow.stream().map(rf -> mapper.toReportingFlowGetDto(rf)).toList();
  }

  @WithSpan(kind = SERVER)
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
