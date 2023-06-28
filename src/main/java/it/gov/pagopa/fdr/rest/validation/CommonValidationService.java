package it.gov.pagopa.fdr.rest.validation;

import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.Optional;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.model.BrokerPsp;
import org.openapi.quarkus.api_config_cache_json.model.Channel;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;
import org.openapi.quarkus.api_config_cache_json.model.CreditorInstitution;
import org.openapi.quarkus.api_config_cache_json.model.PaymentServiceProvider;
import org.openapi.quarkus.api_config_cache_json.model.PspChannelPaymentType;

@ApplicationScoped
public class CommonValidationService {

  public void checkPspSender(Logger log, String psp, String pspId, String reportingFlowName) {
    log.debugf("Check match between psp[%s], sender.pspId[%s]", psp, pspId);
    if (!psp.equals(pspId)) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PSP_ID_NOT_MATCH, reportingFlowName, pspId, psp);
    }
  }

  public void checkFlowName(Logger log, String fdr, String reportingFlowName) {
    log.debugf("Check match between fdr[%s], reportingFlowName[%s]", fdr, reportingFlowName);
    if (!fdr.equals(reportingFlowName)) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_NOT_MATCH, reportingFlowName, fdr);
    }
  }

  public PaymentServiceProvider checkPaymentServiceProvider(
      Logger log, String psp, ConfigDataV1 configData) {
    log.debugf("Check psp[%s]", psp);
    PaymentServiceProvider paymentServiceProvider =
        Optional.ofNullable(configData.getPsps().get(psp))
            .orElseThrow(() -> new AppException(AppErrorCodeMessageEnum.PSP_UNKNOWN, psp));

    log.debugf("Check psp[%s] enabled", psp);
    if (paymentServiceProvider.getEnabled() == null || !paymentServiceProvider.getEnabled()) {
      throw new AppException(AppErrorCodeMessageEnum.PSP_NOT_ENABLED, psp);
    }
    return paymentServiceProvider;
  }

  public BrokerPsp checkBrokerPsp(Logger log, String brokerId, ConfigDataV1 configData) {
    log.debugf("Check brokerPsp[%s]", brokerId);
    BrokerPsp brokerPsp =
        Optional.ofNullable(configData.getPspBrokers().get(brokerId))
            .orElseThrow(() -> new AppException(AppErrorCodeMessageEnum.BROKER_UNKNOWN, brokerId));

    log.debugf("Check brokerPsp[%s] enabled", brokerId);
    if (brokerPsp.getEnabled() == null || !brokerPsp.getEnabled()) {
      throw new AppException(AppErrorCodeMessageEnum.BROKER_NOT_ENABLED, brokerId);
    }
    return brokerPsp;
  }

  public Channel checkChannel(Logger log, String channelId, ConfigDataV1 configData) {
    log.debugf("Check channel[%s]", channelId);
    Channel channel =
        Optional.ofNullable(configData.getChannels().get(channelId))
            .orElseThrow(
                () -> new AppException(AppErrorCodeMessageEnum.CHANNEL_UNKNOWN, channelId));

    log.debugf("Check channel[%s] enabled", channelId);
    if (channel.getEnabled() == null || !channel.getEnabled()) {
      throw new AppException(AppErrorCodeMessageEnum.CHANNEL_NOT_ENABLED, channelId);
    }
    return channel;
  }

  public void checkChannelBrokerPsp(
      Logger log, Channel channel, String channelId, BrokerPsp brokerPsp, String brokerId) {
    log.debugf("Check conjunction between channel[%s], brokerPsp[%s]", channelId, brokerId);
    if (channel.getBrokerPspCode() == null
        || !channel.getBrokerPspCode().equals(brokerPsp.getBrokerPspCode())) {
      throw new AppException(
          AppErrorCodeMessageEnum.CHANNEL_BROKER_WRONG_CONFIG, channelId, brokerId);
    }
  }

  public void checkChannelPsp(
      Logger log,
      Channel channel,
      String channelId,
      PaymentServiceProvider paymentServiceProvider,
      String pspId,
      ConfigDataV1 configData) {
    log.debugf("Check conjunction between channel[%s], psp[%s]", channelId, pspId);
    boolean exist =
        configData.getPspChannelPaymentTypes().entrySet().stream()
            .anyMatch(
                a -> {
                  PspChannelPaymentType value = a.getValue();
                  return value.getPspCode().equals(paymentServiceProvider.getPspCode())
                      && value.getChannelCode().equals(channel.getChannelCode());
                });
    if (!exist) {
      throw new AppException(AppErrorCodeMessageEnum.CHANNEL_PSP_WRONG_CONFIG, channelId, pspId);
    }
  }

  public void checkCreditorInstitution(Logger log, String ecId, ConfigDataV1 configData) {
    log.debugf("Check ec[%s]", ecId);
    CreditorInstitution ec =
        Optional.ofNullable(configData.getCreditorInstitutions().get(ecId))
            .orElseThrow(() -> new AppException(AppErrorCodeMessageEnum.EC_UNKNOWN, ecId));
    log.debugf("Check ec[%s] enabled", ecId);
    if (ec.getEnabled() == null || !ec.getEnabled()) {
      throw new AppException(AppErrorCodeMessageEnum.EC_NOT_ENABLED, ecId);
    }
  }

  public void checkReportingFlowFormat(Logger log, String reportingFlowName, String pspId) {
    log.debugf("Check date format in flowName[%s] with psp[%s]", reportingFlowName, pspId);
    String date = reportingFlowName.substring(0, 10);
    try {
      // default, ISO_LOCAL_DATE ("2016-08-16")
      LocalDate.parse(date);
    } catch (RuntimeException e) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_DATE_WRONG_FORMAT, reportingFlowName);
    }
    // TODO non dovremmo verificare che la data sia uguale a regulationDate o reportingFlowDate

    log.debugf("Check psp format in flowName[%s] with psp[%s]", reportingFlowName, pspId);
    String name = reportingFlowName.substring(10);
    boolean nameWrongFromat = !name.startsWith(String.format("%s-", pspId));
    if (nameWrongFromat) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_PSP_WRONG_FORMAT, reportingFlowName);
    }
  }
}
