package de.turing85.citrus.proxy.behaviour;

import java.util.Collections;
import java.util.UUID;

import lombok.AllArgsConstructor;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestActor;
import org.citrusframework.TestBehavior;
import org.citrusframework.http.actions.HttpClientActionBuilder;
import org.citrusframework.http.actions.HttpClientRequestActionBuilder;
import org.citrusframework.http.actions.HttpServerActionBuilder;
import org.citrusframework.http.actions.HttpServerRequestActionBuilder;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.message.MessageHeaders;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.container.Async.Builder.async;
import static org.citrusframework.container.Sequence.Builder.sequential;
import static org.citrusframework.dsl.MessageSupport.MessageHeaderSupport.fromHeaders;
import static org.citrusframework.http.actions.HttpActionBuilder.http;

@AllArgsConstructor
public class HttpProxyBehaviour implements TestBehavior {
  private static final String EXTERNAL_REQUEST_ID_VARIABLE_NAME_PATTERN = "%s-%s-external-request";

  private final String name;
  private final HttpClient client;
  private final HttpServer proxyServer;
  private final TestActor clientActor;
  private final TestActor proxyServerActor;
  private final HttpMethod httpMethod;
  private final String acceptHeader;
  private final String path;
  private final HttpStatus status;
  @Nullable
  private final String requestBody;
  @Nullable
  private final String responseBody;

  @Override
  public void apply(TestActionRunner runner) {
    final RequestBuilders requestBuilders = constructRequests();
    final String externalRequestIdVariableName =
        EXTERNAL_REQUEST_ID_VARIABLE_NAME_PATTERN.formatted(name, UUID.randomUUID());
    final String externalRequestIdCitrusVariable = "${%s}".formatted(externalRequestIdVariableName);
    // @formatter:off
    runner.run(
        async().actions(
            sequential()
                .actions(
                    requestBuilders
                        .server()
                        .message()
                        .accept(acceptHeader)
                        .body(requestBody),
                    echo("%s: Received %s %s".formatted(name, httpMethod, path)))
                .actor(proxyServerActor),
            sequential()
                .actions(
                    echo("%s: Calling proxied service (%s %s%s) for verification".formatted(
                        name,
                        httpMethod,
                        client.getEndpointConfiguration().getRequestUrl(),
                        path)),
                    requestBuilders
                        .client()
                        .message()
                        .accept(acceptHeader)
                        .body(requestBody)
                        .extract(
                            fromHeaders().header(MessageHeaders.ID, externalRequestIdVariableName)),
                    http()
                        .client(client)
                        .receive()
                        .response()
                        .selector(Collections
                            .singletonMap(MessageHeaders.ID, externalRequestIdCitrusVariable))
                        .message()
                        .status(status)
                        .contentType(acceptHeader)
                        .body(responseBody),
                    echo("%s: Response from proxied service (%s %s%s) verified".formatted(
                        name,
                        httpMethod,
                        client.getEndpointConfiguration().getRequestUrl(),
                        path)))
                .actor(clientActor),
            sequential()
                .actions(
                    echo("%s: Sending response for %s %s".formatted(name, httpMethod, path)),
                    http()
                        .server(proxyServer)
                        .respond()
                        .message()
                        .status(status)
                        .contentType(acceptHeader)
                        .body(responseBody))
                .actor(proxyServerActor)));
    // @formatter:on
  }

  private RequestBuilders constructRequests() {
    final HttpServerActionBuilder.HttpServerReceiveActionBuilder sutReceiveRequest =
        http().server(proxyServer).receive();
    final HttpClientActionBuilder.HttpClientSendActionBuilder externalClientSend =
        http().client(client).send();
    final HttpServerRequestActionBuilder sutRequest;
    final HttpClientRequestActionBuilder externalClientRequest;
    if (httpMethod.equals(HttpMethod.GET)) {
      sutRequest = sutReceiveRequest.get(path);
      externalClientRequest = externalClientSend.get(path);
    } else if (httpMethod.equals(HttpMethod.HEAD)) {
      sutRequest = sutReceiveRequest.head(path);
      externalClientRequest = externalClientSend.head(path);
    } else if (httpMethod.equals(HttpMethod.POST)) {
      sutRequest = sutReceiveRequest.post(path);
      externalClientRequest = externalClientSend.post(path);
    } else if (httpMethod.equals(HttpMethod.PUT)) {
      sutRequest = sutReceiveRequest.put(path);
      externalClientRequest = externalClientSend.put(path);
    } else if (httpMethod.equals(HttpMethod.PATCH)) {
      sutRequest = sutReceiveRequest.patch(path);
      externalClientRequest = externalClientSend.patch(path);
    } else if (httpMethod.equals(HttpMethod.DELETE)) {
      sutRequest = sutReceiveRequest.delete(path);
      externalClientRequest = externalClientSend.delete(path);
    } else if (httpMethod.equals(HttpMethod.OPTIONS)) {
      sutRequest = sutReceiveRequest.options(path);
      externalClientRequest = externalClientSend.options(path);
    } else if (httpMethod.equals(HttpMethod.TRACE)) {
      sutRequest = sutReceiveRequest.trace(path);
      externalClientRequest = externalClientSend.trace(path);
    } else {
      throw new IllegalArgumentException("http method not supported: " + httpMethod);
    }
    return new RequestBuilders(sutRequest, externalClientRequest);
  }

  // @formatter:off
  private record RequestBuilders(HttpServerRequestActionBuilder server,
      HttpClientRequestActionBuilder client) {
  }
  // @formatter:on
}
