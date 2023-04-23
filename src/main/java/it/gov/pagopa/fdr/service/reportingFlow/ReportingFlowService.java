package it.gov.pagopa.fdr.service.reportingFlow;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowRevisionEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.model.ReportingFlowStatusEnumEntity;
import it.gov.pagopa.fdr.service.reportingFlow.dto.AddPaymentDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowByIdEcDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowGetDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowGetPaymentDto;
import it.gov.pagopa.fdr.service.reportingFlow.mapper.ReportingFlowServiceMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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

    ReportingFlowRevisionEntity reportingFlowRevision = getRevision(reportingFlowEntity);
    reportingFlowRevision.persist();
  }

  @WithSpan(kind = SERVER)
  public void addPayment(String id, AddPaymentDto addPaymentDto) {
    log.debugf("Save add payment on DB");
    Instant now = Instant.now();

    //    ReportingFlowEntity reportingFlowEntity = fetch(id, ReportingFlowEntity.class);

    //    if (reportingFlowEntity.payments == null) {
    //      reportingFlowEntity.payments = mapper.toPagamentos(addPaymentDto.getPayments());
    //    } else {
    //      reportingFlowEntity.payments.addAll(mapper.toPagamentos(addPaymentDto.getPayments()));
    //    }

    //    reportingFlowEntity.updated = now;
    //    reportingFlowEntity.status = ReportingFlowStatusEnumEntity.ADD_PAYMENT;
    //    reportingFlowEntity.revision = (reportingFlowEntity.revision == null) ? 1L :
    // reportingFlowEntity.revision + 1;
    //    reportingFlowEntity.update();

    //    ReportingFlowRevisionEntity reportingFlowRevision = getRevision(reportingFlowEntity);
    //    reportingFlowRevision.persist();
  }

  @WithSpan(kind = SERVER)
  public void confirm(String id) {
    log.debugf("Confirm reporting flow");
    //    Instant now = Instant.now();
    //
    //    ReportingFlowEntity reportingFlowEntity = fetch(id, ReportingFlowEntity.class);
    //
    //    reportingFlowEntity.updated = now;
    //    reportingFlowEntity.status = ReportingFlowStatusEnumEntity.CONFIRMED;
    //    reportingFlowEntity.revision =
    //        (reportingFlowEntity.revision == null) ? 1L : reportingFlowEntity.revision + 1;
    //    reportingFlowEntity.update();
    //
    //    ReportingFlowRevisionEntity reportingFlowRevision = getRevision(reportingFlowEntity);
    //    reportingFlowRevision.persist();
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

    ReportingFlowRevisionEntity reportingFlowRevision = getRevision(reportingFlowEntity);
    reportingFlowRevision.persist();
  }

  @WithSpan(kind = SERVER)
  public ReportingFlowGetDto findByReportingFlowName(String reportingFlowName) {
    log.debugf("Get data from DB");

    return mapper.toReportingFlowGetDto(retrieve(reportingFlowName));
  }

  @WithSpan(kind = SERVER)
  public ReportingFlowGetPaymentDto findPaymentById(String id, int pageNumber, int pageSize) {
    log.debugf("Get data from DB");

    //    ReportingFlowOnlyPayment reportingFlowOnlyPayment = fetchSlicePayment(id, pageNumber,
    // pageSize);
    //
    //    long count = reportingFlowOnlyPayment.count;
    //    int totPage = (int) Math.ceil(count / (double) pageSize);
    //
    //    return ReportingFlowGetPaymentDto.builder()
    //        .metadata(
    //            MetadataDto.builder()
    //                .pageSize(pageSize)
    //                .pageNumber(pageNumber)
    //                .totPage(totPage)
    //                .build())
    //        .count(count)
    //        .sum(reportingFlowOnlyPayment.sum)
    //        .data(mapper.toPagamentoDtos(reportingFlowOnlyPayment.payments))
    //        .build();
    return null;
  }

  @WithSpan(kind = SERVER)
  public ReportingFlowByIdEcDto findByIdEc(
      String idEc, String idPsp, int pageNumber, int pageSize) {
    log.debugf("Get all data from DB");

    //    Page page = Page.of(pageNumber - 1, pageSize);
    //    Sort sort = getSort(List.of("_id,asc"));
    //
    //    PanacheQuery<ReportingFlowEntity> reportingFlowPanacheQuery;
    //    if (idPsp == null || idPsp.isBlank()) {
    //      reportingFlowPanacheQuery = ReportingFlowEntity.find("receiver.idEc", sort, idEc);
    //    } else {
    //      reportingFlowPanacheQuery =
    //          ReportingFlowEntity.find(
    //              "receiver.idEc = ?1 and sender.idPsp = ?2", sort, idEc, idPsp);
    //    }
    //    PanacheQuery<ReportingFlowId> reportingFlowIdPanacheQuery =
    //        reportingFlowPanacheQuery.page(page).project(ReportingFlowId.class);
    //    List<ReportingFlowId> reportingFlowIds = reportingFlowIdPanacheQuery.list();
    //
    //    int totPage = reportingFlowIdPanacheQuery.pageCount();
    //    long countReportingFlow = reportingFlowIdPanacheQuery.count();
    //
    //    return ReportingFlowByIdEcDto.builder()
    //        .metadata(
    //            MetadataDto.builder()
    //                .pageSize(pageSize)
    //                .pageNumber(pageNumber)
    //                .totPage(totPage)
    //                .build())
    //        .count(countReportingFlow)
    //        .data(reportingFlowIds.stream().map(rf -> rf.id.toString()).toList())
    //        .build();
    return null;
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
    if (getByReportingFlowName(reportingFlowName).isPresent()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_ALREADY_EXIST, reportingFlowName);
    }
  }

  private ReportingFlowEntity retrieve(String reportingFlowName) {
    return getByReportingFlowName(reportingFlowName)
        .orElseThrow(
            () ->
                new AppException(
                    AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, reportingFlowName));
  }

  private Optional<ReportingFlowEntity> getByReportingFlowName(String reportingFlowName) {
    return ReportingFlowEntity.find("reporting_flow_name", reportingFlowName)
        .project(ReportingFlowEntity.class)
        .firstResultOptional();
  }

  //  private <T> T fetch(String id, Class<T> clazz) {
  //    Optional<T> reportingFlowOptional =
  //        ReportingFlowEntity.find("_id", id).project(clazz).firstResultOptional();
  //    return reportingFlowOptional.orElseThrow(
  //        () -> new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, id));
  //  }

  //  private ReportingFlowOnlyPayment fetchSlicePayment(String id, long pageNumber, long pageSize)
  // {
  //    long skip = (pageNumber - 1) * pageSize;
  //    List<Document> aggregate =
  //        Arrays.asList(
  //            new Document("$match", new Document("_id", objectId)),
  //            new Document(
  //                "$project",
  //                new Document(
  //                    "payments",
  //                    new Document(
  //                        "$sortArray",
  //                        new Document(
  //                                "input",
  //                                new Document("$ifNull", Arrays.asList("$payments", List.of())))
  //                            .append(
  //                                "sortBy", new Document("identificativoUnivocoVersamento",
  // 1L))))),
  //            new Document(
  //                "$addFields",
  //                new Document("count", new Document("$size", "$payments"))
  //                    .append(
  //                        "sum",
  //                        new Document(
  //                            "$toDecimal", new Document("$sum",
  // "$payments.singoloImportoPagato")))),
  //            new Document(
  //                "$project",
  //                new Document("count", 1L)
  //                    .append("sum", 1L)
  //                    .append(
  //                        "payments",
  //                        new Document("$slice", Arrays.asList("$payments", skip, pageSize)))));
  //
  //    ReportingFlowOnlyPayment reportingFlowOnlyPayment =
  //        ReportingFlowEntity.mongoCollection()
  //            .aggregate(aggregate, ReportingFlowOnlyPayment.class)
  //            .first();
  //
  //    return Optional.ofNullable(reportingFlowOnlyPayment)
  //        .orElseThrow(() -> new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND,
  // id));
  //  }

  private ReportingFlowRevisionEntity getRevision(ReportingFlowEntity reportingFlowEntity) {
    return mapper.toReportingFlowRevision(reportingFlowEntity);
  }
}
