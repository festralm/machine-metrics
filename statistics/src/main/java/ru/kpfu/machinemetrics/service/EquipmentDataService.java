package ru.kpfu.machinemetrics.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kpfu.machinemetrics.dto.StatisticsDto;
import ru.kpfu.machinemetrics.model.EquipmentData;
import ru.kpfu.machinemetrics.repository.EquipmentDataRepository;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentDataService {

    private final EquipmentDataRepository equipmentDataRepository;

    public StatisticsDto getData(Long equipmentId, Instant start, Instant end) {
        if (start == null) {
            start = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).minus(Period.ofMonths(1)).toInstant();
        }
        if (end == null) {
            end = Instant.now();
        }

        final List<EquipmentData> equipmentData = equipmentDataRepository.getData(
                start.toString(),
                end.toString(),
                equipmentId
        );

        // todo query the last previous and retrieve if it was turned on
        return calculateDuration(equipmentData, start, end);
    }

    private StatisticsDto calculateDuration(List<EquipmentData> equipmentDataList, Instant start, Instant end) {
        Duration totalDuration = Duration.between(start, end);

        Duration totalUpDuration = Duration.ZERO;
        Instant previousTime = null;
        boolean previousEnabled = false;

        for (int i = 0; i < equipmentDataList.size(); i++) {
            EquipmentData equipmentData = equipmentDataList.get(i);
            Instant currentTime = equipmentData.getTime();

            if (previousEnabled) {
                Duration duration;
                if (previousTime != null) {
                    duration = Duration.between(previousTime, currentTime);
                } else {
                    duration = Duration.between(start, currentTime);
                }
                totalUpDuration = totalUpDuration.plus(duration);
            }

            previousTime = currentTime;
            previousEnabled = equipmentData.getEnabled();
        }
        if (previousEnabled) {
            Duration duration = Duration.between(previousTime, end);
            totalUpDuration = totalUpDuration.plus(duration);
        }

        final long upHours = totalUpDuration.toHours();
        final long downHours = totalDuration.minus(totalUpDuration).toHours();
        final long totalHours = totalDuration.toHours();
        return StatisticsDto.builder()
                .equipmentData(equipmentDataList)
                .upHours(upHours)
                .downHours(downHours)
                .totalHours(totalHours)
                .upPercent(upHours * 100.0 / totalHours)
                .start(start)
                .end(end)
                .build();
    }

    public void delete(@NotNull Long equipmentId) {
        equipmentDataRepository.delete(equipmentId);
    }
}
