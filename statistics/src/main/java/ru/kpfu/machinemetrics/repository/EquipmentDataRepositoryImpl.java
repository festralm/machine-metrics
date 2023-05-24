package ru.kpfu.machinemetrics.repository;

import com.influxdb.Cancellable;
import com.influxdb.client.DeleteApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.DeletePredicateRequest;
import com.influxdb.query.FluxRecord;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.kpfu.machinemetrics.model.EquipmentData;
import ru.kpfu.machinemetrics.properties.AppProperties;
import ru.kpfu.machinemetrics.properties.InfluxDbProperties;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

;

@Repository
@RequiredArgsConstructor
public class EquipmentDataRepositoryImpl implements EquipmentDataRepository {

    private final InfluxDBClient influxDBClient;
    private final InfluxDbProperties influxDbProperties;
    private final AppProperties appProperties;

    private static EquipmentData mapFluxRecord(ZoneOffset zoneOffset, FluxRecord fluxRecord) {
        return EquipmentData.builder()
                .equipmentId(Long.parseLong((String) Objects.requireNonNull(fluxRecord.getValueByKey("equipment_id"))))
                .u((Double) fluxRecord.getValueByKey("u"))
                .enabled((Boolean) fluxRecord.getValueByKey("enabled"))
                .time(fluxRecord.getTime().atOffset(zoneOffset))
                .build();
    }

    @Override
    public List<EquipmentData> getData(@NotNull String start, @NotNull String stop, Long equipmentId) {

        QueryApi queryApi = influxDBClient.getQueryApi();

        List<EquipmentData> result = new ArrayList<>();

        CountDownLatch latch = new CountDownLatch(2);

        Runnable onComplete = () -> {
            // Handle completion of the query
            System.out.println("Query completed successfully");
            latch.countDown();
        };

        BiConsumer<Cancellable, FluxRecord> onNext = (cancellable, fluxRecord) -> {
            result.add(mapFluxRecord(ZoneOffset.of(appProperties.getDefaultZone()), fluxRecord));
        };

        Consumer<Throwable> onError = throwable -> {
            throw new RuntimeException();
        };

        addLastPreviousRecord(start, equipmentId, queryApi, onNext, onError, onComplete);

        addRecordsInPeriod(start, stop, equipmentId, queryApi, onNext, onError, onComplete);

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private void addRecordsInPeriod(
            String start,
            String stop,
            Long equipmentId,
            QueryApi queryApi,
            BiConsumer<Cancellable, FluxRecord> onNext,
            Consumer<Throwable> onError, Runnable onComplete
    ) {
        String query = String.format(
                "from(bucket: \"%s\") " +
                        "|> range(start: time(v: %s), stop: time(v: %s)) " +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"equipment_statistics\")" +
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

        queryApi.query(
                query,
                influxDbProperties.getOrg(),
                onNext,
                onError,
                onComplete
        );
    }

    private void addLastPreviousRecord(
            @NotNull String start,
            Long equipmentId,
            QueryApi queryApi,
            BiConsumer<Cancellable, FluxRecord> onNext,
            Consumer<Throwable> onError, Runnable onComplete
    ) {
        String query = String.format(
                "from(bucket: \"%s\") " +
                        "|> range(start: -inf, stop: time(v: %s)) " +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"equipment_statistics\")" +
                        (
                                equipmentId == null ?
                                        "" :
                                        "|> filter(fn: (r) => r[\"equipment_id\"] == \"%s\")"
                        ) +
                        "|> pivot(rowKey:[\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")" +
                        "|> last(column: \"_time\")",
                influxDbProperties.getBucket(),
                start,
                equipmentId
        );

        queryApi.query(
                query,
                influxDbProperties.getOrg(),
                onNext,
                onError,
                onComplete
        );
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
