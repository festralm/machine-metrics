package ru.kpfu.machinemetrics.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("keycloak")
public class KeycloakProperties {

    private String realm;
    private String resource;
    private String authServerUrl;
    private String clientSecret;
}
