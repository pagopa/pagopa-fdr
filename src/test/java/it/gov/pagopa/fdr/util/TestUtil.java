package it.gov.pagopa.fdr.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;

@ApplicationScoped
public class TestUtil {

  @Inject ObjectMapper mapper;

  @SneakyThrows
  public <T> String prettyPrint(String json, Class<T> clazz) {
    T obj = mapper.readValue(json, clazz);
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
  }

  @SneakyThrows
  public <T> String prettyPrint(T obj) {
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
  }
}
