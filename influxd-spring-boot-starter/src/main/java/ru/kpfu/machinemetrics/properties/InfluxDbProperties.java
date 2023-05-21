package ru.kpfu.machinemetrics.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("influxdb")
public class InfluxDbProperties {

    private String url;

    private String bucket;

    private String token;

    private String org;
}
