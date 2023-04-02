package ru.kpfu.machinemetrics.litener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.stereotype.Component;
import ru.kpfu.machinemetrics.dto.detector.EquipmentMeasurements;
import ru.kpfu.machinemetrics.model.EquipmentData;
import ru.kpfu.machinemetrics.service.EquipmentDataService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class EquipmentDataListener implements MessageListener {
    private final EquipmentDataService equipmentDataService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message) {
        try {
            EquipmentMeasurements equipmentMeasurements = objectMapper.readValue(new String(message.getBody()), EquipmentMeasurements.class);
            EquipmentData equipmentData = null; // todo map equipmentMeasurements
            equipmentDataService.save(equipmentData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

