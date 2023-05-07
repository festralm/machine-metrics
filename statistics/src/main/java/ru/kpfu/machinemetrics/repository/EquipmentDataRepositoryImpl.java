package ru.kpfu.machinemetrics.repository;

import com.influxdb.client.DeleteApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.DeletePredicateRequest;
import com.influxdb.query.FluxTable;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.kpfu.machinemetrics.model.EquipmentData;
import ru.kpfu.machinemetrics.properties.InfluxDbProperties;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

;

@Repository
@RequiredArgsConstructor
public class EquipmentDataRepositoryImpl implements EquipmentDataRepository {

    private final InfluxDBClient influxDBClient;
    private final InfluxDbProperties influxDbProperties;

    @org.jetbrains.annotations.NotNull
    private static List<EquipmentData> mapResult(List<FluxTable> result) {
        return result.stream()
                .flatMap(fluxTable -> fluxTable.getRecords().stream()
                        .map(
                                fluxRecord -> EquipmentData.builder()
                                        .equipmentId(Long.parseLong((String) Objects.requireNonNull(fluxRecord.getValueByKey("equipment_id"))))
                                        .u((Double) fluxRecord.getValueByKey("u"))
                                        .enabled((Boolean) fluxRecord.getValueByKey("enabled"))
                                        .time(fluxRecord.getTime())
                                        .build()
                        )
                )
                .sorted(Comparator.comparing(EquipmentData::getTime))
                .collect(Collectors.toList());
    }

    @Override
    public List<EquipmentData> getData(@NotNull String start, @NotNull String stop, Long equipmentId) {

        QueryApi queryApi = influxDBClient.getQueryApi();


        String query = String.format(
                "from(bucket: \"%s\") " +
                        "|> range(start: time(v: %s), stop: time(v: %s)) " +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"equipment_data\")" +
                        (
                                equipmentId == null ?
                                        "" :
                                        "|> filter(fn: (r) => r[\"equipment_id\"] == \"%s\")"
                        ) +
                        "|> pivot(rowKey:[\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")",
                influxDbProperties.getBucket(),
                start,
                stop,
                equipmentId
        );

        // todo use another method
        List<FluxTable> result = queryApi.query(query, influxDbProperties.getOrg());
        return mapResult(result);
    }

    @Override
    public void delete(Long equipmentId) {
        DeleteApi deleteApi = influxDBClient.getDeleteApi();

        String predicate = String.format("equipment_id=\"%s\"", equipmentId);
        deleteApi.delete(
                new DeletePredicateRequest()
                        .predicate(predicate)
                        .start(
                                OffsetDateTime.of(
                                        LocalDate.of(1970, 1, 1),
                                        LocalTime.MIN,
                                        ZoneOffset.UTC
                                )
                        )
                        .stop(OffsetDateTime.now().plusDays(1))
                ,
                influxDbProperties.getBucket(),
                influxDbProperties.getOrg()
        );
    }
}
