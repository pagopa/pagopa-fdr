package it.gov.pagopa.fdr.service.history;

import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.service.HistoryService;
import it.gov.pagopa.fdr.util.FileUtil;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
class HistoryServiceTest {

  @Inject HistoryService historyService;
  @Inject FileUtil fileUtil;

  @ConfigProperty(name = "json.schema.version")
  String jsonSchemaVersion;

  @Test
  void testHistoryJsonValidation_OK() {
    String jsonSchema =
        fileUtil.convertToString(
            fileUtil.getFileFromResourceAsStream(
                "/schema-json/fdr_history_schema_" + jsonSchemaVersion.toLowerCase() + ".json"));
    String jsonString =
        """
            {
              "status": "PUBLISHED",
              "revision": 1,
              "created": "2024-01-26T08:42:17.708Z",
              "updated": "2024-01-26T08:42:21.788414200Z",
              "fdr": "2024-01-2660000000001-17062585352",
              "fdrDate": "2024-01-26T08:42:15.242Z",
              "regulation": "SEPA - Bonifico xzy",
              "regulationDate": "2024-01-26T08:42:15.242Z",
              "bicCodePouringBank": "UNCRITMMXXX",
              "sender": {
                "type": "LEGAL_PERSON",
                "id": "SELBIT2B",
                "pspId": "60000000001",
                "pspName": "Bank",
                "pspBrokerId": "60000000001",
                "channelId": "15376371009_04",
                "password": "PLACEHOLDER"
              },
              "receiver": {
                "id": "APPBIT2B",
                "organizationId": "15376371009",
                "organizationName": "Comune di xyz"
              },
              "published": "2024-01-26T08:42:21.788414200Z",
              "computedTotPayments": 3,
              "computedSumPayments": 0.03,
              "totPayments": 3,
              "sumPayments": 0.03,
              "paymentList": [
                {
                  "iuv": "84100314577508a",
                  "iur": "6208035415a",
                  "index": 1,
                  "pay": 0.01,
                  "payStatus": "EXECUTED",
                  "payDate": "2023-02-03T12:00:30.900Z"
                },
                {
                  "iuv": "84100314577508b",
                  "iur": "6208035415b",
                  "index": 2,
                  "pay": 0.01,
                  "payStatus": "EXECUTED",
                  "payDate": "2023-02-03T12:00:30.900Z"
                },
                {
                  "iuv": "84100314577508c",
                  "iur": "6208035415c",
                  "index": 3,
                  "pay": 0.01,
                  "payStatus": "EXECUTED",
                  "payDate": "2023-02-03T12:00:30.900Z"
                }
              ]
            }
            """;
    Assertions.assertDoesNotThrow(() -> historyService.isJsonValid(jsonString, jsonSchema));
  }

  @Test
  void testHistoryJsonValidation_Error() {
    String jsonSchema =
        fileUtil.convertToString(
            fileUtil.getFileFromResourceAsStream(
                "/schema-json/fdr_history_schema_" + jsonSchemaVersion.toLowerCase() + ".json"));
    String jsonString =
        """
            {
              "status": "PUBLISHED",
              "revision": "1",
              "created": "2024-01-26T08:42:17.708Z",
              "updated": "2024-01-26T08:42:21.788414200Z",
              "fdr": "2024-01-2660000000001-17062585352",
              "fdrDate": "2024-01-26T08:42:15.242Z",
              "regulation": "SEPA - Bonifico xzy",
              "regulationDate": "2024-01-26T08:42:15.242Z",
              "bicCodePouringBank": "UNCRITMMXXX",
              "sender": {
                "type": "LEGAL_PERSON",
                "id": "SELBIT2B",
                "pspId": "60000000001",
                "pspName": "Bank",
                "pspBrokerId": "60000000001",
                "channelId": "15376371009_04",
                "password": "PLACEHOLDER"
              },
              "receiver": {
                "id": "APPBIT2B",
                "organizationId": "15376371009",
                "organizationName": "Comune di xyz"
              },
              "published": "2024-01-26T08:42:21.788414200Z",
              "computedTotPayments": 3,
              "computedSumPayments": 0.03,
              "totPayments": 3,
              "sumPayments": 0.03,
              "paymentList": [
                {
                  "iuv": "84100314577508a",
                  "iur": "6208035415a",
                  "index": 1,
                  "pay": 0.01,
                  "payStatus": "EXECUTED",
                  "payDate": "2023-02-03T12:00:30.900Z"
                },
                {
                  "iuv": "84100314577508b",
                  "iur": "6208035415b",
                  "index": 2,
                  "pay": 0.01,
                  "payStatus": "EXECUTED",
                  "payDate": "2023-02-03T12:00:30.900Z"
                },
                {
                  "iuv": "84100314577508c",
                  "iur": "6208035415c",
                  "index": 3,
                  "pay": 0.01,
                  "payStatus": "EXECUTED",
                  "payDate": "2023-02-03T12:00:30.900Z"
                }
              ]
            }
            """;
    Assertions.assertThrows(
        AppException.class, () -> historyService.isJsonValid(jsonString, jsonSchema));
  }
}
