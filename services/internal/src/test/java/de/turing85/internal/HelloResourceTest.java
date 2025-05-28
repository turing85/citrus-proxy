package de.turing85.internal;

import java.util.Objects;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@ConnectWireMock
@TestHTTPEndpoint(HelloResource.class)
class HelloResourceTest {
  @Nullable
  WireMock wireMock;

  @BeforeEach
  void setup() {
    Objects.requireNonNull(wireMock).resetRequests();
    wireMock.resetToDefaultMappings();
    wireMock.resetScenarios();
  }

  @Test
  void testHello() {
    final String expected = "Expected";
    // @formatter:off
    Objects.requireNonNull(wireMock).register(
        get(urlEqualTo("/hello"))
            .willReturn(aResponse()
                .withStatus(Response.Status.OK.getStatusCode())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                .withBody(expected)));
    RestAssured
        .when().get()
        .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(MediaType.TEXT_PLAIN)
            .body(is(expected));
    wireMock.verifyThat(1, getRequestedFor(urlEqualTo("/hello")));
    // @formatter:on
  }
}
