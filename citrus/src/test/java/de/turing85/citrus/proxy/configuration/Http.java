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
  public static final String EXTERNAL_API_CLIENT_NAME = "http-external-api-proxy-client";
  public static final String EXTERNAL_API_PROXY_SERVER_NAME = "http-external-api-proxy-server";
  public static final String SUT_CLIENT = "http-sut-client";

  @Bean(EXTERNAL_API_PROXY_SERVER_NAME)
  public HttpServer externalApiProxyServer(@Value("${test-config.http.server.port}") int port) {
    // @formatter:off
    return CitrusEndpoints
        .http()
        .server()
        .port(port)
        .timeout(Duration.ofSeconds(10).toMillis())
        .autoStart(true)
        .name(EXTERNAL_API_PROXY_SERVER_NAME)
        .build();
    // @formatter:on
  }

  @Bean(EXTERNAL_API_CLIENT_NAME)
  public HttpClient externalApiClient(
      @Value("${test-config.external-api.url}") String externalApiClientUrl) {
    // @formatter:off
    return CitrusEndpoints
        .http()
        .client()
        .requestUrl(externalApiClientUrl)
        .name(EXTERNAL_API_CLIENT_NAME)
        .build();
    // @formatter:on
  }

  @Bean(SUT_CLIENT)
  public HttpClient sutClient(@Value("${test-config.sut.url}") String sutUrl) {
    // @formatter:off
    return CitrusEndpoints
        .http()
        .client()
        .requestUrl(sutUrl)
        .name(SUT_CLIENT)
        .build();
    // @formatter:on
  }
}
