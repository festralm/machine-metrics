package ru.kpfu.machinemetrics.task;

import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@AllArgsConstructor
public class FetchDataServiceTask implements Runnable {

    private String serviceName;
    private Long equipmentId;
    private RabbitTemplate rabbitTemplate;

    @Override
    public void run() {
        rabbitTemplate.convertAndSend(String.format("rk-%s", serviceName), equipmentId);
    }
}