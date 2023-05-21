package ru.kpfu.machinemetrics.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.kpfu.machinemetrics.properties.AppProperties;
import ru.kpfu.machinemetrics.properties.InfluxDbProperties;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({AppProperties.class, InfluxDbProperties.class})
public class InfluxdAutoConfiguration {

    private final InfluxDbProperties influxDbProperties;

    private final ConnectionFactory connectionFactory;

    private final AppProperties appProperties;

    @Bean
    @ConditionalOnMissingBean
    public InfluxDBClient influxDBClient() {
        return InfluxDBClientFactory.create(influxDbProperties.getUrl(), influxDbProperties.getToken().toCharArray());
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());

        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }

    @Bean
    public Queue myQueue() {
        return new Queue(String.format("rk-%s", appProperties.getName()), false);
    }

    @Bean
    public Queue myDeleteQueue() {
        return new Queue(String.format("rk-%s-delete", appProperties.getName()), false);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        rabbitTemplate.setMessageConverter(jsonMessageConverter());

        return rabbitTemplate;
    }
}
