package it.gov.pagopa.fdr.rest.psps;

import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.rest.exceptionmapper.ErrorResponse;
import it.gov.pagopa.fdr.rest.model.*;
import it.gov.pagopa.fdr.rest.organizations.response.GetPaymentResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetResponse;
import it.gov.pagopa.fdr.rest.psps.response.GetAllCreatedResponse;
import it.gov.pagopa.fdr.service.dto.SenderTypeEnumDto;
import it.gov.pagopa.fdr.test.util.AzuriteResource;
import it.gov.pagopa.fdr.test.util.MongoResource;
import it.gov.pagopa.fdr.test.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.smallrye.common.constraint.Assert.assertTrue;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.*;
import static it.gov.pagopa.fdr.test.util.TestUtil.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@QuarkusTestResource(MockServerTestResource.class)
@QuarkusTestResource(MongoResource.class)
@QuarkusTestResource(AzuriteResource.class)
class PspResourceTest {

  private static final String GET_FDR_CREATED_URL = "/psps/%s/created/fdrs/%s/organizations/%s";
  private static final String GET_PAYMENTS_FDR_PUBLISHED_URL = "/psps/%s/published/fdrs/%s/revisions/%s/organizations/%s/payments";
  private static final String GET_FDR_PUBLISHED_URL = "/psps/%s/published/fdrs/%s/revisions/%s/organizations/%s";
  private static final String GET_ALL_FDR_CREATED_URL = "/psps/%s/created";
  private static final String GET_PAYMENTS_FDR_CREATED_URL = "/psps/%s/created/fdrs/%s/organizations/%s/payments";


  protected static String PAYMENTS_SAME_INDEX_ADD_TEMPLATE =
      """
      {
        "payments": [{
            "index": 100,
            "iuv": "a",
            "iur": "abcdefg",
            "idTransfer": 1,
            "pay": 0.01,
            "payStatus": "EXECUTED",
            "payDate": "2023-02-03T12:00:30.900000Z"
          },{
            "index": 100,
            "iuv": "b",
            "iur": "abcdefg",
            "idTransfer": 2,
            "pay": 0.01,
            "payStatus": "REVOKED",
            "payDate": "2023-02-03T12:00:30.900000Z"
          }
        ]
      }
      """;

  protected static String PAYMENTS_2_ADD_TEMPLATE =
      """
      {
        "payments": [{
          "index": 100,
          "iuv": "a",
          "iur": "abcdefg",
          "idTransfer": 1,
          "pay": 0.01,
          "payStatus": "EXECUTED",
          "payDate": "2023-02-03T12:00:30.900000Z"
        }]
      }
      """;

  protected static String PAYMENTS_DELETE_WRONG_TEMPLATE =
      """
      {
        "indexList": [
            5
        ]
      }
      """;

  protected static String MALFORMED_JSON = """
    {
      12345
    }
    """;

  protected static String PAYMENTS_ADD_INVALID_FORMAT_TEMPLATE =
    """
    {
      "payments": {
          "index": 100,
          "iuv": "a",
          "iur": "abcdefg",
          "idTransfer": 1,
          "pay": "%s",
          "payStatus": "EXECUTED",
          "payDate": "2023-02-03T12:00:30.900000Z"
        }
    }
    """;

  protected static String FLOW_TEMPLATE_WRONG_INSTANT =
    """
        {
          "fdr": "%s",
          "fdrDate": "%s",
          "sender": {
            "type": "%s",
            "id": "SELBIT2B",
            "pspId": "%s",
            "pspName": "Bank",
            "pspBrokerId": "%s",
            "channelId": "%s",
            "password": "1234567890"
          },
          "receiver": {
            "id": "APPBIT2B",
            "organizationId": "%s",
            "organizationName": "Comune di xyz"
          },
          "regulation": "SEPA - Bonifico xzy",
          "regulationDate": "2023-04-03T12:00:30.900000Z",
          "bicCodePouringBank": "UNCRITMMXXX",
          "totPayments": 3,
          "sumPayments": 0.03
        }
        """;

    protected static String PAYMENTS_ADD_INVALID_FIELD_VALUE_FORMAT_TEMPLATE =
      """
      {
        "payments": [{
            "index": 100,
            "iuv": "a",
            "iur": "abcdefg",
            "idTransfer": 1,
            "pay": "%s",
            "payStatus": "EXECUTED",
            "payDate": "2023-02-03T12:00:30.900000Z"
          }
        ]
      }
      """;

