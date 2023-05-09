package it.gov.pagopa.fdr.rest.internal;

import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.rest.internal.mapper.InternalsResourceServiceMapper;
import it.gov.pagopa.fdr.rest.internal.response.InternalsGetAllResponse;
import it.gov.pagopa.fdr.rest.internal.validation.InternalsValidationService;
import it.gov.pagopa.fdr.service.internals.InternalsService;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

@Tag(name = "Internals", description = "Get reporting flow operations")
@Path("/internals/{app}/flows")
@Consumes("application/json")
@Produces("application/json")
public class InternalsResource {

  @Inject Config config;
  @Inject Logger log;

  @Inject InternalsValidationService validator;

  @Inject InternalsResourceServiceMapper mapper;

  @Inject InternalsService service;

  @Operation(
      summary = "Get all published reporting flow",
      description = "Get all published reporting flow by ec and idPsp(optional param)")
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
                    schema = @Schema(implementation = InternalsGetAllResponse.class)))
      })
  @GET
  public InternalsGetAllResponse getAllPublishedFlow(
      @PathParam("app") String app,
      @QueryParam("page") @DefaultValue("1") @Min(value = 1) long pageNumber,
      @QueryParam("size") @DefaultValue("50") @Min(value = 1) long pageSize) {

    log.infof(
        "Get id of reporting flow for internal app [%s] - page: [%s], pageSize: [%s]",
        app, pageNumber, pageSize);

    // validation
    validator.validateGetAllByInternal(app);

    // get from db
    return mapper.toGetAllResponse(service.findByInternals(app, pageNumber, pageSize));
  }
}
