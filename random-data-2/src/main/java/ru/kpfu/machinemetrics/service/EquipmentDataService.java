package ru.kpfu.machinemetrics.service;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.kpfu.machinemetrics.model.EquipmentData;
import ru.kpfu.machinemetrics.repository.EquipmentDataRepository;

import java.time.Instant;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EquipmentDataService {
    private final EquipmentDataRepository equipmentDataRepository;

    private static final Random r = new Random();

    public void generateData(Long equipmentId) {
        final double u = r.nextDouble() * 100;
        EquipmentData equipmentData = EquipmentData.builder().equipmentId(equipmentId).u(u).enabled(u > 20).build();

        Point point = Point.measurement("equipment_data").addTag("equipment_id",
                equipmentData.getEquipmentId().toString()).addField("u", equipmentData.getU()).addField("enabled",
                equipmentData.getEnabled()).time(Instant.now(), WritePrecision.NS);
        equipmentDataRepository.save(point);
    }
}
