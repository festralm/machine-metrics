package ru.kpfu.machinemetrics.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.kpfu.machinemetrics.properties.AppProperties;

@Configuration
@EnableConfigurationProperties({AppProperties.class})
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final AppProperties appProperties;

    @Bean
    public Queue myQueue() {
        return new Queue(String.format("rk-%s", appProperties.getName()), false);
    }
}
