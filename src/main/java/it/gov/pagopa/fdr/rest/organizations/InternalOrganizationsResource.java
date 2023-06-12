package it.gov.pagopa.fdr.rest.organizations;

import static it.gov.pagopa.fdr.util.MDCKeys.ACTION;
import static it.gov.pagopa.fdr.util.MDCKeys.EC_ID;
import static it.gov.pagopa.fdr.util.MDCKeys.FLOW_NAME;
import static it.gov.pagopa.fdr.util.MDCKeys.NDP;
import static it.gov.pagopa.fdr.util.MDCKeys.PSP_ID;

import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.rest.model.GenericResponse;
import it.gov.pagopa.fdr.rest.organizations.mapper.OrganizationsResourceServiceMapper;
import it.gov.pagopa.fdr.rest.organizations.response.GetAllInternalResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetIdResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetPaymentResponse;
import it.gov.pagopa.fdr.rest.organizations.validation.InternalOrganizationsValidationService;
import it.gov.pagopa.fdr.service.dto.ReportingFlowGetDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowGetPaymentDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowInternalDto;
import it.gov.pagopa.fdr.service.organizations.InternalOrganizationsService;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;
import org.slf4j.MDC;

@Tag(name = "Internal Organizations", description = "Get reporting flow operations")
@Path("/internal/history/organizations/ndp/flows")
@Consumes("application/json")
@Produces("application/json")
public class InternalOrganizationsResource {

  private static final String GET_ALL_PUBLISHED_FLOW = "getAllPublishedFlowInternal";
  private static final String GET_REPORTING_FLOW = "getReportingFlowInternal";
  private static final String GET_REPORTING_FLOW_PAYMENTS = "getReportingFlowPaymentsInternal";
  private static final String CHANGE_READ_FLAG = "changeReadFlagInternal";

  @Inject Config config;
  @Inject Logger log;

  @Inject InternalOrganizationsValidationService internalValidator;

  @Inject OrganizationsResourceServiceMapper mapper;

  @Inject InternalOrganizationsService internalService;

  @Operation(
      summary = "Get all published reporting flow",
      description = "Get all published reporting flow by ec and idPsp(optional param)")
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/AppException400"),
        @APIResponse(ref = "#/components/responses/AppException404"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GetAllInternalResponse.class)))
      })
  @GET
  public GetAllInternalResponse getAllPublishedFlow(
      @QueryParam("idPsp") @Pattern(regexp = "^\\w{1,35}$") String idPsp,
      @QueryParam("page") @DefaultValue("1") @Min(value = 1) long pageNumber,
      @QueryParam("size") @DefaultValue("50") @Min(value = 1) long pageSize) {
    MDC.put(ACTION, GET_ALL_PUBLISHED_FLOW);
    MDC.put(EC_ID, NDP);
    MDC.put(PSP_ID, idPsp);

    log.infof(
        AppMessageUtil.logProcess("%s with idPsp:[%s] - page:[%s], pageSize:[%s]"),
        GET_ALL_PUBLISHED_FLOW,
        idPsp,
        pageNumber,
        pageSize);

    ConfigDataV1 configData = config.getClonedCache();
    // validation
    internalValidator.validateGetAllInternal(GET_ALL_PUBLISHED_FLOW, idPsp, configData);

    // get from db
    ReportingFlowInternalDto reportingFlowInternalDto =
        internalService.findByInternals(GET_ALL_PUBLISHED_FLOW, idPsp, pageNumber, pageSize);

    return mapper.toGetAllInternalResponse(reportingFlowInternalDto);
  }

  @Operation(
      summary = "Get reporting flow",
      description = "Get reporting flow by id but not payments")
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
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
  @Path("/{fdr}/rev/{rev}/psps/{psp}")
  public GetIdResponse getReportingFlow(
      @PathParam("fdr") String fdr, @PathParam("rev") Long rev, @PathParam("psp") String psp) {
    MDC.put(ACTION, GET_REPORTING_FLOW);
    MDC.put(EC_ID, NDP);
    MDC.put(FLOW_NAME, fdr);
    MDC.put(PSP_ID, psp);

    log.infof(AppMessageUtil.logProcess("%s with id:[%s]"), GET_REPORTING_FLOW, fdr);

    ConfigDataV1 configData = config.getClonedCache();
    // validation
    internalValidator.validateGetInternal(GET_REPORTING_FLOW, fdr, psp, configData);

    // get from db
    ReportingFlowGetDto flowNameInternals =
        internalService.findByReportingFlowNameInternals(GET_REPORTING_FLOW, fdr, rev, psp);

    return mapper.toGetIdResponse(flowNameInternals);
  }

  @Operation(
      summary = "Get payments of reporting flow",
      description = "Get only payments of reporting flow by id paginated")
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
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
  @Path("/{fdr}/rev/{rev}/psps/{psp}/payments")
  public GetPaymentResponse getReportingFlowPayments(
      @PathParam("fdr") String fdr,
      @PathParam("rev") Long rev,
      @PathParam("psp") String psp,
      @QueryParam("page") @DefaultValue("1") @Min(value = 1) long pageNumber,
      @QueryParam("size") @DefaultValue("50") @Min(value = 1) long pageSize) {
    MDC.put(ACTION, GET_REPORTING_FLOW_PAYMENTS);
    MDC.put(EC_ID, NDP);
    MDC.put(FLOW_NAME, fdr);
    MDC.put(PSP_ID, psp);

    log.infof(
        AppMessageUtil.logProcess("%s with id:[%s] - page:[%s], pageSize:[%s]"),
        GET_REPORTING_FLOW_PAYMENTS,
        fdr,
        pageNumber,
        pageSize);

    ConfigDataV1 configData = config.getClonedCache();
    // validation
    internalValidator.validateGetPaymentInternal(GET_REPORTING_FLOW_PAYMENTS, fdr, psp, configData);

    // get from db
    ReportingFlowGetPaymentDto flowNameInternals =
        internalService.findPaymentByReportingFlowNameInternals(
            GET_REPORTING_FLOW_PAYMENTS, fdr, rev, psp, pageNumber, pageSize);

    return mapper.toGetPaymentResponse(flowNameInternals);
  }

  @Operation(
      summary = "Change internal read flag of reporting flow",
      description = "Change internal read flag of reporting flow")
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/AppException400"),
        @APIResponse(ref = "#/components/responses/AppException404"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GenericResponse.class)))
      })
  @PUT
  @Path("/{fdr}/rev/{rev}/psps/{psp}/read")
  public GenericResponse changeInternalReadFlag(
      @PathParam("fdr") String fdr, @PathParam("rev") Long rev, @PathParam("psp") String psp) {
    MDC.put(ACTION, CHANGE_READ_FLAG);
    MDC.put(EC_ID, NDP);
    MDC.put(FLOW_NAME, fdr);
    MDC.put(PSP_ID, psp);

    log.infof(AppMessageUtil.logProcess("%s with id:[%s]"), CHANGE_READ_FLAG, fdr);

    ConfigDataV1 configData = config.getClonedCache();
    // validation
    internalValidator.validateChangeInternalReadFlag(CHANGE_READ_FLAG, fdr, psp, configData);

    // change on DB
    internalService.changeInternalReadFlag(CHANGE_READ_FLAG, fdr, rev, psp);

    return GenericResponse.builder().message(String.format("Flow [%s] internal read", fdr)).build();
  }
}
