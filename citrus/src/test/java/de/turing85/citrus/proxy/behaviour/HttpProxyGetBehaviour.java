package de.turing85.citrus.proxy.behaviour;

import java.util.Collections;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestActor;
import org.citrusframework.TestBehavior;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.message.MessageHeaders;
import org.springframework.http.HttpStatus;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.container.Async.Builder.async;
import static org.citrusframework.container.Sequence.Builder.sequential;
import static org.citrusframework.dsl.MessageSupport.MessageHeaderSupport.fromHeaders;
import static org.citrusframework.http.actions.HttpActionBuilder.http;

@Builder
@AllArgsConstructor
public class HttpProxyGetBehaviour implements TestBehavior {
  private static final String EXTERNAL_REQUEST = "external-request";
  private static final String EXTERNAL_REQUEST_VAR = "${%s}".formatted(EXTERNAL_REQUEST);

  private final HttpClient client;
  private final HttpServer proxyServer;

  private final TestActor clientActor;
  private final TestActor proxyServerActor;

  private final String acceptHeader;
  private final String expectedResponse;
  private final String path;

  @Override
  public void apply(TestActionRunner runner) {
    // @formatter:off
    runner.run(
        async().actions(
            sequential().actor(proxyServerActor).actions(
                http()
                    .server(proxyServer)
                    .receive()
                    .get(path)
                    .message()
                    .accept(acceptHeader),
                echo().message("Received message on %s".formatted(proxyServer.getName()))),
            sequential().actor(clientActor).actions(
                echo().message("Calling %s for verification".formatted(client.getName())),
                http()
                    .client(client)
                    .send()
                    .get(path)
                    .message()
                    .accept(acceptHeader)
                    .extract(fromHeaders().header(MessageHeaders.ID, EXTERNAL_REQUEST)),
                http()
                    .client(client)
                    .receive()
                    .response()
                    .selector(Collections.singletonMap(MessageHeaders.ID, EXTERNAL_REQUEST_VAR))
                    .message()
                    .status(HttpStatus.OK)
                    .contentType(acceptHeader)
                    .body(expectedResponse),
                echo().message("Response from %s verified".formatted(client.getName()))),
            sequential().actor(proxyServerActor).actions(
                echo().message("Sending response from %s".formatted(proxyServer.getName())),
                http()
                    .server(proxyServer)
                    .respond()
                    .message()
                    .status(HttpStatus.OK)
                    .contentType(acceptHeader)
                    .body(expectedResponse))));
    // @formatter:on
  }
}
