package ru.kpfu.machinemetrics.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange("rk-equipment");
    }

    @Bean
    public Queue scheduleQueue() {
        return new Queue("rk-equipment-schedule", true);
    }

    @Bean
    public Queue statisticsQueue() {
        return new Queue("rk-equipment-statistics", true);
    }

    @Bean
    public Binding scheduleBinding() {
        return BindingBuilder.bind(scheduleQueue()).to(fanoutExchange());
    }

    @Bean
    public Binding statisticsBinding() {
        return BindingBuilder.bind(statisticsQueue()).to(fanoutExchange());
    }
}
