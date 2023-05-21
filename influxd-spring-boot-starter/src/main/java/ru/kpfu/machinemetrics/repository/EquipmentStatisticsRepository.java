package ru.kpfu.machinemetrics.repository;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.write.Point;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.kpfu.machinemetrics.properties.InfluxDbProperties;

@Repository
@RequiredArgsConstructor
public class EquipmentStatisticsRepository {

    private final InfluxDBClient influxDBClient;
    private final InfluxDbProperties influxDbProperties;

    public void save(Point point) {
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        writeApi.writePoint(influxDbProperties.getBucket(), influxDbProperties.getOrg(), point);
    }
}
