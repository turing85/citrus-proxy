package de.turing85.citrus.proxy.configuration;

import org.citrusframework.config.CitrusSpringConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CitrusSpringConfig.class, Actors.class, Http.class})
public class ConfigurationRoot {
}
