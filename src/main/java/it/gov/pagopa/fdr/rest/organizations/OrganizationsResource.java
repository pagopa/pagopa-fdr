package it.gov.pagopa.fdr.rest.organizations;

import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.rest.model.GenericResponse;
import it.gov.pagopa.fdr.rest.organizations.mapper.OrganizationsResourceServiceMapper;
import it.gov.pagopa.fdr.rest.organizations.response.GetAllResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetIdResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetPaymentResponse;
import it.gov.pagopa.fdr.rest.organizations.validation.OrganizationsValidationService;
import it.gov.pagopa.fdr.service.organizations.OrganizationsService;
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

@Tag(name = "Organizations", description = "Get reporting flow operations")
@Path("/organizations/{ec}/flows")
@Consumes("application/json")
@Produces("application/json")
public class OrganizationsResource {

  @Inject Config config;
  @Inject Logger log;

  @Inject OrganizationsValidationService validator;

  @Inject OrganizationsResourceServiceMapper mapper;

  @Inject OrganizationsService service;

  // TODO in tutte le API bisogna fare dei check per identificare che il chiamante sia esattamente
  // id messo nella richiesta o ci pensa nuova connettività/APIM??
  /*TODO in tutte queste API vanno replicati tutti i controlli come nel vecchio? Ora ci sono solo
   * i controlli sui campi in input altrimenti bisogna caricare tutto il resto dei campi utili solo a bloccare o no le richieste.
   * Ha senso?
   * */
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
                    schema = @Schema(implementation = GetAllResponse.class)))
      })
  @GET
  public GetAllResponse getAllPublishedFlow(
      @PathParam("ec") @Pattern(regexp = "^\\w{1,35}$") String ec,
      @QueryParam("idPsp") @Pattern(regexp = "^\\w{1,35}$") String idPsp,
      @QueryParam("page") @DefaultValue("1") @Min(value = 1) long pageNumber,
      @QueryParam("size") @DefaultValue("50") @Min(value = 1) long pageSize) {

    // TODO aggiungere date from to per leggere al massimo n (as is 30) giorni con limite di 90 e
    // meglio mettere TTL sulla collections a 90 giorni

    // TODO si potrebbe aggiungere un API per metterle in stato già letto da PA in modo da non
    // ripresentarle ogni volta

    log.infof(
        "Get id of reporting flow by idEc [%s], idPsp [%s] - page: [%s], pageSize: [%s]",
        ec, idPsp, pageNumber, pageSize);

    ConfigDataV1 configData = config.getClonedCache();
    // validation
    validator.validateGetAllByEc(ec, idPsp, configData);

    // get from db
    return mapper.toGetAllResponse(service.findByIdEc(ec, idPsp, pageNumber, pageSize));
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
  @Path("/{fdr}/psps/{psp}")
  public GetIdResponse getReportingFlow(
      @PathParam("ec") String ec, @PathParam("fdr") String fdr, @PathParam("psp") String psp) {
    log.infof("Get reporting flow by reportingFlowName [%s]", fdr);

    // validation
    validator.validateGet(fdr);

    // get from db
    return mapper.toGetIdResponse(service.findByReportingFlowName(fdr, psp));
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
  @Path("/{fdr}/psps/{psp}/payments")
  public GetPaymentResponse getReportingFlowPayments(
      @PathParam("ec") String ec,
      @PathParam("fdr") String fdr,
      @PathParam("psp") String psp,
      @QueryParam("page") @DefaultValue("1") @Min(value = 1) long pageNumber,
      @QueryParam("size") @DefaultValue("50") @Min(value = 1) long pageSize) {
    log.infof(
        "Get payment of reporting flow by id [%s] - page: [%s], pageSize: [%s]",
        fdr, pageNumber, pageSize);

    // validation
    validator.validateGetPayment(fdr);

    // get from db
    return mapper.toGetPaymentResponse(
        service.findPaymentByReportingFlowName(fdr, psp, pageNumber, pageSize));
  }

  @Operation(
      summary = "Change read flag of reporting flow",
      description = "Change read flag of reporting flow")
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
  @Path("/{fdr}/psps/{psp}/read")
  public GenericResponse changeReadFlag(
      @PathParam("ec") String ec, @PathParam("fdr") String fdr, @PathParam("psp") String psp) {
    log.infof("Get payment of reporting flow by id [%s]", fdr);

    // validation
    validator.validateChangeReadFlag(fdr);

    // change on DB
    service.changeReadFlag(ec, psp, fdr);

    // get from db
    return GenericResponse.builder().message(String.format("Flow [%s] read", fdr)).build();
  }
}
