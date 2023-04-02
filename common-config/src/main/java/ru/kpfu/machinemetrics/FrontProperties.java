package ru.kpfu.machinemetrics;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("app.front")
public class FrontProperties {
    private String url;
}
