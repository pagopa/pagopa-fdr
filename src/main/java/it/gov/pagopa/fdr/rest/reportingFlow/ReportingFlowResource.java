package it.gov.pagopa.fdr.rest.reportingFlow;

import it.gov.pagopa.fdr.rest.reportingFlow.mapper.ReportingFlowDtoServiceMapper;
import it.gov.pagopa.fdr.rest.reportingFlow.request.AddPaymentRequest;
import it.gov.pagopa.fdr.rest.reportingFlow.request.CreateRequest;
import it.gov.pagopa.fdr.rest.reportingFlow.response.CreateResponse;
import it.gov.pagopa.fdr.rest.reportingFlow.response.GetAllResponse;
import it.gov.pagopa.fdr.rest.reportingFlow.response.GetIdResponse;
import it.gov.pagopa.fdr.rest.reportingFlow.response.GetPaymentResponse;
import it.gov.pagopa.fdr.rest.reportingFlow.validation.ReportingFlowValidationService;
import it.gov.pagopa.fdr.service.reportingFlow.ReportingFlowService;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowByIdEcDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowGetDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowGetPaymentDto;
import java.util.List;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
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

  @Operation(summary = "Create reporting flow")
  @RequestBody(content = @Content(schema = @Schema(implementation = CreateRequest.class)))
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/BadRequest"),
        @APIResponse(ref = "#/components/responses/ReportingFlowNotFound"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = CreateResponse.class)))
      })
  @POST
  public CreateResponse create(
      @NotNull(message = "reporting-flow.create.req.notNull") @Valid CreateRequest createRequest) {

    log.infof("Create reporting [%s]", createRequest.getReportingFlow());

    // validation
    validator.validateCreate(createRequest);

    // save on DB
    String id = service.save(mapper.toReportingFlowDto(createRequest));

    return CreateResponse.builder().id(id).build();
  }

  @Operation(summary = "Add payments to reporting flow")
  @RequestBody(content = @Content(schema = @Schema(implementation = AddPaymentRequest.class)))
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/BadRequest"),
        @APIResponse(ref = "#/components/responses/ReportingFlowNotFound"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Response.class)))
      })
  @PUT
  @Path("/{id}/add-payment")
  public Response addPayment(
      @PathParam("id") String id,
      @NotNull(message = "reporting-flow.create.req.notNull") @Valid
          AddPaymentRequest addPaymentRequest) {

    log.infof("Add payment to reporting flow [%s]", id);

    // validation
    validator.validateAddPayment(addPaymentRequest);

    // save on DB
    service.addPayment(id, mapper.toAddPaymentDto(addPaymentRequest));

    return Response.ok().build();
  }

  @Operation(summary = "Get reporting flow")
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/BadRequest"),
        @APIResponse(ref = "#/components/responses/ReportingFlowNotFound"),
        @APIResponse(ref = "#/components/responses/ReportingFlowIdInvalid"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GetIdResponse.class)))
      })
  @GET
  @Path("/{id}")
  public GetIdResponse get(@PathParam("id") String id) {
    log.infof("Get reporting by id [%s]", id);

    // validation
    validator.validateGet(id);

    // get from db
    ReportingFlowGetDto byId = service.findById(id);

    return mapper.toGetIdResponse(byId);
  }

  @Operation(summary = "Get reporting flow")
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/BadRequest"),
        @APIResponse(ref = "#/components/responses/ReportingFlowNotFound"),
        @APIResponse(ref = "#/components/responses/ReportingFlowIdInvalid"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GetPaymentResponse.class)))
      })
  @GET
  @Path("/{id}/payment")
  public GetPaymentResponse getPayment(
      @PathParam("id") String id,
      @QueryParam("sort") @DefaultValue("_id,asc") List<String> sortColumn,
      @QueryParam("page")
          @DefaultValue("1")
          @Min(
              value = 1,
              message = "reporting-flow.getAllByEc.pageNumber.min|${validatedValue}|{value}")
          int pageNumber,
      @QueryParam("size")
          @DefaultValue("50")
          @Min(
              value = 1,
              message = "reporting-flow.getAllByEc.pageSize.min|${validatedValue}|{value}")
          int pageSize) {
    log.infof("Get reporting by id [%s]", id);

    // validation
    validator.validateGet(id);

    // get from db
    ReportingFlowGetPaymentDto byId = service.findPaymentById(id, pageNumber, pageSize, sortColumn);

    return mapper.toGetPaymentResponse(byId);
  }

  @Operation(summary = "Get reporting flow")
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/BadRequest"),
        @APIResponse(ref = "#/components/responses/ReportingFlowNotFound"),
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
  public GetAllResponse getAllByEc(
      @PathParam("idEc") String idEc,
      @QueryParam("idPsp") String idPsp,
      @QueryParam("sort") @DefaultValue("_id,asc") List<String> sortColumn,
      @QueryParam("page")
          @DefaultValue("1")
          @Min(
              value = 1,
              message = "reporting-flow.getAllByEc.pageNumber.min|${validatedValue}|{value}")
          int pageNumber,
      @QueryParam("size")
          @DefaultValue("50")
          @Min(
              value = 1,
              message = "reporting-flow.getAllByEc.pageSize.min|${validatedValue}|{value}")
          int pageSize) {

    log.infof("Get all reporting by idEc [%s]", idEc);

    // validation
    validator.validateGetAllByEc(idEc);

    // get from db
    ReportingFlowByIdEcDto reportingFlowByIdEcDto =
        service.findByIdEc(idEc, idPsp, pageNumber, pageSize, sortColumn);

    return mapper.toGetAllResponse(reportingFlowByIdEcDto);
  }

  //  @POST
  //  @Path("/p/{id}/payments/add")
  //  public ModifyPaymentResponse paymentAdd(
  //      @PathParam("id") Long id,
  //      @NotNull(message = "reporting-flow.modify.req.not-null") @Valid
  //          ModifyPaymentRequest modifyPaymentRequest) {
  //    return ModifyPaymentResponse.builder().id("").build();
  //  }
  //
  //  @PUT
  //  @Path("/p/{id}/payments/update")
  //  public ModifyPaymentResponse paymentUpdate(
  //      @PathParam("id") Long id,
  //      @NotNull(message = "reporting-flow.modify.req.not-null") @Valid
  //          ModifyPaymentRequest modifyPaymentRequest) {
  //    return ModifyPaymentResponse.builder().id("").build();
  //  }
  //
  //  @DELETE
  //  @Path("/p/{id}/payments/delete")
  //  public ModifyPaymentResponse paymentDelete(
  //      @PathParam("id") Long id,
  //      @NotNull(message = "reporting-flow.modify.req.not-null") @Valid
  //          ModifyPaymentRequest modifyPaymentRequest) {
  //    return ModifyPaymentResponse.builder().id("").build();
  //  }
  //
  //  @DELETE
  //  @Path("/p/{id}/delete")
  //  public DeleteResponse delete(
  //      @PathParam("id") Long id,
  //      @NotNull(message = "reporting-flow.delete.req.not-null") @Valid DeleteRequest
  // deleteRequest) {
  //    return DeleteResponse.builder().id("").build();
  //  }
  //
  //  @PUT
  //  @Path("/p/{id}/confirm")
  //  public ConfirmResponse confirm(
  //      @PathParam("id") Long id,
  //      @NotNull(message = "reporting-flow.confirm.req.not-null") @Valid
  //          ConfirmRequest confirmRequest) {
  //    return ConfirmResponse.builder().id("").build();
  //  }
}
