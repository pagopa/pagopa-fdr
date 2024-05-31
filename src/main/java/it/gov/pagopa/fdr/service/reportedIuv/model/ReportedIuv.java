package it.gov.pagopa.fdr.service.reportedIuv.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportedIuv {

  @JsonProperty("IUV")
  private String identificativoUnivocoVersamento;

  @JsonProperty("IUR")
  private String identificativoUnivocoRiscossione;

  @JsonProperty("IMPORTO")
  private BigDecimal singoloImportoPagato;

  @JsonProperty("COD_ESITO")
  private Integer codiceEsitoSingoloPagamento;

  @JsonProperty("DATA_ESITO_SINGOLO_PAGAMENTO")
  private Instant dataEsitoSingoloPagamento;

  @JsonProperty("IDSP")
  private String indiceDatiSingoloPagamento;

  @JsonProperty("ID_FLUSSO")
  private String identificativoFlusso;

  @JsonProperty("DATA_ORA_FLUSSO")
  private Instant dataOraFlusso;

  @JsonProperty("ID_DOMINIO")
  private String identificativoDominio;

  @JsonProperty("PSP")
  private String identificativoPSP;

  @JsonProperty("INT_PSP")
  private String identificativoIntermediarioPSP;

  @JsonProperty("UNIQUE_ID")
  private String uniqueId;

  @JsonProperty("INSERTED_TIMESTAMP")
  private Instant insertedTimestamp;
}
