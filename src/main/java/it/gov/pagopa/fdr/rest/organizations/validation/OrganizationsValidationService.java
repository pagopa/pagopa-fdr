package it.gov.pagopa.fdr.rest.organizations.validation;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.Optional;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;
import org.openapi.quarkus.api_config_cache_json.model.CreditorInstitution;
import org.openapi.quarkus.api_config_cache_json.model.PaymentServiceProvider;

@ApplicationScoped
public class OrganizationsValidationService {

  @Inject Logger log;

  @WithSpan(kind = SERVER)
  public void validateGetAllByEc(String ecId, String pspId, ConfigDataV1 configData) {
    log.debug("Validate get all by ec");

    // check psp
    PaymentServiceProvider paymentServiceProvider =
        Optional.ofNullable(configData.getPsps().get(pspId))
            .orElseThrow(() -> new AppException(AppErrorCodeMessageEnum.PSP_UNKNOWN, pspId));

    if (paymentServiceProvider.getEnabled() == null || !paymentServiceProvider.getEnabled()) {
      throw new AppException(AppErrorCodeMessageEnum.PSP_NOT_ENABLED, pspId);
    }

    // check ec
    CreditorInstitution ec =
        Optional.ofNullable(configData.getCreditorInstitutions().get(ecId))
            .orElseThrow(() -> new AppException(AppErrorCodeMessageEnum.EC_UNKNOWN, ecId));

    if (ec.getEnabled() == null || !ec.getEnabled()) {
      throw new AppException(AppErrorCodeMessageEnum.EC_NOT_ENABLED, ecId);
    }
  }

  @WithSpan(kind = SERVER)
  public void validateGet(String fdr, String ecId, String pspId, ConfigDataV1 configData) {
    log.debug("Validate get");

    // check psp
    PaymentServiceProvider paymentServiceProvider =
        Optional.ofNullable(configData.getPsps().get(pspId))
            .orElseThrow(() -> new AppException(AppErrorCodeMessageEnum.PSP_UNKNOWN, pspId));

    if (paymentServiceProvider.getEnabled() == null || !paymentServiceProvider.getEnabled()) {
      throw new AppException(AppErrorCodeMessageEnum.PSP_NOT_ENABLED, pspId);
    }

    // check ec
    CreditorInstitution ec =
        Optional.ofNullable(configData.getCreditorInstitutions().get(ecId))
            .orElseThrow(() -> new AppException(AppErrorCodeMessageEnum.EC_UNKNOWN, ecId));

    if (ec.getEnabled() == null || !ec.getEnabled()) {
      throw new AppException(AppErrorCodeMessageEnum.EC_NOT_ENABLED, ecId);
    }

    // check reportingFlowName format
    String date = fdr.substring(0, 10);
    try {
      // default, ISO_LOCAL_DATE ("2016-08-16")
      LocalDate.parse(date);
    } catch (RuntimeException e) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_DATE_WRONG_FORMAT, fdr);
    }

    String name = fdr.substring(10);
    boolean nameWrongFromat = !name.startsWith(String.format("%s-", pspId));
    if (nameWrongFromat) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_PSP_WRONG_FORMAT, fdr);
    }
  }

  @WithSpan(kind = SERVER)
  public void validateGetPayment(String fdr, String ecId, String pspId, ConfigDataV1 configData) {
    log.debug("Validate get payment");

    // check psp
    PaymentServiceProvider paymentServiceProvider =
        Optional.ofNullable(configData.getPsps().get(pspId))
            .orElseThrow(() -> new AppException(AppErrorCodeMessageEnum.PSP_UNKNOWN, pspId));

    if (paymentServiceProvider.getEnabled() == null || !paymentServiceProvider.getEnabled()) {
      throw new AppException(AppErrorCodeMessageEnum.PSP_NOT_ENABLED, pspId);
    }

    // check ec
    CreditorInstitution ec =
        Optional.ofNullable(configData.getCreditorInstitutions().get(ecId))
            .orElseThrow(() -> new AppException(AppErrorCodeMessageEnum.EC_UNKNOWN, ecId));

    if (ec.getEnabled() == null || !ec.getEnabled()) {
      throw new AppException(AppErrorCodeMessageEnum.EC_NOT_ENABLED, ecId);
    }

    // check reportingFlowName format
    String date = fdr.substring(0, 10);
    try {
      // default, ISO_LOCAL_DATE ("2016-08-16")
      LocalDate.parse(date);
    } catch (RuntimeException e) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_DATE_WRONG_FORMAT, fdr);
    }

    String name = fdr.substring(10);
    boolean nameWrongFromat = !name.startsWith(String.format("%s-", pspId));
    if (nameWrongFromat) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_PSP_WRONG_FORMAT, fdr);
    }
  }

  @WithSpan(kind = SERVER)
  public void validateChangeReadFlag(
      String fdr, String ecId, String pspId, ConfigDataV1 configData) {
    log.debug("Validate change read flag");

    // check psp
    PaymentServiceProvider paymentServiceProvider =
        Optional.ofNullable(configData.getPsps().get(pspId))
            .orElseThrow(() -> new AppException(AppErrorCodeMessageEnum.PSP_UNKNOWN, pspId));

    if (paymentServiceProvider.getEnabled() == null || !paymentServiceProvider.getEnabled()) {
      throw new AppException(AppErrorCodeMessageEnum.PSP_NOT_ENABLED, pspId);
    }

    // check ec
    CreditorInstitution ec =
        Optional.ofNullable(configData.getCreditorInstitutions().get(ecId))
            .orElseThrow(() -> new AppException(AppErrorCodeMessageEnum.EC_UNKNOWN, ecId));

    if (ec.getEnabled() == null || !ec.getEnabled()) {
      throw new AppException(AppErrorCodeMessageEnum.EC_NOT_ENABLED, ecId);
    }

    // check reportingFlowName format
    String date = fdr.substring(0, 10);
    try {
      // default, ISO_LOCAL_DATE ("2016-08-16")
      LocalDate.parse(date);
    } catch (RuntimeException e) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_DATE_WRONG_FORMAT, fdr);
    }

    String name = fdr.substring(10);
    boolean nameWrongFromat = !name.startsWith(String.format("%s-", pspId));
    if (nameWrongFromat) {
      throw new AppException(AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_PSP_WRONG_FORMAT, fdr);
    }
  }
}
