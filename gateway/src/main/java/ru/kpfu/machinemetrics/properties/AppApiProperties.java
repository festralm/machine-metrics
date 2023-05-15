package ru.kpfu.machinemetrics.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("app.api.prefix")
public class AppApiProperties {
    private String v1;
}
