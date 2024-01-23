package it.gov.pagopa.fdr.service.history;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.fdr.service.history.mapper.HistoryServiceMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class StorageFdrService {
  @Inject HistoryServiceMapper mapper;
  @Inject Logger logger;
  @Inject ObjectMapper objMapper;
}
