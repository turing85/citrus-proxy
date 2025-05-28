package de.turing85.citrus.proxy.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({Actor.class, Http.class})
public class ConfigurationRoot {
}
