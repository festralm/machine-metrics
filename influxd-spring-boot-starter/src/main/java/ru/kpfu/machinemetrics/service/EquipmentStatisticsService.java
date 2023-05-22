package ru.kpfu.machinemetrics.service;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.RequiredArgsConstructor;
import ru.kpfu.machinemetrics.model.EquipmentInfluxDbData;
import ru.kpfu.machinemetrics.repository.EquipmentStatisticsRepository;

import java.time.Instant;

@RequiredArgsConstructor
public abstract class EquipmentStatisticsService {

    private final EquipmentStatisticsRepository equipmentStatisticsRepository;

    public final void process(Long equipmentId) {
        var data = getEquipmentInfluxDbData(equipmentId);

        Point point = Point
                .measurement("equipment_statistics")
                .addTag("equipment_id", equipmentId.toString())
                .addField("u", data.getU())
                .addField("enabled", data.getEnabled())
                .time(Instant.now(), WritePrecision.NS);
        equipmentStatisticsRepository.save(point);

    }

    public abstract EquipmentInfluxDbData getEquipmentInfluxDbData(Long equipmentId);
}
