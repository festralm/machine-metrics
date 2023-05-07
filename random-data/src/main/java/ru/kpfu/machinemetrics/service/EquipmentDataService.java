package ru.kpfu.machinemetrics.service;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kpfu.machinemetrics.repository.EquipmentDataRepository;

import java.time.Instant;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EquipmentDataService {
    private static final Random r = new Random();
    private final EquipmentDataRepository equipmentDataRepository;

    public void generateData(Long equipmentId) {
        final double u = r.nextDouble() * 100;

        Point point = Point
                .measurement("equipment_data")
                .addTag("equipment_id", equipmentId.toString())
                .addField("u", u)
                .addField("enabled", u > 20)
                .time(Instant.now(), WritePrecision.NS);
        equipmentDataRepository.save(point);
    }
}
