package it.gov.pagopa.fdr.rest.reportingFlow;

import it.gov.pagopa.fdr.rest.reportingFlow.mapper.ReportingFlowDtoServiceMapper;
import it.gov.pagopa.fdr.rest.reportingFlow.request.AddPaymentRequest;
import it.gov.pagopa.fdr.rest.reportingFlow.request.CreateRequest;
import it.gov.pagopa.fdr.rest.reportingFlow.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.rest.reportingFlow.response.GetAllResponse;
import it.gov.pagopa.fdr.rest.reportingFlow.response.GetIdResponse;
import it.gov.pagopa.fdr.rest.reportingFlow.response.GetPaymentResponse;
import it.gov.pagopa.fdr.rest.reportingFlow.validation.ReportingFlowValidationService;
import it.gov.pagopa.fdr.service.reportingFlow.ReportingFlowService;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

@Tag(name = "Reporting Flow", description = "Reporting Flow operations")
@Path("/reporting-flow")
@Consumes("application/json")
@Produces("application/json")
public class ReportingFlowResource {

  @Inject Logger log;

  @Inject ReportingFlowValidationService validator;

  @Inject ReportingFlowDtoServiceMapper mapper;

  @Inject ReportingFlowService service;

  @Operation(
      summary = "Create reporting flow",
      description = "Create new reporting flow and return id for add payment")
  @RequestBody(content = @Content(schema = @Schema(implementation = CreateRequest.class)))
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/ValidationBadRequest"),
        @APIResponse(ref = "#/components/responses/AppException400"),
        @APIResponse(ref = "#/components/responses/AppException404"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Response.class)))
      })
  @POST
  public Response createReportingFlow(@NotNull @Valid CreateRequest createRequest) {

    log.infof("Create reporting flow [%s]", createRequest.getReportingFlowName());

    // validation
    validator.validateCreate(createRequest);

    // save on DB
    service.save(mapper.toReportingFlowDto(createRequest));

    return Response.ok().build();
  }

  @Operation(
      summary = "Add payments to reporting flow",
      description = "Add payments to reporting flow")
  @RequestBody(content = @Content(schema = @Schema(implementation = AddPaymentRequest.class)))
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/ValidationBadRequest"),
        @APIResponse(ref = "#/components/responses/AppException400"),
        @APIResponse(ref = "#/components/responses/AppException404"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Response.class)))
      })
  @PUT
  @Path("/{reportingFlowName}/add-payment")
  public Response addPaymentToReportingFlow(
      @PathParam("reportingFlowName") String reportingFlowName,
      @NotNull @Valid AddPaymentRequest addPaymentRequest) {

    log.infof("Add payment to reporting flow [%s]", reportingFlowName);

    // validation
    validator.validateAddPayment(addPaymentRequest);

    // save on DB
    service.addPayment(reportingFlowName, mapper.toAddPaymentDto(addPaymentRequest));

    return Response.ok().build();
  }

  @Operation(
      summary = "Delete payments to reporting flow",
      description = "Delete payments to reporting flow")
  @RequestBody(content = @Content(schema = @Schema(implementation = DeletePaymentRequest.class)))
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/ValidationBadRequest"),
        @APIResponse(ref = "#/components/responses/AppException400"),
        @APIResponse(ref = "#/components/responses/AppException404"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Response.class)))
      })
  @DELETE
  @Path("/{reportingFlowName}/delete-payment")
  public Response deletePaymentToReportingFlow(
      @PathParam("reportingFlowName") String reportingFlowName,
      @NotNull @Valid DeletePaymentRequest deletePaymentRequest) {

    log.infof("Delete payment to reporting flow [%s]", reportingFlowName);

    // validation
    validator.validateDeletePayment(deletePaymentRequest);

    // save on DB
    service.deletePayment(reportingFlowName, mapper.toDeletePaymentDto(deletePaymentRequest));

    return Response.ok().build();
  }

  @Operation(summary = "Confirm reporting flow", description = "Confirm reporting flow")
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/ValidationBadRequest"),
        @APIResponse(ref = "#/components/responses/AppException400"),
        @APIResponse(ref = "#/components/responses/AppException404"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Response.class)))
      })
  @PUT
  @Path("/{reportingFlowName}/confirm")
  public Response confirmReportingFlow(@PathParam("reportingFlowName") String reportingFlowName) {

    log.infof("Confirm reporting flow [%s]", reportingFlowName);

    // validation
    validator.validateConfirm(reportingFlowName);

    // save on DB
    service.confirmByReportingFlowName(reportingFlowName);

    return Response.ok().build();
  }

  @Operation(summary = "Delete reporting flow", description = "Delete reporting flow")
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/ValidationBadRequest"),
        @APIResponse(ref = "#/components/responses/AppException400"),
        @APIResponse(ref = "#/components/responses/AppException404"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Response.class)))
      })
  @DELETE
  @Path("/{reportingFlowName}")
  public Response deleteReportingFlow(@PathParam("reportingFlowName") String reportingFlowName) {

    log.infof("Delete reporting flow [%s]", reportingFlowName);

    // validation
    validator.validateDelete(reportingFlowName);

    // save on DB
    service.deleteByReportingFlowName(reportingFlowName);

    return Response.ok().build();
  }

  @Operation(
      summary = "Get reporting flow",
      description = "Get reporting flow by id but not payments")
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/ValidationBadRequest"),
        @APIResponse(ref = "#/components/responses/AppException400"),
        @APIResponse(ref = "#/components/responses/AppException404"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GetIdResponse.class)))
      })
  @GET
  @Path("/{reportingFlowName}")
  public GetIdResponse getReportingFlowNotPayments(
      @PathParam("reportingFlowName") String reportingFlowName) {
    log.infof("Get reporting flow by reportingFlowName [%s]", reportingFlowName);

    // validation
    validator.validateGet(reportingFlowName);

    // get from db
    return mapper.toGetIdResponse(service.findByReportingFlowName(reportingFlowName));
  }

  @Operation(
      summary = "Get payments of reporting flow",
      description = "Get only payments of reporting flow by id paginated")
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/ValidationBadRequest"),
        @APIResponse(ref = "#/components/responses/AppException400"),
        @APIResponse(ref = "#/components/responses/AppException404"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GetPaymentResponse.class)))
      })
  @GET
  @Path("/{reportingFlowName}/payment")
  public GetPaymentResponse getReportingFlowOnlyPayments(
      @PathParam("reportingFlowName") String reportingFlowName,
      @QueryParam("page") @DefaultValue("1") @Min(value = 1) long pageNumber,
      @QueryParam("size") @DefaultValue("50") @Min(value = 1) long pageSize) {
    log.infof(
        "Get payment of reporting flow by id [%s] - page: [%s], pageSize: [%s]",
        reportingFlowName, pageNumber, pageSize);

    // validation
    validator.validateGet(reportingFlowName);

    // get from db
    return mapper.toGetPaymentResponse(
        service.findPaymentByReportingFlowName(reportingFlowName, pageNumber, pageSize));
  }

  @Operation(
      summary = "Get id of reporting flow",
      description = "Get id of reporting flow by idEc and idPsp(optional param)")
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/ValidationBadRequest"),
        @APIResponse(ref = "#/components/responses/AppException400"),
        @APIResponse(ref = "#/components/responses/AppException404"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GetAllResponse.class)))
      })
  @GET
  @Path("/all-id-by-ec/{idEc}")
  public GetAllResponse getReportingFlowOnlyId(
      @PathParam("idEc") String idEc,
      @QueryParam("idPsp") @Pattern(regexp = "^\\w{1,35}$") String idPsp,
      @QueryParam("page") @DefaultValue("1") @Min(value = 1) long pageNumber,
      @QueryParam("size") @DefaultValue("50") @Min(value = 1) long pageSize) {

    log.infof(
        "Get id of reporting flow by idEc [%s], idPsp [%s] - page: [%s], pageSize: [%s]",
        idEc, idPsp, pageNumber, pageSize);

    // validation
    validator.validateGetAllByEc(idEc, idPsp);

    // get from db
    return mapper.toGetAllResponse(service.findByIdEc(idEc, idPsp, pageNumber, pageSize));
  }
}
