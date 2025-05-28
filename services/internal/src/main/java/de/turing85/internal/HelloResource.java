package de.turing85.internal;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.logging.Log;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("hello")
public class HelloResource {
  private final ExternalApi externalApi;

  public HelloResource(@RestClient ExternalApi externalApi) {
    this.externalApi = externalApi;
  }

  @GET
  public String hello() {
    Log.info("calling external api");
    return externalApi.hello();
  }
}
