package it.gov.pagopa.fdr.controller.internal;

import static io.restassured.RestAssured.given;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.*;
import static it.gov.pagopa.fdr.test.util.TestUtil.FLOW_TEMPLATE;
import static it.gov.pagopa.fdr.test.util.TestUtil.PAYMENTS_ADD_TEMPLATE;
import static it.gov.pagopa.fdr.test.util.TestUtil.PAYMENTS_ADD_TEMPLATE_2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.controller.InternalOrganizationsOperationsController;
import it.gov.pagopa.fdr.controller.model.common.response.GenericResponse;
import it.gov.pagopa.fdr.controller.model.error.ErrorResponse;
import it.gov.pagopa.fdr.controller.model.flow.enums.SenderTypeEnum;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsResponse;
import it.gov.pagopa.fdr.service.FlowService;
import it.gov.pagopa.fdr.service.model.arguments.FindFlowsByFiltersArgs;
import it.gov.pagopa.fdr.test.util.AzuriteResource;
import it.gov.pagopa.fdr.test.util.PostgresResource;
import it.gov.pagopa.fdr.test.util.TestUtil;
import it.gov.pagopa.fdr.util.common.FileUtil;
import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import java.lang.reflect.Constructor;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@Slf4j
@QuarkusTest
@QuarkusTestResource(MockServerTestResource.class)
@QuarkusTestResource(PostgresResource.class)
@QuarkusTestResource(AzuriteResource.class)
class InternalOperationControllerTest {

  private FileUtil fileUtil;

  @BeforeEach
  void setUp() {
    Logger logger = mock(Logger.class);
    fileUtil = new FileUtil(logger);
  }

