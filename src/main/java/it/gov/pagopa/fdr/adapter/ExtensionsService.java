package it.gov.pagopa.fdr.adapter;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import java.util.Set;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/extensions")
@RegisterRestClient
public interface ExtensionsService {

  @GET
  Set<Extension> getById(@QueryParam("id") String id);
}
