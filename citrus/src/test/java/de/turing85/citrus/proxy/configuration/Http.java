package de.turing85.citrus.proxy.configuration;

import java.time.Duration;

import org.citrusframework.dsl.endpoint.CitrusEndpoints;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.server.HttpServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Http {
  public static final String CLIENT_EXTERNAL_API_NAME = "client-external-api";
  public static final String CLIENT_SUT_NAME = "client-sut";
  public static final String SERVER_EXTERNAL_API_PROXY_NAME = "server-external-api-proxy";

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
  public HttpServer serverExternalApiProxy(@Value("${test-config.http.server.port}") int port) {
    // @formatter:off
    return CitrusEndpoints
        .http()
        .server()
        .port(port)
        .timeout(Duration.ofSeconds(10).toMillis())
        .autoStart(true)
        .name(SERVER_EXTERNAL_API_PROXY_NAME)
        .build();
    // @formatter:on
  }
}
