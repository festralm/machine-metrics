package ru.kpfu.randomdata.repository;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.kpfu.randomdata.model.EquipmentData;

import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class EquipmentDataRepositoryImpl implements EquipmentDataRepository {

    @Value("${influxdb.bucket}")
    private String bucket;

    @Value("${influxdb.org}")
    private String org;

    private final InfluxDBClient influxDBClient;

    @Override
    public void save(EquipmentData equipmentData) {
        Point point = Point.measurement("equipment_data")
                .addTag("equipment_id", equipmentData.getEquipmentId().toString())
                .addField("u", equipmentData.getU())
                .addField("enabled", equipmentData.getEnabled())
                .time(Instant.now(), WritePrecision.NS);

        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        writeApi.writePoint(bucket, org, point);
    }
}
