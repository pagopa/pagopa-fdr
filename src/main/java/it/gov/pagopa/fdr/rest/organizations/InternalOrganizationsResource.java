package it.gov.pagopa.fdr.rest.organizations;

import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.rest.model.GenericResponse;
import it.gov.pagopa.fdr.rest.organizations.mapper.OrganizationsResourceServiceMapper;
import it.gov.pagopa.fdr.rest.organizations.response.GetAllInternalResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetIdResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetPaymentResponse;
import it.gov.pagopa.fdr.rest.organizations.validation.InternalOrganizationsValidationService;
import it.gov.pagopa.fdr.service.organizations.InternalOrganizationsService;
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

@Tag(name = "Internal Organizations", description = "Get reporting flow operations")
@Path("/internal/history/organizations/ndp/flows")
@Consumes("application/json")
@Produces("application/json")
public class InternalOrganizationsResource {

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
      @QueryParam("flowName") @Pattern(regexp = "[a-zA-Z0-9\\-_]{1,35}") String flowName,
      @QueryParam("idPsp") @Pattern(regexp = "^\\w{1,35}$") String idPsp,
      @QueryParam("page") @DefaultValue("1") @Min(value = 1) long pageNumber,
      @QueryParam("size") @DefaultValue("50") @Min(value = 1) long pageSize) {

    log.infof(
        "Get id of reporting flow, idPsp [%s] , flowName [%s] - page: [%s], pageSize: [%s]",
        idPsp, flowName, pageNumber, pageSize);

    ConfigDataV1 configData = config.getClonedCache();
    // validation
    internalValidator.validateGetAllInternal(idPsp, configData);

    // get from db
    return mapper.toGetAllInternalResponse(
        internalService.findByInternals(flowName, idPsp, pageNumber, pageSize));
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
    log.infof("Get reporting flow by reportingFlowName [%s] for ndp", fdr);

    // validation
    internalValidator.validateGetInternal(fdr);

    // get from db
    return mapper.toGetIdResponse(internalService.findByReportingFlowNameInternals(fdr, rev, psp));
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
    log.infof(
        "Get payment of reporting flow by id [%s] - page: [%s], pageSize: [%s]",
        fdr, pageNumber, pageSize);

    // validation
    internalValidator.validateGetPaymentInternal(fdr);

    // get from db
    return mapper.toGetPaymentResponse(
        internalService.findPaymentByReportingFlowNameInternals(
            fdr, rev, psp, pageNumber, pageSize));
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
    log.infof("Get payment of reporting flow by id [%s]", fdr);

    // validation
    internalValidator.validateChangeInternalReadFlag(fdr);

    // change on DB
    internalService.changeInternalReadFlag(fdr, rev, psp);

    // get from db
    return GenericResponse.builder().message(String.format("Flow [%s] internal read", fdr)).build();
  }
}
