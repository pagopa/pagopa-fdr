package it.gov.pagopa.fdr.service.flowTx.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FlowTx {

  @JsonProperty("ID_FLUSSO")
  private String idFlusso;

  @JsonProperty("DATA_ORA_FLUSSO")
  private Instant dataOraFlusso;

  @JsonProperty("INSERTED_TIMESTAMP")
  private Instant insertedTimestamp;

  @JsonProperty("DATA_REGOLAMENTO")
  private Instant dataRegolamento;

  @JsonProperty("CAUSALE")
  private String identificativoUnivocoRegolamento;

  @JsonProperty("NUM_PAGAMENTI")
  private Integer numeroTotalePagamenti;

  @JsonProperty("SOMMA_VERSATA")
  private BigDecimal importoTotalePagamenti;

  @JsonProperty("ID_DOMINIO")
  private String idDominio;

  @JsonProperty("PSP")
  private String psp;

  @JsonProperty("INT_PSP")
  private String intPsp;

  @JsonProperty("UNIQUE_ID")
  private String uniqueId;

  @JsonProperty("ALL_DATES")
  private List<Instant> dataEsitoSingoloPagamentoList;
}
