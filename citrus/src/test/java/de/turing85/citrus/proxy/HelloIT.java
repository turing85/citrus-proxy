package de.turing85.citrus.proxy;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import de.turing85.citrus.proxy.behaviour.HttpProxyGetBehaviour;
import de.turing85.citrus.proxy.configuration.Actor;
import de.turing85.citrus.proxy.configuration.ConfigurationRoot;
import de.turing85.citrus.proxy.configuration.Http;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import static org.citrusframework.actions.ApplyTestBehaviorAction.Builder.apply;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.container.Sequence.Builder.sequential;
import static org.citrusframework.dsl.MessageSupport.MessageHeaderSupport.fromHeaders;
import static org.citrusframework.http.actions.HttpActionBuilder.http;

@ContextConfiguration(classes = ConfigurationRoot.class)
public class HelloIT extends TestNGCitrusSpringSupport {
  private static final String ACCEPT_HEADER = "accept-header";
  private static final String ACCEPT_HEADER_VAR = "${%s}".formatted(ACCEPT_HEADER);
  private static final String EXPECTED_RESPONSE = "expected-response";
  private static final String EXPECTED_RESPONSE_VAR = "${%s}".formatted(EXPECTED_RESPONSE);
  private static final String INTERNAL_REQUEST = "internal-request";
  private static final String INTERNAL_REQUEST_VAR = "${%s}".formatted(INTERNAL_REQUEST);
  private static final String PATH = "path";
  private static final String PATH_VAR = "${%s}".formatted(PATH);

  @Autowired
  @Qualifier(Http.EXTERNAL_API_CLIENT_NAME)
  HttpClient externalApiClient;

  @Autowired
  @Qualifier(Http.EXTERNAL_API_PROXY_SERVER_NAME)
  HttpServer externalApiProxyServer;

  @Autowired
  @Qualifier(Http.SUT_CLIENT)
  HttpClient sutClient;

  @Autowired
  @Qualifier(Actor.EXTERNAL_API_CLIENT_NAME)
  TestActor externalApiClientActor;

  @Autowired
  @Qualifier(Actor.EXTERNAL_API_PROXY_NAME)
  TestActor externalApiProxyActor;

  @Autowired
  @Qualifier(Actor.SUT_NAME)
  TestActor sutActor;

  @Test
  @CitrusTest
  public void testHello(@Optional @CitrusResource GherkinTestActionRunner runner) {
    // @formatter:off
    variable(
        ACCEPT_HEADER,
        "%s;charset=%s".formatted(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8));
    variable(EXPECTED_RESPONSE, "hello");
    variable(PATH, "/hello");
    runner.given(
        apply(HttpProxyGetBehaviour.builder()
            .client(externalApiClient)
            .proxyServer(externalApiProxyServer)
            .clientActor(externalApiClientActor)
            .proxyServerActor(sutActor)
            .acceptHeader(ACCEPT_HEADER_VAR)
            .expectedResponse(EXPECTED_RESPONSE_VAR)
            .path(PATH_VAR)
            .build())
        .actor(externalApiProxyActor));

    runner.when(
        sequential().actor(sutActor).actions(
            echo().message("Triggering SUT"),
            http()
                .client(sutClient)
                .send()
                .get(EXPECTED_RESPONSE_VAR)
                .message()
                .contentType(ACCEPT_HEADER_VAR)
                .extract(fromHeaders().header(MessageHeaders.ID, INTERNAL_REQUEST))));

    runner.then(
        sequential().actor(sutActor).actions(
            http()
                .client(sutClient)
                .receive()
                .response()
                .message()
                .selector(Collections.singletonMap(MessageHeaders.ID, INTERNAL_REQUEST_VAR))
                .status(HttpStatus.OK)
                .contentType(ACCEPT_HEADER_VAR)
                .body(EXPECTED_RESPONSE_VAR),
            echo().message("Response from SUT verified")));
    // @formatter:on
  }
}
