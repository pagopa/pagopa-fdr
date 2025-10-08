package it.gov.pagopa.fdr.test.util;

import static io.restassured.RestAssured.given;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import it.gov.pagopa.fdr.controller.model.common.response.GenericResponse;
import it.gov.pagopa.fdr.controller.model.flow.Receiver;
import it.gov.pagopa.fdr.controller.model.flow.Sender;
import it.gov.pagopa.fdr.controller.model.flow.enums.SenderTypeEnum;
import it.gov.pagopa.fdr.repository.entity.FlowToHistoryEntity;
import it.gov.pagopa.fdr.storage.model.FlowBlob;
import it.gov.pagopa.fdr.storage.model.PaymentBlob;
import it.gov.pagopa.fdr.util.common.FileUtil;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

public class TestUtil {

  public static String getDynamicFlowName() {
    return getDynamicFlowName(PSP_CODE);
  }

  public static String getDynamicFlowName(String psp) {
    return String.format("2016-08-16%s-%s", psp, Instant.now().toEpochMilli());
  }

  public static final String FLOW_TEMPLATE = FileUtil.getStringFromJsonFile(FLOW_TEMPLATE_PATH);
  public static final String FLOW_TEMPLATE_CUSTOM_DATE_FLOW_TEMPLATE = FileUtil.getStringFromJsonFile(FLOW_TEMPLATE_CUSTOM_DATE_PATH);

  public static String PAYMENTS_ADD_TEMPLATE =
      FileUtil.getStringFromJsonFile(PAYMENTS_ADD_TEMPLATE_PATH);

  public static String PAYMENTS_ADD_TEMPLATE_2 =
      FileUtil.getStringFromJsonFile(PAYMENTS_ADD_TEMPLATE_2_PATH);

  public static void pspSunnyDay(String flowName) {

    pspCreateUnpublishedFlow(flowName);

    String urlPublish = FLOWS_PUBLISH_URL.formatted(PSP_CODE, flowName);
    GenericResponse resPublish =
        given()
            .header(HEADER)
            .when()
            .post(urlPublish)
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(GenericResponse.class);
    assertThat(resPublish.getMessage(), equalTo(String.format("Fdr [%s] published", flowName)));
  }

  public static void pspCreateUnpublishedFlow(String flowName) {
    Instant now = Instant.now();
    ZonedDateTime nowUtc = now.atZone(ZoneOffset.UTC);
    ZonedDateTime limitZoned = nowUtc.minusDays(20);
    Instant flowDate = limitZoned.toInstant();
    String urlPspFlow = FLOWS_URL.formatted(PSP_CODE, flowName);
    String bodyFmtPspFlow =
            FLOW_TEMPLATE_CUSTOM_DATE_FLOW_TEMPLATE.formatted(
            flowName,
            flowDate,
            SenderTypeEnum.LEGAL_PERSON.name(),
            PSP_CODE,
            BROKER_CODE,
            CHANNEL_CODE,
            EC_CODE);

    GenericResponse resPspFlow =
        given()
            .body(bodyFmtPspFlow)
            .header(HEADER)
            .when()
            .post(urlPspFlow)
            .then()
            .statusCode(201)
            .extract()
            .body()
            .as(GenericResponse.class);
    assertThat(resPspFlow.getMessage(), equalTo(String.format("Fdr [%s] saved", flowName)));

    String urlPayment = PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);
    String bodyPayment = PAYMENTS_ADD_TEMPLATE;
    GenericResponse resPayment =
        given()
            .body(bodyPayment)
            .header(HEADER)
            .when()
            .put(urlPayment)
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(GenericResponse.class);
    assertThat(resPayment.getMessage(), equalTo(String.format("Fdr [%s] payment added", flowName)));
  }

  public static FlowBlob validFlowBlob() {
    return FlowBlob.builder()
        .fdr("fdr")
        .fdrDate(Instant.now())
        .revision(1L)
        .created(Instant.now())
        .updated(Instant.now())
        .published(Instant.now())
        .status("PUBLISHED")
        .sender(
            Sender.builder()
                .type(SenderTypeEnum.ABI_CODE)
                .id("id")
                .pspId("5555")
                .pspName("PSP Name")
                .pspBrokerId("7777")
                .channelId("7777_1")
                .password("password")
                .build())
        .receiver(
            Receiver.builder().id("id").organizationId("22222").organizationName("EC Name").build())
        .regulation("123")
        .regulationDate("")
        .bicCodePouringBank("123")
        .computedTotPayments(1L)
        .computedSumPayments(BigDecimal.valueOf(1.0))
        .payments(
            List.of(
                PaymentBlob.builder()
                    .pay(BigDecimal.valueOf(1.0))
                    .index(1L)
                    .iuv("iuv")
                    .iur("iur")
                    .idTransfer(1L)
                    .payDate("")
                    .payStatus("OK")
                    .build()))
        .build();
  }

  public static FlowToHistoryEntity validFlowToHistory(String dynamicFlowName) {
    return FlowToHistoryEntity.builder()
        .id(1L)
        .pspId(AppConstantTestHelper.PSP_CODE)
        .name(dynamicFlowName)
        .retries(0)
        .isExternal(true)
        .lastExecution(Instant.now())
        .created(Instant.now())
        .revision(1L)
        .build();
  }
}
