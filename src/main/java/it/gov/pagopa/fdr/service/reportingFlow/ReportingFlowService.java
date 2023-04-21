package it.gov.pagopa.fdr.service.reportingFlow;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlow;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowRevision;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowStatusEnum;
import it.gov.pagopa.fdr.repository.reportingFlow.projection.ReportingFlowId;
import it.gov.pagopa.fdr.repository.reportingFlow.projection.ReportingFlowNoPayment;
import it.gov.pagopa.fdr.repository.reportingFlow.projection.ReportingFlowOnlyPayment;
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

    ReportingFlow reportingFlow = mapper.toReportingFlow(reportingFlowDto);
    reportingFlow.created = now;

    reportingFlow.updated = now;
    reportingFlow.status = ReportingFlowStatusEnum.NEW_LOAD;
    reportingFlow.revision = (reportingFlow.revision == null) ? 1L : reportingFlow.revision + 1;
    reportingFlow.persist();

    ReportingFlowRevision reportingFlowRevision = getRevision(reportingFlow);
    reportingFlowRevision.persist();

    return reportingFlow.id.toString();
  }

  @WithSpan(kind = SERVER)
  public void addPayment(String id, AddPaymentDto addPaymentDto) {
    log.debugf("Save add payment on DB");
    Instant now = Instant.now();

    ReportingFlow reportingFlow = fetch(id, ReportingFlow.class);

    if (reportingFlow.payments == null) {
      reportingFlow.payments = mapper.toPagamentos(addPaymentDto.getPayments());
    } else {
      reportingFlow.payments.addAll(mapper.toPagamentos(addPaymentDto.getPayments()));
    }

    reportingFlow.updated = now;
    reportingFlow.status = ReportingFlowStatusEnum.ADD_PAYMENT;
    reportingFlow.revision = (reportingFlow.revision == null) ? 1L : reportingFlow.revision + 1;
    reportingFlow.update();

    ReportingFlowRevision reportingFlowRevision = getRevision(reportingFlow);
    reportingFlowRevision.persist();
  }

  @WithSpan(kind = SERVER)
  public void confirm(String id) {
    log.debugf("Confirm reporting flow");
    Instant now = Instant.now();

    ReportingFlow reportingFlow = fetch(id, ReportingFlow.class);

    reportingFlow.updated = now;
    reportingFlow.status = ReportingFlowStatusEnum.CONFIRM;
    reportingFlow.revision = (reportingFlow.revision == null) ? 1L : reportingFlow.revision + 1;
    reportingFlow.update();

    ReportingFlowRevision reportingFlowRevision = getRevision(reportingFlow);
    reportingFlowRevision.persist();
  }

  @WithSpan(kind = SERVER)
  public void delete(String id) {
    log.debugf("Delete reporting flow");
    Instant now = Instant.now();

    ReportingFlow reportingFlow = fetch(id, ReportingFlow.class);

    reportingFlow.updated = now;
    reportingFlow.status = ReportingFlowStatusEnum.DELETE;
    reportingFlow.revision = (reportingFlow.revision == null) ? 1L : reportingFlow.revision + 1;
    reportingFlow.update();

    ReportingFlowRevision reportingFlowRevision = getRevision(reportingFlow);
    reportingFlowRevision.persist();
  }

  @WithSpan(kind = SERVER)
  public ReportingFlowGetDto findById(String id) {
    log.debugf("Get data from DB");

    ReportingFlowNoPayment reportingFlowNoPayment = fetch(id, ReportingFlowNoPayment.class);

    return mapper.toReportingFlowGetDto(reportingFlowNoPayment);
  }

  @WithSpan(kind = SERVER)
  public ReportingFlowGetPaymentDto findPaymentById(String id, int pageNumber, int pageSize) {
    log.debugf("Get data from DB");

    ReportingFlowOnlyPayment reportingFlowOnlyPayment = fetchSlicePayment(id, pageNumber, pageSize);

    long count = reportingFlowOnlyPayment.count;
    int totPage = (int) Math.ceil(count / (double) pageSize);

    return ReportingFlowGetPaymentDto.builder()
        .metadata(
            MetadataDto.builder()
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .totPage(totPage)
                .build())
        .count(count)
        .sum(reportingFlowOnlyPayment.sum)
        .data(mapper.toPagamentoDtos(reportingFlowOnlyPayment.payments))
        .build();
  }

  @WithSpan(kind = SERVER)
  public ReportingFlowByIdEcDto findByIdEc(
      String idEc, String idPsp, int pageNumber, int pageSize) {
    log.debugf("Get all data from DB");

    Page page = Page.of(pageNumber - 1, pageSize);
    Sort sort = getSort(List.of("_id,asc"));

    PanacheQuery<ReportingFlow> reportingFlowPanacheQuery;
    if (idPsp == null || idPsp.isBlank()) {
      reportingFlowPanacheQuery = ReportingFlow.find("receiver.idEc", sort, idEc);
    } else {
      reportingFlowPanacheQuery =
          ReportingFlow.find("receiver.idEc = ?1 and sender.idPsp = ?2", sort, idEc, idPsp);
    }
    PanacheQuery<ReportingFlowId> reportingFlowIdPanacheQuery =
        reportingFlowPanacheQuery.page(page).project(ReportingFlowId.class);
    List<ReportingFlowId> reportingFlowIds = reportingFlowIdPanacheQuery.list();

    int totPage = reportingFlowIdPanacheQuery.pageCount();
    long countReportingFlow = reportingFlowIdPanacheQuery.count();

    return ReportingFlowByIdEcDto.builder()
        .metadata(
            MetadataDto.builder()
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .totPage(totPage)
                .build())
        .count(countReportingFlow)
        .data(reportingFlowIds.stream().map(rf -> rf.id.toString()).toList())
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

  private ObjectId getObjectId(String id) {
    if (!ObjectId.isValid(id)) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_ID_INVALID, id);
    }
    return new ObjectId(id);
  }

  private <T> T fetch(String id, Class<T> clazz) {
    ObjectId objectId = getObjectId(id);
    Optional<T> reportingFlowOptional =
        ReportingFlow.find("_id", objectId).project(clazz).firstResultOptional();
    return reportingFlowOptional.orElseThrow(
        () -> new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, id));
  }

  private ReportingFlowOnlyPayment fetchSlicePayment(String id, long pageNumber, long pageSize) {
    ObjectId objectId = getObjectId(id);
    long skip = (pageNumber - 1) * pageSize;
    List<Document> aggregate =
        Arrays.asList(
            new Document("$match", new Document("_id", objectId)),
            new Document(
                "$project",
                new Document(
                    "payments",
                    new Document(
                        "$sortArray",
                        new Document(
                                "input",
                                new Document("$ifNull", Arrays.asList("$payments", List.of())))
                            .append(
                                "sortBy", new Document("identificativoUnivocoVersamento", 1L))))),
            new Document(
                "$addFields",
                new Document("count", new Document("$size", "$payments"))
                    .append(
                        "sum",
                        new Document(
                            "$toDecimal", new Document("$sum", "$payments.singoloImportoPagato")))),
            new Document(
                "$project",
                new Document("count", 1L)
                    .append("sum", 1L)
                    .append(
                        "payments",
                        new Document("$slice", Arrays.asList("$payments", skip, pageSize)))));

    ReportingFlowOnlyPayment reportingFlowOnlyPayment =
        ReportingFlow.mongoCollection()
            .aggregate(aggregate, ReportingFlowOnlyPayment.class)
            .first();

    return Optional.ofNullable(reportingFlowOnlyPayment)
        .orElseThrow(() -> new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND, id));
  }

  private ReportingFlowRevision getRevision(ReportingFlow reportingFlow) {
    ReportingFlowRevision reportingFlowRevision = mapper.toReportingFlowRevision(reportingFlow);
    reportingFlowRevision.reportingFlowId = reportingFlowRevision.id;
    reportingFlowRevision.id = null;
    return reportingFlowRevision;
  }
}
