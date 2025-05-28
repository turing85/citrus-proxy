package de.turing85.external;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.logging.Log;

@Path("hello")
@Produces(MediaType.TEXT_PLAIN)
public class HelloResource {
  @GET
  public String hello() {
    Log.info("called");
    return "hello";
  }
}
