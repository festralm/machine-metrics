package ru.kpfu.machinemetrics.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.kpfu.machinemetrics.properties.InfluxDbProperties;

@Configuration
@EnableConfigurationProperties({InfluxDbProperties.class})
@RequiredArgsConstructor
public class InfluxDBConfig {

    private final InfluxDbProperties influxDbProperties;

    @Bean
    public InfluxDBClient influxDBClient() {
        return InfluxDBClientFactory.create(influxDbProperties.getUrl(), influxDbProperties.getToken().toCharArray());
    }
}
