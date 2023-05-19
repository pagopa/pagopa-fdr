package it.gov.pagopa.fdr.rest.organizations;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.Header;
import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.service.dto.FlowDto;
import it.gov.pagopa.fdr.service.dto.MetadataDto;
import it.gov.pagopa.fdr.service.dto.ReceiverDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowByIdEcDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowGetDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowStatusEnumDto;
import it.gov.pagopa.fdr.service.dto.SenderDto;
import it.gov.pagopa.fdr.service.dto.SenderTypeEnumDto;
import it.gov.pagopa.fdr.service.organizations.OrganizationsService;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;
import org.openapi.quarkus.api_config_cache_json.model.CreditorInstitution;
import org.openapi.quarkus.api_config_cache_json.model.PaymentServiceProvider;

@QuarkusTest
public class OrganizationResourceTest {

  private static final String reportingFlowName = "2016-08-16pspTest-1176";
  private static final String pspCode = "pspTest";
  private static final String pspCode2 = "pspTest2";
  private static final String pspCodeNotEnabled = "pspNotEnabled";
  private static final String brokerCode = "intTest";
  private static final String channelCode = "canaleTest";
  private static final String ecCode = "12345678900";
  private static final String ecCodeNotEnabled = "00987654321";
  private static final Header header = new Header("Content-Type", "application/json");

  private static final String organizationFindByIdEcUrl =
      "/organizations/%s/flows?idPsp=%s&page=%d&size=%d";
  private static final String organizationfindByReportingFlowNameUrl =
      "/organizations/%s/flows/%s/psps/%s";

  private static ReportingFlowByIdEcDto reportingFlowByIdEc =
      ReportingFlowByIdEcDto.builder()
          .count(1L)
          .data(List.of(FlowDto.builder().name(reportingFlowName).pspId(pspCode).build()))
          .metadata(MetadataDto.builder().totPage(1).pageSize(10).pageNumber(1).build())
          .build();

  private static ReportingFlowByIdEcDto reportingFlowByIdEcNoResults =
      ReportingFlowByIdEcDto.builder()
          .count(0L)
          .data(Collections.emptyList())
          .metadata(MetadataDto.builder().totPage(1).pageSize(10).pageNumber(1).build())
          .build();

  private static ReportingFlowGetDto reportingFlowGet =
      ReportingFlowGetDto.builder()
          .revision(1L)
          .created(Instant.now())
          .updated(Instant.now())
          .status(ReportingFlowStatusEnumDto.CREATED)
          .reportingFlowName(reportingFlowName)
          .reportingFlowDate(Instant.parse("2023-04-05T09:21:37.810000Z"))
          .sender(
              SenderDto.builder()
                  .type(SenderTypeEnumDto.LEGAL_PERSON)
                  .id("SELBIT2B")
                  .pspId(pspCode)
                  .pspName("Bank")
                  .brokerId(brokerCode)
                  .channelId(channelCode)
                  .password("1234567890")
                  .build())
          .receiver(ReceiverDto.builder().ecId(ecCode).ecName("Comune di xyz").build())
          .regulation("SEPA - Bonifico xzy")
          .regulationDate(Instant.parse("2023-04-03T12:00:30.900000Z"))
          .bicCodePouringBank("UNCRITMMXXX")
          .build();

  @InjectMock Config config;

  @InjectMock OrganizationsService organizationsService;

  ObjectMapper mapper;

  @BeforeEach
  void setup() {
    mapper =
        new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    Mockito.doNothing().when(config).init();
    Mockito.when(config.getClonedCache()).thenReturn(getConfig());
  }

