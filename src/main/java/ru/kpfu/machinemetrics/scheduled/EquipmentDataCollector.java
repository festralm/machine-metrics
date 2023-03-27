package ru.kpfu.machinemetrics.scheduled;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.kpfu.machinemetrics.dto.detector.EquipmentMeasurements;
import ru.kpfu.machinemetrics.service.EquipmentService;

@Component
@RequiredArgsConstructor
public class EquipmentDataCollector {
    private final EquipmentService equipmentService;
    private final RabbitTemplate rabbitTemplate;
    private final RestTemplate restTemplate;


    @Scheduled(fixedDelay = 1000)
    public void collectEquipmentData() {
        ResponseEntity<EquipmentMeasurements> response = restTemplate.getForEntity("http://example.com/equipment/measurements", EquipmentMeasurements.class);
        EquipmentMeasurements measurements = response.getBody();

        rabbitTemplate.convertAndSend("equipment-measurements", measurements);
    }
}

