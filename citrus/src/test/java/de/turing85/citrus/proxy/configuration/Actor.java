package de.turing85.citrus.proxy.configuration;

import org.citrusframework.TestActor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Actor {
  public static final String EXTERNAL_API_CLIENT_NAME = "external-api-client";
  public static final String EXTERNAL_API_PROXY_NAME = "external-api-proxy";
  public static final String SUT_NAME = "sut";

  @Bean(EXTERNAL_API_CLIENT_NAME)
  public TestActor externalApiProxyClient() {
    return new TestActor(EXTERNAL_API_CLIENT_NAME);
  }

  @Bean(EXTERNAL_API_PROXY_NAME)
  public TestActor externalApiProxy() {
    return new TestActor(EXTERNAL_API_PROXY_NAME);
  }

  @Bean(SUT_NAME)
  public TestActor sut() {
    return new TestActor(SUT_NAME);
  }
}
