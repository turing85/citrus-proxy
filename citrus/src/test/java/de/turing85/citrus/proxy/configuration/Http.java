package de.turing85.citrus.proxy.configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.citrusframework.channel.ChannelEndpoint;
import org.citrusframework.channel.ChannelEndpointBuilder;
import org.citrusframework.channel.MessageSelectingQueueChannel;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.endpoint.CitrusEndpoints;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.adapter.RequestDispatchingEndpointAdapter;
import org.citrusframework.endpoint.adapter.StaticEndpointAdapter;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.message.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Http {
  public static final String CLIENT_EXTERNAL_API_NAME = "client-external-api";
  public static final String CLIENT_SUT_NAME = "client-sut";
  public static final String SERVER_EXTERNAL_API_PROXY_NAME = "server-external-api-proxy";

  private static final Map<String, Endpoint> ENDPOINTS = new HashMap<>();

  @Bean(name = CLIENT_EXTERNAL_API_NAME)
  public HttpClient clientExternalApi(
      @Value("${test-config.external-api.url}") String externalApiClientUrl) {
    // @formatter:off
    return CitrusEndpoints
        .http()
        .client()
        .requestUrl(externalApiClientUrl)
        .name(CLIENT_EXTERNAL_API_NAME)
        .build();
    // @formatter:on
  }

  @Bean(name = CLIENT_SUT_NAME)
  public HttpClient clientSut(@Value("${test-config.sut.url}") String sutUrl) {
    // @formatter:off
    return CitrusEndpoints
        .http()
        .client()
        .requestUrl(sutUrl)
        .name(CLIENT_SUT_NAME)
        .build();
    // @formatter:on
  }

  @Bean(name = SERVER_EXTERNAL_API_PROXY_NAME)
  public HttpServer serverExternalApiProxy(@Value("${test-config.http.server.port}") int port,
      TestContext context) {
    // @formatter:off
    return CitrusEndpoints
        .http()
        .server()
        .port(port)
        .endpointAdapter(constructAdapter(context))
        .timeout(Duration.ofSeconds(10).toMillis())
        .autoStart(true)
        .name(SERVER_EXTERNAL_API_PROXY_NAME)
        .build();
    // @formatter:on
  }

  private static RequestDispatchingEndpointAdapter constructAdapter(TestContext context) {
    RequestDispatchingEndpointAdapter adapter = new RequestDispatchingEndpointAdapter();
    adapter.setMappingKeyExtractor(
        message -> message.getHeader(HttpMessageHeaders.HTTP_REQUEST_URI).toString());
    adapter.setMappingStrategy(key -> new StaticEndpointAdapter() {
      @Override
      protected Message handleMessageInternal(Message message) {
        context.setVariable("citrus_message_correlator_direct-%s:consumer".formatted(key),
            UUID.randomUUID().toString());
        Http.getEndpoint(key).createProducer().send(message, context);
        return Http.getEndpoint(key).createConsumer().receive(context);
      }
    });
    return adapter;
  }

  public static Endpoint getEndpoint(String endpointName) {
    return ENDPOINTS.computeIfAbsent(endpointName, Http::constructChannel);
  }

  private static ChannelEndpoint constructChannel(String name) {
    MessageSelectingQueueChannel channel = new MessageSelectingQueueChannel();
    channel.setBeanName("channel-%s".formatted(name));
    // @formatter:off
    return new ChannelEndpointBuilder()
        .channel(channel)
        .filterInternalHeaders(false)
        .name("direct-%s".formatted(name))
        .build();
    // @formatter:on
  }
}
