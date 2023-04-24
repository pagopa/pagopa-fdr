package it.gov.pagopa.fdr.service.reportingFlow;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowPaymentEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowPaymentRevisionEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowRevisionEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.model.ReportingFlowPaymentStatusEnumEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.model.ReportingFlowStatusEnumEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.projection.ReportingFlowIdNameProjection;
import it.gov.pagopa.fdr.repository.reportingFlow.projection.ReportingFlowNameProjection;
import it.gov.pagopa.fdr.repository.reportingFlow.projection.ReportingFlowPaymentComputedFieldProjection;
import it.gov.pagopa.fdr.service.reportingFlow.dto.AddPaymentDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.MetadataDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowByIdEcDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowGetDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowGetPaymentDto;
import it.gov.pagopa.fdr.service.reportingFlow.mapper.ReportingFlowServiceMapper;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.bson.Document;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReportingFlowService {

  @Inject ReportingFlowServiceMapper mapper;

  @Inject Logger log;

  @WithSpan(kind = SERVER)
  public void save(ReportingFlowDto reportingFlowDto) {
    log.debugf("Save data on DB");
    Instant now = Instant.now();
    String reportingFlowName = reportingFlowDto.getReportingFlowName();

    abortIfExist(reportingFlowName);

    ReportingFlowEntity reportingFlowEntity = mapper.toReportingFlow(reportingFlowDto);
    reportingFlowEntity.created = now;

    reportingFlowEntity.updated = now;
    reportingFlowEntity.status = ReportingFlowStatusEnumEntity.NEW;
    reportingFlowEntity.revision =
        (reportingFlowEntity.revision == null) ? 1L : reportingFlowEntity.revision + 1;
    reportingFlowEntity.persist();

    ReportingFlowRevisionEntity reportingFlowRevision =
        mapper.toReportingFlowRevision(reportingFlowEntity);
    reportingFlowRevision.persist();
  }

  @WithSpan(kind = SERVER)
  public void addPayment(String reportingFlowName, AddPaymentDto addPaymentDto) {
    log.debugf("Save add payment on DB");
    Instant now = Instant.now();

    ReportingFlowIdNameProjection reportingFlowEntity =
        retrieve(reportingFlowName, ReportingFlowIdNameProjection.class);

    List<ReportingFlowPaymentEntity> payments =
        mapper.toReportingFlowPaymentEntityList(addPaymentDto.getPayments());
    payments.forEach(
        p -> {
          p.created = now;

          p.updated = now;
          p.status = ReportingFlowPaymentStatusEnumEntity.ADD;
          p.revision = (p.revision == null) ? 1L : p.revision + 1;
          p.reporting_flow_id = reportingFlowEntity.id;
          p.reporting_flow_name = reportingFlowEntity.reporting_flow_name;
        });
    ReportingFlowPaymentEntity.persist(payments);

    List<ReportingFlowPaymentRevisionEntity> reportingFlowPaymentRevisionEntity =
        mapper.toReportingFlowPaymentRevisionEntityList(payments);
    ReportingFlowPaymentRevisionEntity.persist(reportingFlowPaymentRevisionEntity);
  }

  @WithSpan(kind = SERVER)
  public void confirmByReportingFlowName(String reportingFlowName) {
    log.debugf("Confirm reporting flow");
    Instant now = Instant.now();

    ReportingFlowEntity reportingFlowEntity = retrieve(reportingFlowName);

    reportingFlowEntity.updated = now;
    reportingFlowEntity.status = ReportingFlowStatusEnumEntity.CONFIRMED;
    reportingFlowEntity.revision =
        (reportingFlowEntity.revision == null) ? 1L : reportingFlowEntity.revision + 1;
    reportingFlowEntity.update();

    ReportingFlowRevisionEntity reportingFlowRevision =
        mapper.toReportingFlowRevision(reportingFlowEntity);
    reportingFlowRevision.persist();
  }

  @WithSpan(kind = SERVER)
  public void deleteByReportingFlowName(String reportingFlowName) {
    log.debugf("Delete reporting flow");
    Instant now = Instant.now();

    ReportingFlowEntity reportingFlowEntity = retrieve(reportingFlowName);

    reportingFlowEntity.updated = now;
    reportingFlowEntity.status = ReportingFlowStatusEnumEntity.DELETED;
    reportingFlowEntity.revision =
        (reportingFlowEntity.revision == null) ? 1L : reportingFlowEntity.revision + 1;
    reportingFlowEntity.update();

    ReportingFlowRevisionEntity reportingFlowRevision =
        mapper.toReportingFlowRevision(reportingFlowEntity);
    reportingFlowRevision.persist();
  }

  @WithSpan(kind = SERVER)
  public ReportingFlowGetDto findByReportingFlowName(String reportingFlowName) {
    log.debugf("Get data from DB");

    return mapper.toReportingFlowGetDto(retrieve(reportingFlowName));
  }

  @WithSpan(kind = SERVER)
  public ReportingFlowGetPaymentDto findPaymentById(
      String reportingFlowName, int pageNumber, int pageSize) {
    log.debugf("Get data from DB");

    ReportingFlowPaymentComputedFieldProjection sliceOfPayment =
        getSliceOfPayment(reportingFlowName, pageNumber, pageSize);

    long count = sliceOfPayment.count;
    int totPage = (int) Math.ceil(count / (double) pageSize);

    return ReportingFlowGetPaymentDto.builder()
        .metadata(
            MetadataDto.builder()
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .totPage(totPage)
                .build())
        .count(count)
        .sum(sliceOfPayment.sum)
        .data(mapper.toPagamentoDtos(sliceOfPayment.data))
        .build();
  }

  @WithSpan(kind = SERVER)
  public ReportingFlowByIdEcDto findByIdEc(
      String idEc, String idPsp, int pageNumber, int pageSize) {
    log.debugf("Get all data from DB");

    Page page = Page.of(pageNumber - 1, pageSize);
    Sort sort = getSort(List.of("_id,asc"));

    PanacheQuery<ReportingFlowEntity> reportingFlowPanacheQuery;
    if (idPsp == null || idPsp.isBlank()) {
      reportingFlowPanacheQuery = ReportingFlowEntity.find("receiver.ec_id", sort, idEc);
    } else {
      reportingFlowPanacheQuery =
          ReportingFlowEntity.find("receiver.ec_id = ?1 and sender.psp_id = ?2", sort, idEc, idPsp);
    }
    PanacheQuery<ReportingFlowNameProjection> reportingFlowNameProjectionPanacheQuery =
        reportingFlowPanacheQuery.page(page).project(ReportingFlowNameProjection.class);

    List<ReportingFlowNameProjection> reportingFlowIds =
        reportingFlowNameProjectionPanacheQuery.list();

    int totPage = reportingFlowNameProjectionPanacheQuery.pageCount();
    long countReportingFlow = reportingFlowNameProjectionPanacheQuery.count();

    return ReportingFlowByIdEcDto.builder()
        .metadata(
            MetadataDto.builder()
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .totPage(totPage)
                .build())
        .count(countReportingFlow)
        .data(reportingFlowIds.stream().map(rf -> rf.reporting_flow_name).toList())
        .build();
  }

  private Sort getSort(List<String> sortColumn) {
    Sort sort = Sort.empty();
    if (sortColumn != null && sortColumn.size() > 0) {
      sortColumn.stream()
          .filter(s -> s.replace(",", "").isBlank())
          .forEach(
              a -> {
                String[] split = a.split(",");
                String column = split[0].trim();
                String direction = split[1].trim();
                if (!column.isBlank()) {
                  if (direction.equalsIgnoreCase("asc")) {
                    sort.and(column, Direction.Ascending);
                  } else if (direction.equalsIgnoreCase("desc")) {
                    sort.and(column, Direction.Descending);
                  } else {
                    sort.and(column);
                  }
                }
              });
    }
    return sort;
  }

  private void abortIfExist(String reportingFlowName) {
    if (getByReportingFlowName(reportingFlowName, ReportingFlowEntity.class).isPresent()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_ALREADY_EXIST, reportingFlowName);
    }
  }

  private ReportingFlowEntity retrieve(String reportingFlowName) {
    return getByReportingFlowName(reportingFlowName, ReportingFlowEntity.class)
        .orElseThrow(
            () ->
                new AppException(
                    AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, reportingFlowName));
  }

  private <T> T retrieve(String reportingFlowName, Class<T> clazz) {
    return getByReportingFlowName(reportingFlowName, clazz)
        .orElseThrow(
            () ->
                new AppException(
                    AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, reportingFlowName));
  }

  private <T> Optional<T> getByReportingFlowName(String reportingFlowName, Class<T> clazz) {
    return ReportingFlowEntity.find("reporting_flow_name", reportingFlowName)
        .project(clazz)
        .firstResultOptional();
  }

  private ReportingFlowPaymentComputedFieldProjection getSliceOfPayment(
      String reportingFlowName, long pageNumber, long pageSize) {
    long skip = (pageNumber - 1) * pageSize;
    List<Document> aggregate =
        Arrays.asList(
            new Document("$match", new Document("reporting_flow_name", reportingFlowName)),
            new Document(
                "$group",
                new Document("_id", "$reporting_flow_name")
                    .append(
                        "data",
                        new Document(
                            "$addToSet",
                            new Document("_id", "$_id")
                                .append("created", "$created")
                                .append("index", "$index")
                                .append("iur", "$iur")
                                .append("iuv", "$iuv")
                                .append("pay", "$pay")
                                .append("pay_date", "$pay_date")
                                .append("pay_status", "$pay_status")
                                .append("revision", "$revision")
                                .append("reporting_flow_id", "$reporting_flow_id")
                                .append("reporting_flow_name", "$reporting_flow_name")
                                .append("status", "$status")
                                .append("updated", "$updated")))
                    .append("count", new Document("$sum", 1L))
                    .append("sum", new Document("$sum", "$pay"))),
            new Document(
                "$project",
                new Document("_id", 0L)
                    .append(
                        "data",
                        new Document(
                            "$slice",
                            Arrays.asList(
                                new Document(
                                    "$sortArray",
                                    new Document("input", "$data")
                                        .append("sortBy", new Document("index", 1L))),
                                skip,
                                pageSize)))
                    .append("count", 1L)
                    .append("sum", 1L)));

    ReportingFlowPaymentComputedFieldProjection reportingFlowPaymentComputedFieldProjection =
        ReportingFlowPaymentEntity.mongoCollection()
            .aggregate(aggregate, ReportingFlowPaymentComputedFieldProjection.class)
            .first();

    return Optional.ofNullable(reportingFlowPaymentComputedFieldProjection)
        .orElseThrow(
            () ->
                new AppException(
                    AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, reportingFlowName));
  }
}
