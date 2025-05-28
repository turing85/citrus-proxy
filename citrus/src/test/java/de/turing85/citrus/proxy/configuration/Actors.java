package de.turing85.citrus.proxy.configuration;

import org.citrusframework.TestActor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Actors {
  public static final String CALL_SUT_NAME = "call-sut";
  public static final String EXTERNAL_API_NAME = "external-api";
  public static final String VERIFY_EXTERNAL_API_NAME = "verify-external-api";

  @Bean(name = CALL_SUT_NAME)
  public TestActor callSutActor() {
    return new TestActor(CALL_SUT_NAME);
  }

  @Bean(name = EXTERNAL_API_NAME)
  public TestActor simulateExternalApiActor() {
    return new TestActor(EXTERNAL_API_NAME);
  }

  @Bean(name = VERIFY_EXTERNAL_API_NAME)
  public TestActor verifyExternalApiActor() {
    return new TestActor(VERIFY_EXTERNAL_API_NAME);
  }
}
