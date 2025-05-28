package de.turing85.external;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestHTTPEndpoint(HelloResource.class)
class HelloResourceTest {
  @Test
  void testHello() {
    // @formatter:off
    RestAssured
        .when().get()
        .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(MediaType.TEXT_PLAIN)
            .body(is("hello"));
    // @formatter:on
  }
}
