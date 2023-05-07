package ru.kpfu.machinemetrics.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    @Bean
    public Queue myQueue() {
        return new Queue("rk-equipment-statistics", true);
    }
}
