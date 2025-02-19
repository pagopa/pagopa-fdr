package it.gov.pagopa.fdr.test.util;

import static io.restassured.RestAssured.given;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import it.gov.pagopa.fdr.controller.model.common.response.GenericResponse;
import it.gov.pagopa.fdr.controller.model.flow.enums.SenderTypeEnum;
import it.gov.pagopa.fdr.util.common.FileUtil;
import java.time.Instant;

public class TestUtil {

  public static String getDynamicFlowName() {
    return getDynamicFlowName(PSP_CODE);
  }

  public static String getDynamicFlowName(String psp) {
    return String.format("2016-08-16%s-%s", psp, Instant.now().toEpochMilli());
  }

  public static final String FLOW_TEMPLATE = FileUtil.getStringFromJsonFile(FLOW_TEMPLATE_PATH);

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
    String urlPspFlow = FLOWS_URL.formatted(PSP_CODE, flowName);
    String bodyFmtPspFlow =
        FLOW_TEMPLATE.formatted(
            flowName,
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
}
