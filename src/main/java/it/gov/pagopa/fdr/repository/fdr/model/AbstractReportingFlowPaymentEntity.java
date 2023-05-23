// package it.gov.pagopa.fdr.repository.fdr.model;
//
// import io.quarkus.mongodb.panache.PanacheMongoEntity;
// import java.time.Instant;
// import lombok.Data;
// import lombok.EqualsAndHashCode;
// import org.bson.codecs.pojo.annotations.BsonProperty;
// import org.bson.types.ObjectId;
//
// @Data
// @EqualsAndHashCode(callSuper = true)
// public abstract class AbstractReportingFlowPaymentEntity extends PanacheMongoEntity {
//
//  private Long revision;
//
//  private Instant created;
//
//  private Instant updated;
//
//  private String iuv;
//  private String iur;
//
//  private Long index;
//  private Double pay;
//
//  @BsonProperty("pay_status")
//  private PaymentStatusEnumEntity payStatus;
//
//  @BsonProperty("pay_date")
//  private Instant payDate;
//
//  @BsonProperty("ref_fdr_id")
//  private ObjectId refFdrId;
//
//  @BsonProperty("ref_fdr_reporting_flow_name")
//  private String refFdrReportingFlowName;
//
//  @BsonProperty("ref_fdr_reporting_sender_psp_id")
//  private String refFdrReportingSenderPspId;
// }