  @Test
  @DisplayName("PSPS - KO FDR-0400 - JSON input wrong fields")
  void test_psp_KO_FDR0400() {
    String flowName = TestUtil.getDynamicFlowName();
    String url = INTERNAL_FLOWS_URL.formatted(PSP_CODE, flowName);
    String bodyFmt =
        fileUtil
            .getStringFromResourceAsString(FLOW_TEMPLATE_WRONG_FIELDS_PATH)
            .formatted(
                flowName,
                SenderTypeEnum.LEGAL_PERSON.name(),
                PSP_CODE,
                BROKER_CODE,
                CHANNEL_CODE,
                EC_CODE);

    ErrorResponse res =
        given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .post(url)
            .then()
            .statusCode(400)
            .extract()
            .as(ErrorResponse.class);
    assertThat(res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.BAD_REQUEST.errorCode()));
    assertThat(res.getErrors(), hasItem(hasProperty("message", equalTo("non deve essere null"))));
    assertThat(
        res.getErrors(),
        hasItem(hasProperty("path", equalTo("createEmptyFlowForInternalUse.request.fdr"))));
  }

  @Test
  @DisplayName("PSPS - OK - flow and payments creation with subsequent publish ")
  void test_psp_OK() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName, FLOW_DATE);
  }

  @Test
  @DisplayName("PSPS - OK - flow insert - Type ABI_CODE")
  void test_psp_ABICODE_createFlow_OK() {
    String flowName = TestUtil.getDynamicFlowName();
    String url = INTERNAL_FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt =
        TestUtil.FLOW_TEMPLATE.formatted(
            flowName, FLOW_DATE, SenderTypeEnum.ABI_CODE.name(), PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);

    GenericResponse res =
        given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .post(url)
            .then()
            .statusCode(201)
            .extract()
            .as(GenericResponse.class);
    assertThat(res.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));
  }

  @Test
  @DisplayName("PSPS - OK - - flow insert - Type BIC_CODE")
  void test_psp_BIC_CODE_createFlow_OK() {
    String flowName = TestUtil.getDynamicFlowName();
    String url = INTERNAL_FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt =
        FLOW_TEMPLATE.formatted(
            flowName, FLOW_DATE, SenderTypeEnum.BIC_CODE.name(), PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);

    GenericResponse res =
        given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .post(url)
            .then()
            .statusCode(201)
            .extract()
            .as(GenericResponse.class);
    assertThat(res.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));
  }

  @Test
  @DisplayName("PSPS - OK - flow insert and subsequent delete")
  void test_psp_deleteFlow_OK() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = INTERNAL_FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt =
        FLOW_TEMPLATE.formatted(
            flowName,
            FLOW_DATE,
            SenderTypeEnum.LEGAL_PERSON.name(),
            PSP_CODE,
            BROKER_CODE,
            CHANNEL_CODE,
            EC_CODE);

    GenericResponse resSave =
        given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .post(urlSave)
            .then()
            .statusCode(201)
            .extract()
            .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    String urlDel = INTERNAL_FLOWS_DELETE_URL.formatted(PSP_CODE, flowName);

    GenericResponse resDel =
        given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .delete(urlDel)
            .then()
            .statusCode(200)
            .extract()
            .as(GenericResponse.class);
    assertThat(resDel.getMessage(), equalTo("Fdr [%s] deleted".formatted(flowName)));

    String urlDel2 = INTERNAL_FLOWS_DELETE_URL.formatted(PSP_CODE, flowName);

    ErrorResponse resDelError =
        given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .delete(urlDel2)
            .then()
            .statusCode(404)
            .extract()
            .as(ErrorResponse.class);
    assertThat(
        resDelError.getAppErrorCode(),
        equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND.errorCode()));
    assertThat(
        resDelError.getErrors(),
        hasItem(
            hasProperty(
                "message", equalTo(String.format("Flow with ID [%s] not found.", flowName)))));
  }

  @Test
  @DisplayName("PSPS - OK - flow with payments insert and subsequent delete")
  void test_psp_deleteFlowWithPayment_OK() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = INTERNAL_FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt =
        FLOW_TEMPLATE.formatted(
            flowName,
            FLOW_DATE,
            SenderTypeEnum.LEGAL_PERSON.name(),
            PSP_CODE,
            BROKER_CODE,
            CHANNEL_CODE,
            EC_CODE);

    GenericResponse resSave =
        given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .post(urlSave)
            .then()
            .statusCode(201)
            .extract()
            .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    String urlSavePayment = INTERNAL_PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);

    GenericResponse resSavePays =
        given()
            .body(PAYMENTS_ADD_TEMPLATE)
            .header(HEADER)
            .when()
            .put(urlSavePayment)
            .then()
            .statusCode(200)
            .extract()
            .as(GenericResponse.class);
    assertThat(resSavePays.getMessage(), equalTo("Fdr [%s] payment added".formatted(flowName)));

    String urlDeleteFlow = INTERNAL_FLOWS_DELETE_URL.formatted(PSP_CODE, flowName);

    GenericResponse resDelFlow =
        given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .delete(urlDeleteFlow)
            .then()
            .statusCode(200)
            .extract()
            .as(GenericResponse.class);
    assertThat(resDelFlow.getMessage(), equalTo("Fdr [%s] deleted".formatted(flowName)));
  }

  @Test
  @DisplayName("PSPS - OK - payments insert and delete")
  void test_psp_deletePayments_OK() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = INTERNAL_FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt =
        FLOW_TEMPLATE.formatted(
            flowName,
            FLOW_DATE,
            SenderTypeEnum.LEGAL_PERSON.name(),
            PSP_CODE,
            BROKER_CODE,
            CHANNEL_CODE,
            EC_CODE);

    GenericResponse resSave =
        given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .post(urlSave)
            .then()
            .statusCode(201)
            .extract()
            .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    String urlSavePayment = INTERNAL_PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);

    GenericResponse resSavePays =
        given()
            .body(PAYMENTS_ADD_TEMPLATE)
            .header(HEADER)
            .when()
            .put(urlSavePayment)
            .then()
            .statusCode(200)
            .extract()
            .as(GenericResponse.class);
    assertThat(resSavePays.getMessage(), equalTo("Fdr [%s] payment added".formatted(flowName)));

    GenericResponse resSavePays2 =
        given()
            .body(PAYMENTS_ADD_TEMPLATE_2)
            .header(HEADER)
            .when()
            .put(urlSavePayment)
            .then()
            .statusCode(200)
            .extract()
            .as(GenericResponse.class);
    assertThat(resSavePays.getMessage(), equalTo("Fdr [%s] payment added".formatted(flowName)));

    String urlDelPays = INTERNAL_PAYMENTS_DELETE_URL.formatted(PSP_CODE, flowName);

    GenericResponse resDelPays =
        given()
            .body(
                fileUtil.getStringFromResourceAsString(
                    INTERNAL_OPERATION_PAYMENTS_DELETE_TEMPLATE_PATH))
            .header(HEADER)
            .when()
            .put(urlDelPays)
            .then()
            .statusCode(200)
            .extract()
            .as(GenericResponse.class);
    assertThat(resDelPays.getMessage(), equalTo("Fdr [%s] payment deleted".formatted(flowName)));

    ErrorResponse resDelError =
        given()
            .body(
                fileUtil.getStringFromResourceAsString(
                    INTERNAL_OPERATION_PAYMENTS_DELETE_TEMPLATE_PATH))
            .header(HEADER)
            .when()
            .put(urlDelPays)
            .then()
            .statusCode(400)
            .extract()
            .as(ErrorResponse.class);
    assertThat(
        resDelError.getAppErrorCode(),
        equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_NO_MATCH_INDEX.errorCode()));
    assertThat(
        resDelError.getErrors(),
        hasItem(
            hasProperty(
                "message",
                equalTo(
                    String.format(
                        "Index of payment not match with index loaded on flow with ID [%s].",
                        flowName)))));
  }

  @Test
  @DisplayName("PSPS - OK - payments insert with partial delete")
  void test_psp_deletePayments_partial_OK() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = INTERNAL_FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt =
        FLOW_TEMPLATE.formatted(
            flowName,
            FLOW_DATE,
            SenderTypeEnum.LEGAL_PERSON.name(),
            PSP_CODE,
            BROKER_CODE,
            CHANNEL_CODE,
            EC_CODE);

    GenericResponse resSave =
        given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .post(urlSave)
            .then()
            .statusCode(201)
            .extract()
            .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    String urlSavePayment = INTERNAL_PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);

    GenericResponse resSavePays =
        given()
            .body(PAYMENTS_ADD_TEMPLATE)
            .header(HEADER)
            .when()
            .put(urlSavePayment)
            .then()
            .statusCode(200)
            .extract()
            .as(GenericResponse.class);
    assertThat(resSavePays.getMessage(), equalTo("Fdr [%s] payment added".formatted(flowName)));

    String urlDelPays = INTERNAL_PAYMENTS_DELETE_URL.formatted(PSP_CODE, flowName);

    GenericResponse resDelPays =
        given()
            .body(
                fileUtil.getStringFromResourceAsString(
                    INTERNAL_OPERATION_PAYMENTS_DELETE_TEMPLATE_PATH))
            .header(HEADER)
            .when()
            .put(urlDelPays)
            .then()
            .statusCode(200)
            .extract()
            .as(GenericResponse.class);
    assertThat(resDelPays.getMessage(), equalTo("Fdr [%s] payment deleted".formatted(flowName)));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0701 - flow not found in publish")
  void test_psp_publish_KO_FDR0701() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = INTERNAL_FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt =
        FLOW_TEMPLATE.formatted(
            flowName,
            FLOW_DATE,
            SenderTypeEnum.LEGAL_PERSON.name(),
            PSP_CODE,
            BROKER_CODE,
            CHANNEL_CODE,
            EC_CODE);

    GenericResponse resSave =
        given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .post(urlSave)
            .then()
            .statusCode(201)
            .extract()
            .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    String urlSavePayment = INTERNAL_PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);

    GenericResponse resSavePays =
        given()
            .body(PAYMENTS_ADD_TEMPLATE)
            .header(HEADER)
            .when()
            .put(urlSavePayment)
            .then()
            .statusCode(200)
            .extract()
            .as(GenericResponse.class);
    assertThat(resSavePays.getMessage(), equalTo("Fdr [%s] payment added".formatted(flowName)));

    String flowNameWrong = TestUtil.getDynamicFlowName();
    String urlPublishFlow = INTERNAL_FLOWS_PUBLISH_URL.formatted(PSP_CODE, flowNameWrong);

    ErrorResponse resDelError =
        given()
            .header(HEADER)
            .when()
            .post(urlPublishFlow)
            .then()
            .statusCode(404)
            .extract()
            .as(ErrorResponse.class);
    assertThat(
        resDelError.getAppErrorCode(),
        equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND.errorCode()));
    assertThat(
        resDelError.getErrors(),
        hasItem(
            hasProperty(
                "message", equalTo(String.format("Flow with ID [%s] not found.", flowNameWrong)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0701 - flow not found in add payments new flowName")
  void test_psp_payments_add_KO_FDR0701() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = INTERNAL_FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt =
        FLOW_TEMPLATE.formatted(
            flowName,
            FLOW_DATE,
            SenderTypeEnum.LEGAL_PERSON.name(),
            PSP_CODE,
            BROKER_CODE,
            CHANNEL_CODE,
            EC_CODE);

    GenericResponse resSave =
        given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .post(urlSave)
            .then()
            .statusCode(201)
            .extract()
            .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    String flowName2 = TestUtil.getDynamicFlowName();
    String urlAddPayments = INTERNAL_PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName2);
    ErrorResponse resDelError =
        given()
            .body(PAYMENTS_ADD_TEMPLATE)
            .header(HEADER)
            .when()
            .put(urlAddPayments)
            .then()
            .statusCode(404)
            .extract()
            .as(ErrorResponse.class);
    assertThat(
        resDelError.getAppErrorCode(),
        equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND.errorCode()));
    assertThat(
        resDelError.getErrors(),
        hasItem(
            hasProperty(
                "message", equalTo(String.format("Flow with ID [%s] not found.", flowName2)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0701 - flow not found in delete payments new flowName")
  void test_psp_payments_delete_KO_FDR0701() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = INTERNAL_FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt =
        FLOW_TEMPLATE.formatted(
            flowName,
            FLOW_DATE,
            SenderTypeEnum.LEGAL_PERSON.name(),
            PSP_CODE,
            BROKER_CODE,
            CHANNEL_CODE,
            EC_CODE);
    GenericResponse resSave =
        given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .post(urlSave)
            .then()
            .statusCode(201)
            .extract()
            .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    String flowNameUnknown = TestUtil.getDynamicFlowName();
    String urlDelPays = INTERNAL_PAYMENTS_DELETE_URL.formatted(PSP_CODE, flowNameUnknown);
    ErrorResponse resDelError =
        given()
            .body(
                fileUtil.getStringFromResourceAsString(
                    INTERNAL_OPERATION_PAYMENTS_DELETE_TEMPLATE_PATH))
            .header(HEADER)
            .when()
            .put(urlDelPays)
            .then()
            .statusCode(404)
            .extract()
            .as(ErrorResponse.class);
    assertThat(
        resDelError.getAppErrorCode(),
        equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND.errorCode()));
    assertThat(
        resDelError.getErrors(),
        hasItem(
            hasProperty(
                "message",
                equalTo(String.format("Flow with ID [%s] not found.", flowNameUnknown)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0702 - flow already exists")
  void test_psp_KO_FDR0702() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = INTERNAL_FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt =
        FLOW_TEMPLATE.formatted(
            flowName,
            FLOW_DATE,
            SenderTypeEnum.LEGAL_PERSON.name(),
            PSP_CODE,
            BROKER_CODE,
            CHANNEL_CODE,
            EC_CODE);

    GenericResponse resSave =
        given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .post(urlSave)
            .then()
            .statusCode(201)
            .extract()
            .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    ErrorResponse resDelError =
        given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .post(urlSave)
            .then()
            .statusCode(400)
            .extract()
            .as(ErrorResponse.class);
    assertThat(
        resDelError.getAppErrorCode(),
        equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_ALREADY_EXIST.errorCode()));
    assertThat(
        resDelError.getErrors(),
        hasItem(
            hasProperty(
                "message",
                equalTo(
                    String.format(
                        "Flow with ID [%s] already exists with [CREATED] status.", flowName)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0703 - flow not found in publish flow CREATED")
  void test_psp_CREATED_publish_KO_FDR0703() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = INTERNAL_FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt =
        FLOW_TEMPLATE.formatted(
            flowName,
            FLOW_DATE,
            SenderTypeEnum.LEGAL_PERSON.name(),
            PSP_CODE,
            BROKER_CODE,
            CHANNEL_CODE,
            EC_CODE);

    GenericResponse resSave =
        given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .post(urlSave)
            .then()
            .statusCode(201)
            .extract()
            .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    String urlPublishFlow = INTERNAL_FLOWS_PUBLISH_URL.formatted(PSP_CODE, flowName);
    ErrorResponse resPublish =
        given()
            .header(HEADER)
            .when()
            .post(urlPublishFlow)
            .then()
            .statusCode(400)
            .extract()
            .as(ErrorResponse.class);
    assertThat(
        resPublish.getAppErrorCode(),
        equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION.errorCode()));
    assertThat(
        resPublish.getErrors(),
        hasItem(
            hasProperty(
                "message",
                equalTo(
                    String.format("Flow with ID [%s] exists with [CREATED] status.", flowName)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0703 - fdr wrong action delete payments")
  void test_psp_payments_delete_KO_FDR0703() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = INTERNAL_FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt =
        FLOW_TEMPLATE.formatted(
            flowName,
            FLOW_DATE,
            SenderTypeEnum.LEGAL_PERSON.name(),
            PSP_CODE,
            BROKER_CODE,
            CHANNEL_CODE,
            EC_CODE);

    GenericResponse resSave =
        given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .post(urlSave)
            .then()
            .statusCode(201)
            .extract()
            .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    String urlDelPays = INTERNAL_PAYMENTS_DELETE_URL.formatted(PSP_CODE, flowName);
    ErrorResponse resDelError =
        given()
            .body(
                fileUtil.getStringFromResourceAsString(
                    INTERNAL_OPERATION_PAYMENTS_DELETE_TEMPLATE_PATH))
            .header(HEADER)
            .when()
            .put(urlDelPays)
            .then()
            .statusCode(400)
            .extract()
            .as(ErrorResponse.class);
    assertThat(
        resDelError.getAppErrorCode(),
        equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION.errorCode()));
    assertThat(
        resDelError.getErrors(),
        hasItem(
            hasProperty(
                "message",
                equalTo(
                    String.format("Flow with ID [%s] exists with [CREATED] status.", flowName)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0706 - payments with same index")
  void test_psp_KO_FDR0706() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = INTERNAL_FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt =
        FLOW_TEMPLATE.formatted(
            flowName,
            FLOW_DATE,
            SenderTypeEnum.LEGAL_PERSON.name(),
            PSP_CODE,
            BROKER_CODE,
            CHANNEL_CODE,
            EC_CODE);

    GenericResponse resSave =
        given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .post(urlSave)
            .then()
            .statusCode(201)
            .extract()
            .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    String urlSavePayment = INTERNAL_PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);

    GenericResponse resSavePays =
        given()
            .body(PAYMENTS_ADD_TEMPLATE)
            .header(HEADER)
            .when()
            .put(urlSavePayment)
            .then()
            .statusCode(200)
            .extract()
            .as(GenericResponse.class);
    assertThat(resSavePays.getMessage(), equalTo("Fdr [%s] payment added".formatted(flowName)));

    ErrorResponse resSavePays2 =
        given()
            .body(fileUtil.getStringFromResourceAsString(PAYMENTS_2_ADD_TEMPLATE_PATH))
            .header(HEADER)
            .when()
            .put(urlSavePayment)
            .then()
            .statusCode(400)
            .extract()
            .as(ErrorResponse.class);
    assertThat(
        resSavePays2.getAppErrorCode(),
        equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_DUPLICATE_INDEX.errorCode()));
    assertThat(
        resSavePays2.getErrors(),
        hasItem(
            hasProperty(
                "message",
                equalTo(
                    String.format(
                        "One or more payment index already added on flow with ID [%s].",
                        flowName)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0707 - payments unknown index delete")
  void test_psp_KO_FDR0707() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = INTERNAL_FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt =
        FLOW_TEMPLATE.formatted(
            flowName,
            FLOW_DATE,
            SenderTypeEnum.LEGAL_PERSON.name(),
            PSP_CODE,
            BROKER_CODE,
            CHANNEL_CODE,
            EC_CODE);

    GenericResponse resSave =
        given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .post(urlSave)
            .then()
            .statusCode(201)
            .extract()
            .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    String urlSavePayment = INTERNAL_PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);

    GenericResponse resSavePays =
        given()
            .body(PAYMENTS_ADD_TEMPLATE)
            .header(HEADER)
            .when()
            .put(urlSavePayment)
            .then()
            .statusCode(200)
            .extract()
            .as(GenericResponse.class);
    assertThat(resSavePays.getMessage(), equalTo("Fdr [%s] payment added".formatted(flowName)));

    String urlDelPays = INTERNAL_PAYMENTS_DELETE_URL.formatted(PSP_CODE, flowName);
    ErrorResponse resDelError =
        given()
            .body(fileUtil.getStringFromResourceAsString(PAYMENTS_DELETE_WRONG_TEMPLATE_PATH))
            .header(HEADER)
            .when()
            .put(urlDelPays)
            .then()
            .statusCode(400)
            .extract()
            .as(ErrorResponse.class);
    assertThat(
        resDelError.getAppErrorCode(),
        equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_NO_MATCH_INDEX.errorCode()));
    assertThat(
        resDelError.getErrors(),
        hasItem(
            hasProperty(
                "message",
                equalTo(
                    String.format(
                        "Index of payment not match with index loaded on flow with ID [%s].",
                        flowName)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0401 - JSON incorrect value")
  void test_psp_KO_FDR0401() {
    String flowName = TestUtil.getDynamicFlowName();
    String url = INTERNAL_PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);
    String wrongFormatDecimal = "0,01";
    String bodyFmt =
        fileUtil
            .getStringFromResourceAsString(PAYMENTS_ADD_INVALID_FIELD_VALUE_TEMPLATE_PATH)
            .formatted(wrongFormatDecimal);

    ErrorResponse res =
        given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .put(url)
            .then()
            .statusCode(400)
            .extract()
            .as(ErrorResponse.class);
    assertThat(
        res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.BAD_REQUEST_INPUT_JSON.errorCode()));
    assertThat(
        res.getErrors(),
        hasItem(
            hasProperty(
                "message",
                equalTo(
                    String.format(
                        "Bad request. Field [payments.pay] is equals to [%s] but this is not a"
                            + " valid value.",
                        wrongFormatDecimal)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0402 - JSON invalid input instant")
  void test_psp_KO_FDR0402() {
    String flowName = TestUtil.getDynamicFlowName();
    String url = INTERNAL_FLOWS_URL.formatted(PSP_CODE, flowName);
    String wrongFormatDate = "2023-04-05";
    String bodyFmt =
        fileUtil
            .getStringFromResourceAsString(FLOW_TEMPLATE_WRONG_INSTANT_PATH)
            .formatted(
                flowName,
                wrongFormatDate,
                SenderTypeEnum.LEGAL_PERSON.name(),
                PSP_CODE,
                BROKER_CODE,
                CHANNEL_CODE,
                EC_CODE);

    ErrorResponse res =
        given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .post(url)
            .then()
            .statusCode(400)
            .extract()
            .as(ErrorResponse.class);
    assertThat(
        res.getAppErrorCode(),
        equalTo(AppErrorCodeMessageEnum.BAD_REQUEST_INPUT_JSON_INSTANT.errorCode()));
    assertThat(
        res.getErrors(),
        hasItem(
            hasProperty(
                "message",
                equalTo(
                    String.format(
                        "Bad request. Field [fdrDate] is equals to [%s] but it is expected to be in"
                            + " ISO-8601 format [yyyy-MM-ddTHH:mm:ssZ] (example:"
                            + " [2025-01-01T12:00:00.123000Z]).",
                        wrongFormatDate)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0403 - JSON invalid input enum")
  void test_psp_KO_FDR0403() {
    String flowName = TestUtil.getDynamicFlowName();
    String url = INTERNAL_FLOWS_URL.formatted(PSP_CODE, flowName);
    String wrongEnum = "WRONG_ENUM";
    String bodyFmt =
        FLOW_TEMPLATE.formatted(flowName, FLOW_DATE, wrongEnum, PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);

    ErrorResponse res =
        given()
            .body(bodyFmt)
            .header(HEADER)
            .when()
            .post(url)
            .then()
            .statusCode(400)
            .extract()
            .as(ErrorResponse.class);
    assertThat(
        res.getAppErrorCode(),
        equalTo(AppErrorCodeMessageEnum.BAD_REQUEST_INPUT_JSON_ENUM.errorCode()));
    assertThat(
        res.getErrors(),
        hasItem(
            hasProperty(
                "message",
                equalTo(
                    String.format(
                        "Bad request. Field [sender.type] is equals to [%s] but it is expected to"
                            + " be one of the following values: [[LEGAL_PERSON, ABI_CODE,"
                            + " BIC_CODE]].",
                        wrongEnum)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0404 - JSON deserialization error")
  void test_psp_KO_FDR0404() {
    String flowName = TestUtil.getDynamicFlowName();
    String url = INTERNAL_PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);

    ErrorResponse resDelError =
        given()
            .body(fileUtil.getStringFromResourceAsString(PAYMENTS_ADD_INVALID_FORMAT_TEMPLATE_PATH))
            .header(HEADER)
            .when()
            .put(url)
            .then()
            .statusCode(400)
            .extract()
            .as(ErrorResponse.class);
    assertThat(
        resDelError.getAppErrorCode(),
        equalTo(AppErrorCodeMessageEnum.BAD_REQUEST_INPUT_JSON_DESERIALIZE_ERROR.errorCode()));
    assertThat(
        resDelError.getErrors(),
        hasItem(
            hasProperty(
                "message",
                equalTo(
                    "Bad request. Field [payments] generate a deserialization error. Please, set"
                        + " the correct value."))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0405 - JSON malformed")
  void test_psp_KO_FDR0405() {
    String flowName = TestUtil.getDynamicFlowName();
    String url = INTERNAL_FLOWS_URL.formatted(PSP_CODE, flowName);

    ErrorResponse res =
        given()
            .body(fileUtil.getStringFromResourceAsString(MALFORMED_JSON_PATH))
            .header(HEADER)
            .when()
            .post(url)
            .then()
            .statusCode(400)
            .extract()
            .as(ErrorResponse.class);
    assertThat(
        res.getAppErrorCode(),
        equalTo(AppErrorCodeMessageEnum.BAD_REQUEST_INPUT_JSON_NON_VALID_FORMAT.errorCode()));
    assertThat(
        res.getErrors(),
        hasItem(
            hasProperty(
                "message", equalTo("Bad request. The format of JSON request is not valid."))));
  }

  // ==================== getAllPublishedFlowsForInternalUse - Unit Tests ====================

  @Nested
  @DisplayName("getAllPublishedFlowsForInternalUse")
  class GetAllPublishedFlowsForInternalUseTest {

    private static final String ORGANIZATION_ID = "15376371009";
    private static final String PSP_ID = "88888888888";
    private static final long PAGE_NUMBER = 1L;
    private static final long PAGE_SIZE = 50L;

    private FlowService flowServiceMock;
    private InternalOrganizationsOperationsController controller;

    @BeforeEach
    void setUpUnit() throws Exception {
      flowServiceMock = mock(FlowService.class);
      Constructor<InternalOrganizationsOperationsController> ctor =
          InternalOrganizationsOperationsController.class.getDeclaredConstructor(FlowService.class);
      ctor.setAccessible(true);
      controller = ctor.newInstance(flowServiceMock);

      when(flowServiceMock.getPaginatedPublishedFlowsForCI(any(FindFlowsByFiltersArgs.class)))
          .thenReturn(PaginatedFlowsResponse.builder().count(0).data(List.of()).build());
    }

    @Test
    @DisplayName("OK - date esplicite passate al service invariate")
    void withExplicitDates_passesThemToService() {
      Instant explicitPublishedGt = Instant.parse("2026-01-15T08:00:00Z");
      Instant explicitFlowDate = Instant.parse("2026-01-10T00:00:00Z");

      controller.getAllPublishedFlowsForInternalUse(
          ORGANIZATION_ID,
          PSP_ID,
          Optional.of(explicitPublishedGt),
          Optional.of(explicitFlowDate),
          PAGE_NUMBER,
          PAGE_SIZE);

      ArgumentCaptor<FindFlowsByFiltersArgs> captor =
          ArgumentCaptor.forClass(FindFlowsByFiltersArgs.class);
      verify(flowServiceMock).getPaginatedPublishedFlowsForCI(captor.capture());

      FindFlowsByFiltersArgs args = captor.getValue();
      assertEquals(ORGANIZATION_ID, args.getOrganizationId());
      assertEquals(PSP_ID, args.getPspId());
      assertEquals(explicitPublishedGt, args.getPublishedGt());
      assertEquals(explicitFlowDate, args.getFlowDate());
      assertEquals(PAGE_NUMBER, args.getPageNumber());
      assertEquals(PAGE_SIZE, args.getPageSize());
    }

    @Test
    @DisplayName("OK - Optional vuoti usano la data di default (30gg fa mezzanotte UTC)")
    void withEmptyOptionals_usesDefaultDate() {
      Instant beforeCall = Instant.now();

      controller.getAllPublishedFlowsForInternalUse(
          ORGANIZATION_ID, PSP_ID, Optional.empty(), Optional.empty(), PAGE_NUMBER, PAGE_SIZE);

      Instant afterCall = Instant.now();

      ArgumentCaptor<FindFlowsByFiltersArgs> captor =
          ArgumentCaptor.forClass(FindFlowsByFiltersArgs.class);
      verify(flowServiceMock).getPaginatedPublishedFlowsForCI(captor.capture());

      FindFlowsByFiltersArgs args = captor.getValue();

      Instant expectedLower =
          beforeCall.atZone(ZoneOffset.UTC).minusDays(30).toLocalDate()
              .atTime(LocalTime.MIN).atZone(ZoneOffset.UTC).toInstant();
      Instant expectedUpper =
          afterCall.atZone(ZoneOffset.UTC).minusDays(30).toLocalDate()
              .atTime(LocalTime.MIN).atZone(ZoneOffset.UTC).toInstant();

      assertTrue(
          !args.getPublishedGt().isBefore(expectedLower)
              && !args.getPublishedGt().isAfter(expectedUpper),
          "publishedGt default fuori range atteso");
      assertTrue(
          !args.getFlowDate().isBefore(expectedLower)
              && !args.getFlowDate().isAfter(expectedUpper),
          "flowDate default fuori range atteso");
    }

    @Test
    @DisplayName("OK - publishedGt esplicito, flowDate di default")
    void withOnlyPublishedGtPresent_usesDefaultForFlowDate() {
      Instant explicitPublishedGt = Instant.parse("2026-02-01T10:00:00Z");
      Instant beforeCall = Instant.now();

      controller.getAllPublishedFlowsForInternalUse(
          ORGANIZATION_ID,
          PSP_ID,
          Optional.of(explicitPublishedGt),
          Optional.empty(),
          PAGE_NUMBER,
          PAGE_SIZE);

      Instant afterCall = Instant.now();

      ArgumentCaptor<FindFlowsByFiltersArgs> captor =
          ArgumentCaptor.forClass(FindFlowsByFiltersArgs.class);
      verify(flowServiceMock).getPaginatedPublishedFlowsForCI(captor.capture());

      FindFlowsByFiltersArgs args = captor.getValue();
      assertEquals(explicitPublishedGt, args.getPublishedGt());

      Instant expectedLower =
          beforeCall.atZone(ZoneOffset.UTC).minusDays(30).toLocalDate()
              .atTime(LocalTime.MIN).atZone(ZoneOffset.UTC).toInstant();
      Instant expectedUpper =
          afterCall.atZone(ZoneOffset.UTC).minusDays(30).toLocalDate()
              .atTime(LocalTime.MIN).atZone(ZoneOffset.UTC).toInstant();

      assertTrue(
          !args.getFlowDate().isBefore(expectedLower) && !args.getFlowDate().isAfter(expectedUpper),
          "flowDate default fuori range atteso");
    }

    @Test
    @DisplayName("OK - flowDate esplicito, publishedGt di default")
    void withOnlyFlowDatePresent_usesDefaultForPublishedGt() {
      Instant explicitFlowDate = Instant.parse("2026-02-10T00:00:00Z");
      Instant beforeCall = Instant.now();

      controller.getAllPublishedFlowsForInternalUse(
          ORGANIZATION_ID,
          PSP_ID,
          Optional.empty(),
          Optional.of(explicitFlowDate),
          PAGE_NUMBER,
          PAGE_SIZE);

      Instant afterCall = Instant.now();

      ArgumentCaptor<FindFlowsByFiltersArgs> captor =
          ArgumentCaptor.forClass(FindFlowsByFiltersArgs.class);
      verify(flowServiceMock).getPaginatedPublishedFlowsForCI(captor.capture());

      FindFlowsByFiltersArgs args = captor.getValue();
      assertEquals(explicitFlowDate, args.getFlowDate());

      Instant expectedLower =
          beforeCall.atZone(ZoneOffset.UTC).minusDays(30).toLocalDate()
              .atTime(LocalTime.MIN).atZone(ZoneOffset.UTC).toInstant();
      Instant expectedUpper =
          afterCall.atZone(ZoneOffset.UTC).minusDays(30).toLocalDate()
              .atTime(LocalTime.MIN).atZone(ZoneOffset.UTC).toInstant();

      assertTrue(
          !args.getPublishedGt().isBefore(expectedLower)
              && !args.getPublishedGt().isAfter(expectedUpper),
          "publishedGt default fuori range atteso");
    }

    @Test
    @DisplayName("OK - la risposta del service viene restituita invariata")
    void returnsServiceResponse() {
      PaginatedFlowsResponse expected =
          PaginatedFlowsResponse.builder().count(5L).data(List.of()).build();
      when(flowServiceMock.getPaginatedPublishedFlowsForCI(any(FindFlowsByFiltersArgs.class)))
          .thenReturn(expected);

      PaginatedFlowsResponse actual =
          controller.getAllPublishedFlowsForInternalUse(
              ORGANIZATION_ID, PSP_ID, Optional.empty(), Optional.empty(), PAGE_NUMBER, PAGE_SIZE);

      assertNotNull(actual);
      assertEquals(expected, actual);
      assertEquals(5L, actual.getCount());
    }

    @Test
    @DisplayName("OK - pspId nullo passato al service invariato")
    void withNullPspId_passesNullToService() {
      controller.getAllPublishedFlowsForInternalUse(
          ORGANIZATION_ID, null, Optional.empty(), Optional.empty(), PAGE_NUMBER, PAGE_SIZE);

      ArgumentCaptor<FindFlowsByFiltersArgs> captor =
          ArgumentCaptor.forClass(FindFlowsByFiltersArgs.class);
      verify(flowServiceMock).getPaginatedPublishedFlowsForCI(captor.capture());

      FindFlowsByFiltersArgs args = captor.getValue();
      assertEquals(ORGANIZATION_ID, args.getOrganizationId());
      assertNull(args.getPspId());
    }
  }
}