  protected static String FLOW_TEMPLATE_WRONG_FIELDS =
      """
          {
            "fdrFake": "%s",
            "fdrDate": "2023-04-05T09:21:37.810000Z",
            "sender": {
              "type": "%s",
              "id": "SELBIT2B",
              "pspId": "%s",
              "pspName": "Bank",
              "pspBrokerId": "%s",
              "channelId": "%s",
              "password": "1234567890"
            },
            "receiver": {
              "id": "APPBIT2B",
              "organizationId": "%s",
              "organizationName": "Comune di xyz"
            },
            "regulation": "SEPA - Bonifico xzy",
            "regulationDate": "2023-04-03T12:00:30.900000Z",
            "bicCodePouringBank": "UNCRITMMXXX",
            "totPayments": 3,
            "sumPayments": 0.03
          }
          """;

    protected static String PAYMENTS_DELETE_TEMPLATE =
      """
      {
        "indexList": [
            100,
            101,
            102
        ]
      }
      """;

  @Test
  @DisplayName("PSPS - OK - inserimento completo e pubblicazione di un flusso")
  void test_psp_OK() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName);
  }

  @Test
  @DisplayName("PSPS - OK - inserimento di un flusso per tipo ABI_CODE")
  void test_psp_ABICODE_createFlow_OK() {
    String flowName = TestUtil.getDynamicFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt = TestUtil.FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.ABI_CODE.name(), PSP_CODE,
        BROKER_CODE, CHANNEL_CODE, EC_CODE);

    GenericResponse res = given()
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
  @DisplayName("PSPS - OK - inserimento di un flusso per tipo BIC_CODE")
  void test_psp_BIC_CODE_createFlow_OK() {
    String flowName = TestUtil.getDynamicFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.BIC_CODE.name(), PSP_CODE,
        BROKER_CODE, CHANNEL_CODE, EC_CODE);

    GenericResponse res = given()
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
  @DisplayName("PSPS - OK - inserimento e cancellazione di un flusso")
  void test_psp_deleteFlow_OK() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);

    GenericResponse resSave = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(urlSave)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    String urlDel = FLOWS_DELETE_URL.formatted(PSP_CODE, flowName);

    GenericResponse resDel = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .delete(urlDel)
        .then()
        .statusCode(200)
        .extract()
        .as(GenericResponse.class);
    assertThat(resDel.getMessage(), equalTo("Fdr [%s] deleted".formatted(flowName)));

    String urlDel2 = FLOWS_DELETE_URL.formatted(PSP_CODE, flowName);

    ErrorResponse resDelError = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .delete(urlDel2)
        .then()
        .statusCode(404)
        .extract()
        .as(ErrorResponse.class);
    assertThat(resDelError.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND.errorCode()));
    assertThat(resDelError.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Fdr [%s] not found", flowName)))));
  }

  @Test
  @DisplayName("PSPS - OK - inserimento completo e cancellazione del flusso con payments")
  void test_psp_deleteFlowWithPayment_OK() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);

    GenericResponse resSave = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(urlSave)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    String urlSavePayment = PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);

    GenericResponse resSavePays = given()
            .body(PAYMENTS_ADD_TEMPLATE)
            .header(HEADER)
            .when()
            .put(urlSavePayment)
            .then()
            .statusCode(200)
            .extract()
            .as(GenericResponse.class);
    assertThat(resSavePays.getMessage(), equalTo("Fdr [%s] payment added".formatted(flowName)));

    String urlDeleteFlow = FLOWS_DELETE_URL.formatted(PSP_CODE, flowName);

    GenericResponse resDelFlow = given()
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
  @DisplayName("PSPS - OK - inserimento completo e cancellazione dei payments")
  void test_psp_deletePayments_OK() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);

    GenericResponse resSave = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(urlSave)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    String urlSavePayment = PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);

    GenericResponse resSavePays = given()
        .body(PAYMENTS_ADD_TEMPLATE)
        .header(HEADER)
        .when()
        .put(urlSavePayment)
        .then()
        .statusCode(200)
        .extract()
        .as(GenericResponse.class);
    assertThat(resSavePays.getMessage(), equalTo("Fdr [%s] payment added".formatted(flowName)));

    GenericResponse resSavePays2 = given()
        .body(PAYMENTS_ADD_TEMPLATE_2)
        .header(HEADER)
        .when()
        .put(urlSavePayment)
        .then()
        .statusCode(200)
        .extract()
        .as(GenericResponse.class);
    assertThat(resSavePays.getMessage(), equalTo("Fdr [%s] payment added".formatted(flowName)));

    String urlDelPays = PAYMENTS_DELETE_URL.formatted(PSP_CODE, flowName);

    GenericResponse resDelPays = given()
        .body(PAYMENTS_DELETE_TEMPLATE)
        .header(HEADER)
        .when()
        .put(urlDelPays)
        .then()
        .statusCode(200)
        .extract()
        .as(GenericResponse.class);
    assertThat(resDelPays.getMessage(), equalTo("Fdr [%s] payment deleted".formatted(flowName)));

    ErrorResponse resDelError = given()
        .body(PAYMENTS_DELETE_TEMPLATE)
        .header(HEADER)
        .when()
        .put(urlDelPays)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(resDelError.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_NO_MATCH_INDEX.errorCode()));
    assertThat(resDelError.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Index of payment not match with index loaded on fdr [%s]", flowName)))));
  }

  @Test
  @DisplayName("PSPS - OK - inserimento completo e cancellazione parziale dei payments")
  void test_psp_deletePayments_partial_OK() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);

    GenericResponse resSave = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(urlSave)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    String urlSavePayment = PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);

    GenericResponse resSavePays = given()
        .body(PAYMENTS_ADD_TEMPLATE)
        .header(HEADER)
        .when()
        .put(urlSavePayment)
        .then()
        .statusCode(200)
        .extract()
        .as(GenericResponse.class);
    assertThat(resSavePays.getMessage(), equalTo("Fdr [%s] payment added".formatted(flowName)));

    String urlDelPays = PAYMENTS_DELETE_URL.formatted(PSP_CODE, flowName);

    GenericResponse resDelPays = given()
        .body(PAYMENTS_DELETE_TEMPLATE)
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
    String urlSave = FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);

    GenericResponse resSave = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(urlSave)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    String urlSavePayment = PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);

    GenericResponse resSavePays = given()
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
    String urlPublishFlow = FLOWS_PUBLISH_URL.formatted(PSP_CODE, flowNameWrong);

    ErrorResponse resDelError = given()
        .header(HEADER)
        .when()
        .post(urlPublishFlow)
        .then()
        .statusCode(404)
        .extract()
        .as(ErrorResponse.class);
    assertThat(resDelError.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND.errorCode()));
    assertThat(resDelError.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Fdr [%s] not found", flowNameWrong)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0701 - flow not found in add payments new flowName")
  void test_psp_payments_add_KO_FDR0701() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);

    GenericResponse resSave = given()
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
    String urlAddPayments = PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName2);
    ErrorResponse resDelError = given()
        .body(PAYMENTS_ADD_TEMPLATE)
        .header(HEADER)
        .when()
        .put(urlAddPayments)
        .then()
        .statusCode(404)
        .extract()
        .as(ErrorResponse.class);
    assertThat(resDelError.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND.errorCode()));
    assertThat(resDelError.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Fdr [%s] not found", flowName2)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0701 - flow not found in delete payments new flowName")
  void test_psp_payments_delete_KO_FDR0701() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);
    GenericResponse resSave = given()
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
    String urlDelPays = PAYMENTS_DELETE_URL.formatted(PSP_CODE, flowNameUnknown);
    ErrorResponse resDelError = given()
        .body(PAYMENTS_DELETE_TEMPLATE)
        .header(HEADER)
        .when()
        .put(urlDelPays)
        .then()
        .statusCode(404)
        .extract()
        .as(ErrorResponse.class);
    assertThat(resDelError.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND.errorCode()));
    assertThat(resDelError.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Fdr [%s] not found", flowNameUnknown)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0702 - flow already exists")
  void test_psp_KO_FDR0702() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);

    GenericResponse resSave = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(urlSave)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    ErrorResponse resDelError = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(urlSave)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(resDelError.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_ALREADY_EXIST.errorCode()));
    assertThat(resDelError.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Fdr [%s] already exist in [CREATED] status", flowName)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0703 - flow not found in publish flow CREATED")
  void test_psp_CREATED_publish_KO_FDR0703() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);

    GenericResponse resSave = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(urlSave)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    String urlPublishFlow = FLOWS_PUBLISH_URL.formatted(PSP_CODE, flowName);
    ErrorResponse resPublish = given()
        .header(HEADER)
        .when()
        .post(urlPublishFlow)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(resPublish.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION.errorCode()));
    assertThat(resPublish.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Fdr [%s] exist but in [CREATED] status", flowName)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0703 - reporting flow wrong action delete payments")
  void test_psp_payments_delete_KO_FDR0703() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);

    GenericResponse resSave = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(urlSave)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    String urlDelPays = PAYMENTS_DELETE_URL.formatted(PSP_CODE, flowName);
    ErrorResponse resDelError = given()
        .body(PAYMENTS_DELETE_TEMPLATE)
        .header(HEADER)
        .when()
        .put(urlDelPays)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(resDelError.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_WRONG_ACTION.errorCode()));
    assertThat(resDelError.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Fdr [%s] exist but in [CREATED] status", flowName)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0704 - psp param and psp body not match")
  void test_psp_KO_FDR0704() {
    String flowName = TestUtil.getDynamicFlowName();
    String pspNotMatch = "PSP_NOT_MATCH";

    String url = FLOWS_URL.formatted(PSP_CODE, flowName);
    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(), pspNotMatch,
        BROKER_CODE, CHANNEL_CODE, EC_CODE);
    ErrorResponse resDelError = given()
        .header(HEADER)
        .body(bodyFmt)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(resDelError.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_PSP_ID_NOT_MATCH.errorCode()));
    assertThat(resDelError.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Fdr [%s] have sender.pspId [%s] but not match with query param [%s]", flowName, pspNotMatch, PSP_CODE)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0705 - add payments with same index in same request")
  void test_psp_payments_add_KO_FDR0705() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);

    GenericResponse resSave = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(urlSave)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    String urlAddPays = PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);
    bodyFmt = PAYMENTS_SAME_INDEX_ADD_TEMPLATE;
    ErrorResponse resDelError = given()
        .header(HEADER)
        .body(bodyFmt)
        .when()
        .put(urlAddPays)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(resDelError.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_SAME_INDEX_IN_SAME_REQUEST.errorCode()));
    assertThat(resDelError.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Exist one or more payment index in same request on fdr [%s]", flowName)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0705 - delete payments with same index in same request")
  void test_psp_payments_delete_KO_FDR0705() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);

    GenericResponse resSave = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(urlSave)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    String urlSavePayment = PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);

    GenericResponse resSavePays = given()
        .body(PAYMENTS_ADD_TEMPLATE)
        .header(HEADER)
        .when()
        .put(urlSavePayment)
        .then()
        .statusCode(200)
        .extract()
        .as(GenericResponse.class);
    assertThat(resSavePays.getMessage(), equalTo("Fdr [%s] payment added".formatted(flowName)));

    String urlDelPays = PAYMENTS_DELETE_URL.formatted(PSP_CODE, flowName);
    bodyFmt = """
        {
          "indexList": [
              1,
              1
          ]
        }
        """;
    ErrorResponse resDelError = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .put(urlDelPays)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(resDelError.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_SAME_INDEX_IN_SAME_REQUEST.errorCode()));
    assertThat(resDelError.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Exist one or more payment index in same request on fdr [%s]", flowName)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0706 - payments with same index")
  void test_psp_KO_FDR0706() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);

    GenericResponse resSave = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(urlSave)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    String urlSavePayment = PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);

    GenericResponse resSavePays = given()
        .body(PAYMENTS_ADD_TEMPLATE)
        .header(HEADER)
        .when()
        .put(urlSavePayment)
        .then()
        .statusCode(200)
        .extract()
        .as(GenericResponse.class);
    assertThat(resSavePays.getMessage(), equalTo("Fdr [%s] payment added".formatted(flowName)));

    ErrorResponse resSavePays2 = given()
        .body(PAYMENTS_2_ADD_TEMPLATE)
        .header(HEADER)
        .when()
        .put(urlSavePayment)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(resSavePays2.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_DUPLICATE_INDEX.errorCode()));
    assertThat(resSavePays2.getErrors(), hasItem(hasProperty("message", equalTo(String.format("One or more payment index already added on fdr [%s]", flowName)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0707 - payments unknown index delete")
  void test_psp_KO_FDR0707() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlSave = FLOWS_URL.formatted(PSP_CODE, flowName);

    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
        PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);

    GenericResponse resSave = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(urlSave)
        .then()
        .statusCode(201)
        .extract()
        .as(GenericResponse.class);
    assertThat(resSave.getMessage(), equalTo("Fdr [%s] saved".formatted(flowName)));

    String urlSavePayment = PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);

    GenericResponse resSavePays = given()
        .body(PAYMENTS_ADD_TEMPLATE)
        .header(HEADER)
        .when()
        .put(urlSavePayment)
        .then()
        .statusCode(200)
        .extract()
        .as(GenericResponse.class);
    assertThat(resSavePays.getMessage(), equalTo("Fdr [%s] payment added".formatted(flowName)));

    String urlDelPays = PAYMENTS_DELETE_URL.formatted(PSP_CODE, flowName);
    ErrorResponse resDelError = given()
        .body(PAYMENTS_DELETE_WRONG_TEMPLATE)
        .header(HEADER)
        .when()
        .put(urlDelPays)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(resDelError.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_NO_MATCH_INDEX.errorCode()));
    assertThat(resDelError.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Index of payment not match with index loaded on fdr [%s]", flowName)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0708 - psp unknown")
  void test_psp_KO_FDR0708() {
    String flowName = TestUtil.getDynamicFlowName();
    String pspUnknown = "PSP_UNKNOWN";

    String url = FLOWS_URL.formatted(pspUnknown, flowName);
    String bodyFmt = FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(), pspUnknown,
        BROKER_CODE, CHANNEL_CODE, EC_CODE);
    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.PSP_UNKNOWN.errorCode()));
    assertThat(res.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Psp [%s] unknown", pspUnknown)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0709 - psp not enabled")
  void test_psp_KO_FDR0709() {
    String flowName = TestUtil.getDynamicFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE_NOT_ENABLED, flowName);
    String bodyFmt =
        FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
            PSP_CODE_NOT_ENABLED, BROKER_CODE, CHANNEL_CODE, EC_CODE);
    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.PSP_NOT_ENABLED.errorCode()));
    assertThat(res.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Psp [%s] not enabled", PSP_CODE_NOT_ENABLED)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0710 - brokerPsp unknown")
  void test_brokerpsp_KO_FDR0710() {
    String flowName = TestUtil.getDynamicFlowName();
    String brokerPspUnknown = "BROKERPSP_UNKNOWN";
    String url = FLOWS_URL.formatted(PSP_CODE, flowName);
    String bodyFmt =
        FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(), PSP_CODE, brokerPspUnknown,
            CHANNEL_CODE, EC_CODE);

    ErrorResponse resDelError = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(resDelError.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.BROKER_UNKNOWN.errorCode()));
    assertThat(resDelError.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Broker [%s] unknown", brokerPspUnknown)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0711 - brokerPsp not enabled")
  void test_brokerpsp_KO_FDR0711() {
    String flowName = TestUtil.getDynamicFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE, flowName);
    String bodyFmt =
        FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(), PSP_CODE,
            BROKER_CODE_NOT_ENABLED, CHANNEL_CODE, EC_CODE);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.BROKER_NOT_ENABLED.errorCode()));
    assertThat(res.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Broker [%s] not enabled", BROKER_CODE_NOT_ENABLED)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0712 - channel unknown")
  void test_channel_KO_FDR0712() {
    String flowName = TestUtil.getDynamicFlowName();
    String channelUnknown = "CHANNEL_UNKNOWN";
    String url = FLOWS_URL.formatted(PSP_CODE, flowName);
    String bodyFmt =
        FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(), PSP_CODE,
            BROKER_CODE, channelUnknown, EC_CODE);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.CHANNEL_UNKNOWN.errorCode()));
    assertThat(res.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Channel [%s] unknown", channelUnknown)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0713 - channel not enabled")
  void test_channel_KO_FDR0713() {
    String flowName = TestUtil.getDynamicFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE, flowName);
    String bodyFmt =
        FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(), PSP_CODE,
            BROKER_CODE, CHANNEL_CODE_NOT_ENABLED, EC_CODE);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.CHANNEL_NOT_ENABLED.errorCode()));
    assertThat(res.getErrors(), hasItem(hasProperty("message", equalTo("channelId.notEnabled"))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0714 - channel with brokerPsp not authorized")
  void test_channelBroker_KO_FDR0714() {
    String flowName = TestUtil.getDynamicFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE, flowName);
    String bodyFmt =
        FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(), PSP_CODE,
            BROKER_CODE_2, CHANNEL_CODE, EC_CODE);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.CHANNEL_BROKER_WRONG_CONFIG.errorCode()));
    assertThat(res.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Channel [%s] with broker [%s] not authorized", CHANNEL_CODE, BROKER_CODE_2)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0715 - channel with psp not authorized")
  void test_channelPsp_KO_FDR0715() {
    String flowName = TestUtil.getDynamicFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE_2, flowName);
    String bodyFmt =
        FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(),
            PSP_CODE_2, BROKER_CODE, CHANNEL_CODE, EC_CODE);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.CHANNEL_PSP_WRONG_CONFIG.errorCode()));
    assertThat(res.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Channel [%s] with psp [%s] not authorized", CHANNEL_CODE, PSP_CODE_2)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0716 - ec unknown")
  void test_ecId_KO_FDR0716() {
    String flowName = TestUtil.getDynamicFlowName();
    String ecUnknown = "EC_UNKNOWN";
    String url = FLOWS_URL.formatted(PSP_CODE, flowName);
    String bodyFmt =
        FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(), PSP_CODE,
            BROKER_CODE, CHANNEL_CODE, ecUnknown);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.EC_UNKNOWN.errorCode()));
    assertThat(res.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Creditor institution [%s] unknown", ecUnknown)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0717 - ec not enabled")
  void test_ecId_KO_FDR0717() {
    String flowName = TestUtil.getDynamicFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE, flowName);
    String bodyFmt =
        FLOW_TEMPLATE.formatted(flowName, SenderTypeEnumDto.LEGAL_PERSON.name(), PSP_CODE,
            BROKER_CODE, CHANNEL_CODE, EC_CODE_NOT_ENABLED);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.EC_NOT_ENABLED.errorCode()));
    assertThat(res.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Creditor institution [%s] not enabled", EC_CODE_NOT_ENABLED)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0718 - flow format wrong date")
  void test_flowName_KO_FDR0718() {
    String url = FLOWS_URL.formatted(PSP_CODE, REPORTING_FLOW_NAME_DATE_WRONG_FORMAT);
    String bodyFmt =
        FLOW_TEMPLATE.formatted(REPORTING_FLOW_NAME_DATE_WRONG_FORMAT, SenderTypeEnumDto.LEGAL_PERSON.name(),
            PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_DATE_WRONG_FORMAT.errorCode()));
    assertThat(res.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Fdr [2016-aa-16%s-1176] has wrong date", PSP_CODE)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0719 - flow format wrong psp")
  void test_flowName_KO_FDR0719() {
    String url = FLOWS_URL.formatted(PSP_CODE, REPORTING_FLOW_NAME_PSP_WRONG_FORMAT);
    String bodyFmt =
        FLOW_TEMPLATE.formatted(REPORTING_FLOW_NAME_PSP_WRONG_FORMAT, SenderTypeEnumDto.LEGAL_PERSON.name(),
            PSP_CODE, BROKER_CODE, CHANNEL_CODE, EC_CODE);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_PSP_WRONG_FORMAT.errorCode()));
    assertThat(res.getErrors(), hasItem(hasProperty("message", equalTo("Fdr [2016-08-16-psp-1176] has wrong psp"))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0400 - JSON input wrong fields")
  void test_psp_KO_FDR0400() {
    String flowName = TestUtil.getDynamicFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE, flowName);
    String bodyFmt = FLOW_TEMPLATE_WRONG_FIELDS.formatted(
        flowName,
        SenderTypeEnum.LEGAL_PERSON.name(),
        PSP_CODE,
        BROKER_CODE,
        CHANNEL_CODE,
        EC_CODE);

    ErrorResponse res = given()
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
    assertThat(res.getErrors(), hasItem(hasProperty("path", equalTo("create.createRequest.fdr"))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0401 - JSON incorrect value")
  void test_psp_KO_FDR0401() {
    String flowName = TestUtil.getDynamicFlowName();
    String url = PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);
    String wrongFormatDecimal = "0,01";
    String bodyFmt = PAYMENTS_ADD_INVALID_FIELD_VALUE_FORMAT_TEMPLATE.formatted(wrongFormatDecimal);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .put(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.BAD_REQUEST_INPUT_JSON.errorCode()));
    assertThat(res.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Bad request. Field [payments.pay] is [%s]. Not match a correct value", wrongFormatDecimal)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0402 - JSON invalid input instant")
  void test_psp_KO_FDR0402() {
    String flowName = TestUtil.getDynamicFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE, flowName);
    String wrongFormatDate = "2023-04-05";
    String bodyFmt = FLOW_TEMPLATE_WRONG_INSTANT.formatted(
        flowName,
        wrongFormatDate,
        SenderTypeEnum.LEGAL_PERSON.name(),
        PSP_CODE,
        BROKER_CODE,
        CHANNEL_CODE,
        EC_CODE);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.BAD_REQUEST_INPUT_JSON_INSTANT.errorCode()));
    assertThat(res.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Bad request. Field [fdrDate] is [%s]. Expected ISO-8601 [2011-12-03T10:15:30Z] [2023-04-05T09:21:37.810000Z]", wrongFormatDate)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0403 - JSON invalid input enum")
  void test_psp_KO_FDR0403() {
    String flowName = TestUtil.getDynamicFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE, flowName);
    String wrongEnum = "WRONG_ENUM";
    String bodyFmt = FLOW_TEMPLATE.formatted(
        flowName,
        wrongEnum,
        PSP_CODE,
        BROKER_CODE,
        CHANNEL_CODE,
        EC_CODE);

    ErrorResponse res = given()
        .body(bodyFmt)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.BAD_REQUEST_INPUT_JSON_ENUM.errorCode()));
    assertThat(res.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Bad request. Field [sender.type] is [%s]. Expected value one of [LEGAL_PERSON, ABI_CODE, BIC_CODE]", wrongEnum)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0404 - JSON deserialization error")
  void test_psp_KO_FDR0404() {
    String flowName = TestUtil.getDynamicFlowName();
    String url = PAYMENTS_ADD_URL.formatted(PSP_CODE, flowName);

    ErrorResponse resDelError = given()
        .body(PAYMENTS_ADD_INVALID_FORMAT_TEMPLATE)
        .header(HEADER)
        .when()
        .put(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(resDelError.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.BAD_REQUEST_INPUT_JSON_DESERIALIZE_ERROR.errorCode()));
    assertThat(resDelError.getErrors(), hasItem(hasProperty("message", equalTo("Bad request. Field [payments] generate an deserialize error. Set correct value"))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0405 - JSON malformed")
  void test_psp_KO_FDR0405() {
    String flowName = TestUtil.getDynamicFlowName();
    String url = FLOWS_URL.formatted(PSP_CODE, flowName);

    ErrorResponse res = given()
        .body(MALFORMED_JSON)
        .header(HEADER)
        .when()
        .post(url)
        .then()
        .statusCode(400)
        .extract()
        .as(ErrorResponse.class);
    assertThat(res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.BAD_REQUEST_INPUT_JSON_NON_VALID_FORMAT.errorCode()));
    assertThat(res.getErrors(), hasItem(hasProperty("message", equalTo("Bad request. Json format not valid"))));
  }





  /** ############### getAllPublishedFlow ################ */
  @Test
  @DisplayName("PSPS - OK - getAllPublishedFlow")
  void testOrganization_getAllPublishedFlow_Ok() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName);
    String url = GET_FDR_PUBLISHED_URL.formatted(PSP_CODE, flowName, 1, EC_CODE);
    GetResponse res =
            given()
                    .header(HEADER)
                    .when()
                    .get(url)
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(GetResponse.class);
    assertThat(res.getTotPayments(), equalTo(4L));
    assertThat(res.getStatus(), equalTo(ReportingFlowStatusEnum.PUBLISHED));
  }

  @Test
  @DisplayName("ORGANIZATIONS - OK - getAllPublishedFlow no results")
  void test_psp_getAllPublishedFlow_OkNoResults() {
    String flowName = TestUtil.getDynamicFlowName();

    String urlPspFlow = FLOWS_URL.formatted(PSP_CODE, flowName);
    String bodyFmtPspFlow =
            FLOW_TEMPLATE.formatted(
                    flowName,
                    SenderTypeEnumDto.LEGAL_PERSON.name(),
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

    String url = GET_FDR_PUBLISHED_URL.formatted(PSP_CODE, flowName, 1, EC_CODE);
    ErrorResponse res = given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(404)
            .extract()
            .as(ErrorResponse.class);
    assertThat(res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND.errorCode()));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0708 - psp unknown")
  void test_psp_getAllPublishedFlow_KO_FDR0708() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName);
    String pspUnknown = "PSP_UNKNOWN";
    String url = GET_FDR_PUBLISHED_URL.formatted(pspUnknown, flowName, 1, EC_CODE);
    ErrorResponse res = given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(400)
            .extract()
            .as(ErrorResponse.class);
    assertThat(res.getHttpStatusDescription(), equalTo("Bad Request"));
    assertThat(res.getAppErrorCode(), equalTo("FDR-0708"));
    assertThat(res.getErrors(), hasSize(1));
    assertThat(res.getErrors(), hasItem(hasProperty("message", equalTo(String.format("Psp [%s] unknown",pspUnknown)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0709 - psp not enabled")
  void test_psp_getAllPublishedFlow_KO_FDR0709() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName);
    String url = GET_FDR_PUBLISHED_URL.formatted(PSP_CODE_NOT_ENABLED, flowName, 1, EC_CODE);

    ErrorResponse res = given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(400)
            .extract()
            .as(ErrorResponse.class);
    assertThat(res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.PSP_NOT_ENABLED.errorCode()));
    assertThat(res.getErrors(), hasItem(hasProperty("message", equalTo("Psp [%s] not enabled".formatted(PSP_CODE_NOT_ENABLED)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0716 - creditor institution unknown")
  void test_psp_getAllPublishedFlow_KO_FDR0716() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName);
    String ecUnknown = "EC_UNKNOWN";
    String url = GET_FDR_PUBLISHED_URL.formatted(PSP_CODE, flowName, 1, ecUnknown);

    ErrorResponse res = given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(400)
            .extract()
            .as(ErrorResponse.class);
    assertThat(res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.EC_UNKNOWN.errorCode()));
    assertThat(res.getErrors(), hasItem(hasProperty("message", equalTo("Creditor institution [%s] unknown".formatted(ecUnknown)))));
  }

  @Test
  @DisplayName("PSPS - KO FDR-0717 - creditor institution not enabled")
  void test_psp_getAllPublishedFlow_KO_FDR0717() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName);
    String url = GET_FDR_PUBLISHED_URL.formatted(PSP_CODE, flowName, 1, EC_CODE_NOT_ENABLED);

    ErrorResponse res = given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(400)
            .extract()
            .as(ErrorResponse.class);
    assertThat(res.getAppErrorCode(), equalTo(AppErrorCodeMessageEnum.EC_NOT_ENABLED.errorCode()));
    assertThat(res.getErrors(), hasItem(hasProperty("message", equalTo("Creditor institution [%s] not enabled".formatted(EC_CODE_NOT_ENABLED)))));
  }

  /** ################# getReportingFlow ############### */
  @Test
  @DisplayName("PSPS - OK - recupero di un reporting flow")
  void test_psp_getReportingFlow_Ok() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName);
    String url = GET_FDR_PUBLISHED_URL.formatted(PSP_CODE, flowName, 1L, EC_CODE);
    GetResponse res = given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(GetResponse.class);
    assertThat(res.getFdr(), equalTo(flowName));
    assertThat(res.getReceiver().getOrganizationId(), equalTo(EC_CODE));
    assertThat(res.getSender().getPspId(), equalTo(PSP_CODE));
    assertThat(res.getStatus(), equalTo(ReportingFlowStatusEnum.PUBLISHED));
    assertThat(res.getComputedTotPayments(), equalTo(4L));
  }

  @Test
  @DisplayName("PSPS - OK - recupero di un reporting flow pubblicato alla revision 2")
  void test_psp_getReportingFlow_revision_2_OK() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName);
    TestUtil.pspSunnyDay(flowName);

    String url = GET_FDR_PUBLISHED_URL.formatted(PSP_CODE, flowName, 2L, EC_CODE);
    GetResponse res = given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(GetResponse.class);
    assertThat(res.getFdr(), equalTo(flowName));
    assertThat(res.getRevision(), equalTo(2L));
    assertThat(res.getStatus(), equalTo(ReportingFlowStatusEnum.PUBLISHED));
  }

  @Test
  @DisplayName("PSPS - OK - nessun flusso trovato in stato CREATED per uno specifico PSP")
  void test_psp_getAllReportingFlowCreated_OK() {
    String url = (GET_ALL_FDR_CREATED_URL+"?page=2&size=1").formatted(PSP_CODE_3);

    GetAllCreatedResponse res = given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(GetAllCreatedResponse.class);
    assertThat(res.getCount(), equalTo(0L));
  }

  /** ################# getReportingFlowPayments ############### */
  @Test
  @DisplayName("PSPS - OK - recupero dei payments di un flow pubblicato")
  void test_psp_getReportingFlowPaymentsPublished_Ok() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName);

    String url = GET_PAYMENTS_FDR_PUBLISHED_URL.formatted(PSP_CODE, flowName, 1L, EC_CODE);

    GetPaymentResponse res = given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(GetPaymentResponse.class);
    assertThat(res.getCount(), equalTo(4L));
    List expectedList = List.of(PaymentStatusEnum.EXECUTED.name(), PaymentStatusEnum.REVOKED.name(), PaymentStatusEnum.NO_RPT.name(), PaymentStatusEnum.STAND_IN.name());
    assertThat(res.getData().stream().map(o -> o.getPayStatus().name()).toList(),
            equalTo(expectedList));
    assertThat(res.getData().stream().map(o -> o.getPayStatus().name()).toList(),
            containsInAnyOrder(expectedList.toArray()));
  }

  @Test
  @DisplayName("PSPS - OK - recupero dei flow created")
  void test_psp_getReportingFlows_created_Ok() {
    String flowName = TestUtil.getDynamicFlowName();
    String urlPspFlow = FLOWS_URL.formatted(PSP_CODE, flowName);
    String bodyFmtPspFlow =
            FLOW_TEMPLATE.formatted(
                    flowName,
                    SenderTypeEnumDto.LEGAL_PERSON.name(),
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

    String url = (GET_ALL_FDR_CREATED_URL).formatted(PSP_CODE);
    GetAllCreatedResponse res = given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(GetAllCreatedResponse.class);

    assertThat(res.getCount(), greaterThan(0L));
//    assertThat(res.getData(), contains(hasProperty("fdr", is(flowName))));
    assertTrue(res.getData().stream().anyMatch(item -> item.getFdr().equals(flowName)));
  }

  @Test
  @DisplayName("PSPS - OK - recupero dei payments di un flow creato")
  void test_psp_getReportingFlowPayments_created_Ok() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName);

    String url = (GET_PAYMENTS_FDR_CREATED_URL).formatted(PSP_CODE, flowName, EC_CODE);
    GetPaymentResponse res = given()
            .header(HEADER)
            .when()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .as(GetPaymentResponse.class);
    List<Payment> data = res.getData();

    assertThat(res.getCount(), equalTo(0L));
  }


}
