package de.turing85.citrus.proxy;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import de.turing85.citrus.proxy.behaviour.HttpProxyBehaviour;
import de.turing85.citrus.proxy.configuration.Actors;
import de.turing85.citrus.proxy.configuration.ConfigurationRoot;
import de.turing85.citrus.proxy.configuration.Http;
import lombok.Getter;
import org.citrusframework.TestActor;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.message.MessageHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import static org.citrusframework.actions.ApplyTestBehaviorAction.Builder.apply;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.container.Sequence.Builder.sequential;
import static org.citrusframework.dsl.MessageSupport.MessageHeaderSupport.fromHeaders;
import static org.citrusframework.http.actions.HttpActionBuilder.http;

@CitrusSpringSupport
@ContextConfiguration(classes = ConfigurationRoot.class)
@Getter
class HelloIT {
  private static final String ACCEPT_HEADER = "accept-header";
  private static final String ACCEPT_HEADER_VAR = "${%s}".formatted(ACCEPT_HEADER);
  private static final String EXPECTED_RESPONSE = "expected-response";
  private static final String EXPECTED_RESPONSE_VAR = "${%s}".formatted(EXPECTED_RESPONSE);
  private static final String INTERNAL_REQUEST = "internal-request";
  private static final String INTERNAL_REQUEST_VAR = "${%s}".formatted(INTERNAL_REQUEST);
  private static final String PATH = "path";
  private static final String PATH_VAR = "${%s}".formatted(PATH);

  private final HttpClient clientExternalApi;
  private final HttpClient clientSut;
  private final TestActor callSutActor;
  private final TestActor externalApiActor;
  private final TestActor verifyExternalApiActor;
  private final String sutUrl;

  HelloIT(@Qualifier(Http.CLIENT_EXTERNAL_API_NAME) HttpClient clientExternalApi,
      @Qualifier(Http.CLIENT_SUT_NAME) HttpClient clientSut,
      @Qualifier(Actors.CALL_SUT_NAME) TestActor callSutActor,
      @Qualifier(Actors.EXTERNAL_API_NAME) TestActor externalApiActor,
      @Qualifier(Actors.VERIFY_EXTERNAL_API_NAME) TestActor verifyExternalApiActor,
      @Value("${test-config.sut.url}") String sutUrl) {
    this.clientExternalApi = clientExternalApi;
    this.clientSut = clientSut;
    this.callSutActor = callSutActor;
    this.externalApiActor = externalApiActor;
    this.verifyExternalApiActor = verifyExternalApiActor;
    this.sutUrl = sutUrl;
  }

  @Test
  @CitrusTest
  void testGetHello(@CitrusResource TestCaseRunner runner) {
    // @formatter:off
    runner.variable(
        ACCEPT_HEADER,
        "%s;charset=%s".formatted(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8));
    runner.variable(EXPECTED_RESPONSE, "hello");
    runner.variable(PATH, "/hello");
    runner.given(
        apply()
            .behavior(new HttpProxyBehaviour(
                "external-proxy",
                clientExternalApi(),
                verifyExternalApiActor(),
                callSutActor(),
                HttpMethod.GET,
                ACCEPT_HEADER_VAR,
                PATH_VAR,
                HttpStatus.OK,
                null,
                EXPECTED_RESPONSE_VAR))
            .actor(externalApiActor()));

    runner.when(
        sequential()
            .actions(
                echo("Calling SUT (GET %s%s)".formatted(sutUrl, PATH_VAR)),
                http()
                    .client(clientSut())
                    .send()
                    .get(PATH_VAR)
                    .message()
                    .contentType(ACCEPT_HEADER_VAR)
                    .extract(fromHeaders().header(MessageHeaders.ID, INTERNAL_REQUEST)))
            .actor(callSutActor()));

    runner.then(
        sequential()
            .actions(
                http()
                    .client(clientSut())
                    .receive()
                    .response()
                    .message()
                    .selector(Collections.singletonMap(MessageHeaders.ID, INTERNAL_REQUEST_VAR))
                    .status(HttpStatus.OK)
                    .contentType(ACCEPT_HEADER_VAR)
                    .body(EXPECTED_RESPONSE_VAR),
                echo("Response from SUT (GET %s%s) verified".formatted(sutUrl, PATH_VAR)))
            .actor(callSutActor()));
    // @formatter:on
  }
}
