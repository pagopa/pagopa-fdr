package it.gov.pagopa.fdr.controller.model.flow.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.fdr.controller.model.flow.Receiver;
import it.gov.pagopa.fdr.controller.model.flow.Sender;
import it.gov.pagopa.fdr.util.constant.AppConstant;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@Jacksonized
public class CreateFlowRequest {

  @NotNull
  @Pattern(regexp = "[a-zA-Z0-9\\-_]{1,35}")
  @Schema(
      example = "2016-08-16pspTest-1178",
      description = "[XML NodoInviaFlussoRendicontazione]=[identificativoFlusso]")
  @JsonProperty(AppConstant.FDR)
  private String fdr;

  @NotNull
  @Schema(
      example = "2023-04-05T09:21:37.810000Z",
      description = "[XML NodoInviaFlussoRendicontazione]=[dataOraFlusso]")
  private Instant fdrDate;

  @NotNull @Valid private Sender sender;

  @NotNull @Valid private Receiver receiver;

  @NotNull
  @Pattern(regexp = "^(.{1,35})$")
  @Schema(
      example = "SEPA - Bonifico xzy",
      description = "[XML FlussoRiversamento]=[identificativoUnivocoRegolamento]")
  private String regulation;

  @NotNull
  @Schema(
      example = "2023-04-03T12:00:30.900000Z",
      description = "[XML FlussoRiversamento]=[dataRegolamento]")
  private Instant regulationDate;

  @Schema(
      example = "UNCRITMMXXX",
      description = "[XML FlussoRiversamento]=[codiceBicBancaDiRiversamento]")
  // TODO non dovrebbe essere 5 numerici?
  @Pattern(regexp = "^(.{1,35})$")
  private String bicCodePouringBank;

  @NotNull
  @Min(value = 1)
  @Schema(example = "1", description = "[XML FlussoRiversamento]=[numeroTotalePagamenti]")
  private Long totPayments;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = false)
  @Digits(integer = Integer.MAX_VALUE, fraction = 2)
  @Schema(example = "0.01", description = "[XML FlussoRiversamento]=[importoTotalePagamenti]")
  private Double sumPayments;
}
