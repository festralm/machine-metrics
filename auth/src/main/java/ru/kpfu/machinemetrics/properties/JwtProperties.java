package ru.kpfu.machinemetrics.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties("app.jwt")
public class JwtProperties {

    private String secretKey;
    private Long expiration;
    private Long refreshTokenExpiration;
}
