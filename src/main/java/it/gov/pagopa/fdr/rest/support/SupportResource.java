package it.gov.pagopa.fdr.rest.support;

import static it.gov.pagopa.fdr.util.MDCKeys.ACTION;
import static it.gov.pagopa.fdr.util.MDCKeys.PSP_ID;

import it.gov.pagopa.fdr.rest.support.mapper.SupportResourceServiceMapper;
import it.gov.pagopa.fdr.rest.support.response.FdrByPspIdIuvIurResponse;
import it.gov.pagopa.fdr.service.dto.PaymentGetByPspIdIuvIurDTO;
import it.gov.pagopa.fdr.service.re.model.FdrActionEnum;
import it.gov.pagopa.fdr.service.support.FindPaymentsByPspIdAndIuvIurArgs;
import it.gov.pagopa.fdr.service.support.SupportService;
import it.gov.pagopa.fdr.util.AppConstant;
import it.gov.pagopa.fdr.util.Re;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.time.Instant;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.MDC;

@Tag(name = "Support", description = "Support operations")
@Path("/internal/psps/{" + AppConstant.PSP+ "}/")
@Consumes("application/json")
@Produces("application/json")
public class SupportResource {

  @Inject SupportResourceServiceMapper mapper;
  @Inject SupportService service;

  @Operation(
      operationId = "getByIuv",
      summary = "Get all payments by psp id and iuv",
      description = "Get all payments by psp id and iuv")
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
                    schema = @Schema(implementation = FdrByPspIdIuvIurResponse.class)))
      })
  @GET
  @Path(
      "iuv/{"
      + AppConstant.IUV
      + "}/"
  )
  @Re(action = FdrActionEnum.INTERNAL_GET_ALL_BY_PSP_IUV_FDR)
  public FdrByPspIdIuvIurResponse getByIuv(
      @PathParam(AppConstant.PSP) @Pattern(regexp = "^(.{1,35})$") String pspId,
      @PathParam(AppConstant.IUV) @Pattern(regexp = "^(.{1,35})$") String iuv,
      @QueryParam(AppConstant.CREATED_FROM) Instant createdFrom,
      @QueryParam(AppConstant.CREATED_TO) Instant createdTo,
      @QueryParam(AppConstant.PAGE) @DefaultValue(AppConstant.PAGE_DEAFULT) @Min(value = 1)
          long pageNumber,
      @QueryParam(AppConstant.SIZE) @DefaultValue(AppConstant.SIZE_DEFAULT) @Min(value = 1)
          long pageSize) {
    String action = MDC.get(ACTION);
    MDC.put(PSP_ID, pspId);
    PaymentGetByPspIdIuvIurDTO paymentDtoList=
        service.findPaymentsByPspIdAndIuvIur(
            FindPaymentsByPspIdAndIuvIurArgs.builder()
                .action(action)
                .pspId(pspId)
                .iuv(iuv)
                .iur(null)
                .createdFrom(createdFrom)
                .createdTo(createdTo)
                .pageNumber(pageNumber)
                .pageSize(pageSize).build());
    return FdrByPspIdIuvIurResponse.builder()
        .metadata(mapper.toMetadata(paymentDtoList.getMetadata()))
        .count(paymentDtoList.getCount())
        .data(mapper.toFdrByIuvIurList(paymentDtoList.getData()))
        .build();
  }
  @Operation(
      operationId = "getByIur",
      summary = "Get all payments by psp id and iur",
      description = "Get all payments by psp id and iur")
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
                  schema = @Schema(implementation = FdrByPspIdIuvIurResponse.class)))
      })
  @GET
  @Path(
      "iur/{"
          + AppConstant.IUR
          + "}/"
  )
  @Re(action = FdrActionEnum.INTERNAL_GET_ALL_BY_PSP_IUR_FDR)
  public FdrByPspIdIuvIurResponse getByIur(
      @PathParam(AppConstant.PSP) @Pattern(regexp = "^(.{1,35})$") String pspId,
      @PathParam(AppConstant.IUR) @Pattern(regexp = "^(.{1,35})$") String iur,
      @QueryParam(AppConstant.CREATED_FROM) Instant createdFrom,
      @QueryParam(AppConstant.CREATED_TO) Instant createdTo,
      @QueryParam(AppConstant.PAGE) @DefaultValue(AppConstant.PAGE_DEAFULT) @Min(value = 1)
      long pageNumber,
      @QueryParam(AppConstant.SIZE) @DefaultValue(AppConstant.SIZE_DEFAULT) @Min(value = 1)
      long pageSize) {
    String action = MDC.get(ACTION);
    MDC.put(PSP_ID, pspId);
    PaymentGetByPspIdIuvIurDTO paymentDtoList=
        service.findPaymentsByPspIdAndIuvIur(
            FindPaymentsByPspIdAndIuvIurArgs.builder()
                .action(action)
                .pspId(pspId)
                .iuv(null)
                .iur(iur)
                .createdFrom(createdFrom)
                .createdTo(createdTo)
                .pageNumber(pageNumber)
                .pageSize(pageSize).build());
    return FdrByPspIdIuvIurResponse.builder()
        .metadata(mapper.toMetadata(paymentDtoList.getMetadata()))
        .count(paymentDtoList.getCount())
        .data(mapper.toFdrByIuvIurList(paymentDtoList.getData()))
        .build();
  }
}
