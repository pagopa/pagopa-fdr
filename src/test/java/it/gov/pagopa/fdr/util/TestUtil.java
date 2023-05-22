package it.gov.pagopa.fdr.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TestUtil {

  @Inject ObjectMapper mapper;

  public <T> String prettyPrint(String json, Class<T> clazz) throws JsonProcessingException {
    T obj = mapper.readValue(json, clazz);
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
  }

  public <T> String prettyPrint(T obj) throws JsonProcessingException {
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
  }
}
