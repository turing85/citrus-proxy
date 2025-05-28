package de.turing85.internal;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "external-api")
@Path("hello")
@Produces(MediaType.TEXT_PLAIN)
public interface ExternalApi {
  @GET
  String hello();
}
