// package it.gov.pagopa.fdr;
//
// import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
// import org.eclipse.microprofile.openapi.annotations.info.Contact;
// import org.eclipse.microprofile.openapi.annotations.info.Info;
// import org.eclipse.microprofile.openapi.annotations.info.License;
// import org.eclipse.microprofile.openapi.annotations.servers.Server;
// import org.eclipse.microprofile.openapi.annotations.tags.Tag;
// import javax.ws.rs.core.Application;
//
// @OpenAPIDefinition(
//    tags = {
//      @Tag(name = "info", description = "Info operations."),
//      @Tag(name = "fruit", description = "Operations related to gaskets")
//    },
//    info =
//        @Info(
//            title = "FDR - Flussi di Rendicontazione",
//            description = "Manage FDR ( aka \"Flussi di Rendicontazione\" ) exchanged between PSP
// and EC",
//            version = "0.0.0-SNAPSHOT",
//            termsOfService = "https://www.pagopa.gov.it/"),
//    servers = @Server(
//        description = "APIM",
//            url= "${host}/apiconfig/api/v1"
//    )
// )
// public class App extends Application {}
