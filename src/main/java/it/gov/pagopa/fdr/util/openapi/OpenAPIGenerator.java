package it.gov.pagopa.fdr.util.openapi;

import io.quarkus.smallrye.openapi.OpenApiFilter;
import it.gov.pagopa.fdr.util.common.StringUtil;
import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;
import org.jboss.jandex.IndexView;

@OpenApiFilter(OpenApiFilter.RunStage.BUILD)
public class OpenAPIGenerator implements OASFilter {

  private static final String TABLE_SEPARATOR = " | ";

  @ConfigProperty(name = "app.version", defaultValue = "0.0.0")
  String version;

  private IndexView view;

  public OpenAPIGenerator(IndexView view) {
    this.view = view;
  }

  @Override
  public void filterOpenAPI(OpenAPI openAPI) {

    openAPI.setInfo(
        OASFactory.createInfo()
            .title("FDR - Flussi di Rendicontazione")
            .description(getMainDescription())
            .version(version)
            .termsOfService("https://www.pagopa.gov.it/"));
    openAPI.setServers(
        List.of(
            OASFactory.createServer().url("${host}").description("Environment host"),
            OASFactory.createServer().url("http://localhost:8080/").description("Localhost")));

    updateAPIsWithTableMetadata(openAPI.getPaths());
  }

  private static String getMainDescription() {

    StringBuilder builder = new StringBuilder();
    builder.append("Manage FDR (aka \"Flussi di Rendicontazione\") exchanged between PSP and EC");
    builder.append("\n\n## OPERATIONAL ERROR CODES\n");
    builder.append("\n<details><summary>Details</summary>\n");
    builder.append(generateOperationalErrorCodeSection());
    return builder.toString();
  }

  private static String generateOperationalErrorCodeSection() {

    StringBuilder builder = new StringBuilder();
    builder
        .append("NAME")
        .append(TABLE_SEPARATOR)
        .append("CODE")
        .append(TABLE_SEPARATOR)
        .append("ERROR MESSAGE TEMPLATE")
        .append(TABLE_SEPARATOR)
        .append("DESCRIPTION")
        .append("\n");
    builder
        .append("-")
        .append(TABLE_SEPARATOR)
        .append("-")
        .append(TABLE_SEPARATOR)
        .append("-")
        .append(TABLE_SEPARATOR)
        .append("-")
        .append("\n");

    for (AppErrorCodeMessageEnum appError : AppErrorCodeMessageEnum.values()) {

      builder
          .append("**")
          .append(appError.errorCode())
          .append("**")
          .append(TABLE_SEPARATOR)
          .append("*")
          .append(StringUtil.insertCharacterAfter(appError.name(), "<br>", 30, '_'))
          .append("*")
          .append(TABLE_SEPARATOR)
          .append(
              StringUtil.insertCharacterAfter(
                  appError.message('%', '%', '%', '%'), "<br>", 50, ' '))
          .append(TABLE_SEPARATOR)
          .append(StringUtil.insertCharacterAfter(appError.openAPIDescription(), "<br>", 50, ' '))
          .append("\n");
    }

    return builder.toString();
  }

  private static void updateAPIsWithTableMetadata(Paths paths) {

    for (PathItem path : paths.getPathItems().values()) {
      List<Operation> operationPerSamePath = path.getOperations().values().stream().toList();
      for (Operation operation : operationPerSamePath) {
        operation.setDescription("## Description:\n" + operation.getDescription());
        extractTableMetadata(operation);
      }
    }
  }

  private static void extractTableMetadata(Operation operation) {
    try {
      String[] operationIdMethodReference = operation.getOperationId().split("\\.");
      if (operationIdMethodReference.length == 2) {

        Class<?> controllerClass =
            Class.forName(
                "it.gov.pagopa.fdr.controller.interfaces.controller."
                    + operationIdMethodReference[0]);
        Optional<Method> method =
            Arrays.stream(controllerClass.getMethods())
                .filter(m -> m.getName().equals(operationIdMethodReference[1]))
                .findFirst();

        if (method.isPresent()) {
          // write app error that can be returned as response
          APIAppErrorMetadata appErrorMetadata =
              method.get().getAnnotation(APIAppErrorMetadata.class);
          if (appErrorMetadata != null) {
            operation.setDescription(
                operation.getDescription() + "\n\n" + buildErrorData(appErrorMetadata));
          }
          // write table related to API characteristics
          APITableMetadata tableMetadata = method.get().getAnnotation(APITableMetadata.class);
          if (tableMetadata != null) {
            operation.setDescription(
                operation.getDescription() + "\n\n" + buildTableData(tableMetadata));
          }
        }
      }
    } catch (ClassNotFoundException e) {
      // skip extraction
    }
  }

  private static String buildErrorData(APIAppErrorMetadata annotation) {
    StringBuilder builder = new StringBuilder();
    builder.append("## Error codes:").append("\n");

    builder.append("APPLICATIVE CODE").append(TABLE_SEPARATOR);
    builder.append("HTTP CODE").append(TABLE_SEPARATOR).append("MESSAGE\n");
    builder.append("-").append(TABLE_SEPARATOR).append("-").append(TABLE_SEPARATOR).append("-\n");

    for (AppErrorCodeMessageEnum error : annotation.errors()) {
      builder.append("**").append(error.errorCode()).append("**").append(TABLE_SEPARATOR);
      builder.append(error.httpStatus().getStatusCode()).append(TABLE_SEPARATOR);
      builder.append(error.message()).append("\n");
    }
    return builder.toString();
  }

  private static String buildTableData(APITableMetadata annotation) {
    return "## API properties:\n"
        + "PROPERTY"
        + TABLE_SEPARATOR
        + "VALUE\n"
        + "-"
        + TABLE_SEPARATOR
        + "-\n"
        + "***Internal***"
        + TABLE_SEPARATOR
        + parseBoolToYN(annotation.internal())
        + "\n"
        + "***External***"
        + TABLE_SEPARATOR
        + parseBoolToYN(annotation.external())
        + "\n"
        + "***Synchronous***"
        + TABLE_SEPARATOR
        + annotation.synchronism().value
        + "\n"
        + "***Authorization***"
        + TABLE_SEPARATOR
        + annotation.authorization().value
        + "\n"
        + "***Authentication***"
        + TABLE_SEPARATOR
        + annotation.authentication().value
        + "\n"
        + "***TPS***"
        + TABLE_SEPARATOR
        + annotation.tps()
        + "/sec"
        + "\n"
        + "***Idempotency***"
        + TABLE_SEPARATOR
        + parseBoolToYN(annotation.idempotency())
        + "\n"
        + "***Stateless***"
        + TABLE_SEPARATOR
        + parseBoolToYN(annotation.stateless())
        + "\n"
        + "***Read/Write Intensive***"
        + TABLE_SEPARATOR
        + parseReadWrite(annotation.readWriteIntense())
        + "\n"
        + "***Cacheable***"
        + TABLE_SEPARATOR
        + parseBoolToYN(annotation.cacheable())
        + "\n";
  }

  private static String parseReadWrite(APITableMetadata.ReadWrite readWrite) {
    return readWrite.getValue();
  }

  private static String parseBoolToYN(boolean value) {
    return value ? "Y" : "N";
  }
}