  /** ############### findByIdEc ################ */
  @Test
  @DisplayName("ORGANIZATIONS findByIdEc Ok")
  public void testOrganization_findByIdEc_Ok() throws JsonProcessingException {
    Mockito.when(
            organizationsService.findByIdEc(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(reportingFlowByIdEc);

    String url = organizationFindByIdEcUrl.formatted(ecCode, pspCode, 10, 10);
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    given()
        .header(header)
        .when()
        .get(url)
        .then()
        .statusCode(200)
        .body(containsString(mapper.writeValueAsString(reportingFlowByIdEc)));
  }

  @Test
  @DisplayName("ORGANIZATIONS findByIdEc no results")
  public void testOrganization_findByIdEc_OkNoResults() throws JsonProcessingException {
    Mockito.when(
            organizationsService.findByIdEc(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(reportingFlowByIdEcNoResults);

    String url = organizationFindByIdEcUrl.formatted(ecCode, pspCode, 10, 10);
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    given()
        .header(header)
        .when()
        .get(url)
        .then()
        .statusCode(200)
        .body(containsString(mapper.writeValueAsString(reportingFlowByIdEcNoResults)));
  }

  @Test
  @DisplayName("ORGANIZATIONS findByIdEc KO FDR-0708")
  public void testOrganization_findByIdEc_KO_FDR0708() {
    String pspUnknown = "PSP_UNKNOWN";
    String url = organizationFindByIdEcUrl.formatted(ecCode, pspUnknown, 10, 10);
    String responseFmt =
        """
        {"httpStatusCode":400,"httpStatusDescription":"Bad Request","appErrorCode":"FDR-0708","errors":[{"message":"Psp [PSP_UNKNOWN] unknown"}]}
        """;
    given().header(header).when().get(url).then().statusCode(400).body(containsString(responseFmt));
  }

  @Test
  @DisplayName("ORGANIZATIONS findByIdEc KO FDR-0709")
  public void testOrganization_findByIdEc_KO_FDR0709() {
    String url = organizationFindByIdEcUrl.formatted(ecCode, pspCodeNotEnabled, 10, 10);
    String responseFmt =
        """
        {"httpStatusCode":400,"httpStatusDescription":"Bad Request","appErrorCode":"FDR-0709","errors":[{"message":"Psp [pspNotEnabled] not enabled"}]}
        """;

    given().header(header).when().get(url).then().statusCode(400).body(containsString(responseFmt));
  }

  @Test
  @DisplayName("ORGANIZATIONS findByIdEc KO FDR-0716")
  public void testOrganization_findByIdEc_KO_FDR0716() {
    String ecUnknown = "EC_UNKNOWN";
    String url = organizationFindByIdEcUrl.formatted(ecUnknown, pspCode, 10, 10);
    String responseFmt =
        """
        {"httpStatusCode":400,"httpStatusDescription":"Bad Request","appErrorCode":"FDR-0716","errors":[{"message":"Creditor institution [EC_UNKNOWN] unknown"}]}
        """;

    given().header(header).when().get(url).then().statusCode(400).body(containsString(responseFmt));
  }

  @Test
  @DisplayName("ORGANIZATIONS findByIdEc KO FDR-0717")
  public void testOrganization_findByIdEc_KO_FDR0717() {
    String url = organizationFindByIdEcUrl.formatted(ecCodeNotEnabled, pspCode, 10, 10);
    String responseFmt =
        """
        {"httpStatusCode":400,"httpStatusDescription":"Bad Request","appErrorCode":"FDR-0717","errors":[{"message":"Creditor institution [%s] not enabled"}]}"""
            .formatted(ecCodeNotEnabled);

    given().header(header).when().get(url).then().statusCode(400).body(containsString(responseFmt));
  }

  /** ################# findByReportingFlowName ############### */
  @Test
  @DisplayName("ORGANIZATIONS findByReportingFlowName Ok")
  public void testOrganization_findByReportingFlowName_Ok() throws JsonProcessingException {
    Mockito.when(
            organizationsService.findByReportingFlowName(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(reportingFlowGet);
    String url =
        organizationfindByReportingFlowNameUrl.formatted(ecCode, reportingFlowName, pspCode);

    given()
        .header(header)
        .when()
        .get(url)
        .then()
        .statusCode(200)
        .body(containsString(mapper.writeValueAsString(reportingFlowGet)));
  }

  private static ConfigDataV1 getConfig() {
    PaymentServiceProvider paymentServiceProvider = new PaymentServiceProvider();
    paymentServiceProvider.setEnabled(true);
    paymentServiceProvider.setPspCode(pspCode);

    PaymentServiceProvider paymentServiceProviderNotEnabled = new PaymentServiceProvider();
    paymentServiceProviderNotEnabled.setEnabled(false);
    paymentServiceProviderNotEnabled.setPspCode(pspCodeNotEnabled);

    PaymentServiceProvider paymentServiceProvider2 = new PaymentServiceProvider();
    paymentServiceProvider2.setEnabled(true);
    paymentServiceProvider2.setPspCode(pspCode2);

    Map<String, PaymentServiceProvider> psps = new LinkedHashMap<>();
    psps.put(pspCode, paymentServiceProvider);
    psps.put(pspCodeNotEnabled, paymentServiceProviderNotEnabled);

    CreditorInstitution creditorInstitution = new CreditorInstitution();
    creditorInstitution.setCreditorInstitutionCode(ecCode);
    creditorInstitution.setEnabled(true);

    CreditorInstitution creditorInstitutionNotEnabled = new CreditorInstitution();
    creditorInstitutionNotEnabled.setCreditorInstitutionCode(ecCodeNotEnabled);
    creditorInstitutionNotEnabled.setEnabled(false);

    Map<String, CreditorInstitution> creditorInstitutionMap = new LinkedHashMap<>();
    creditorInstitutionMap.put(ecCode, creditorInstitution);
    creditorInstitutionMap.put(ecCodeNotEnabled, creditorInstitutionNotEnabled);

    ConfigDataV1 configDataV1 = new ConfigDataV1();
    configDataV1.setPsps(psps);
    configDataV1.setCreditorInstitutions(creditorInstitutionMap);

    return configDataV1;
  }
}
