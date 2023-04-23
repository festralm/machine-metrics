package ru.kpfu.machinemetrics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kpfu.machinemetrics.model.EquipmentData;
import ru.kpfu.machinemetrics.repository.EquipmentDataRepository;

import java.time.Instant;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentDataService {

    private final EquipmentDataRepository equipmentDataRepository;

    public List<EquipmentData> getData(Long equipmentId, Instant start, Instant end) {
        if (start == null) {
            start = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).minus(Period.ofMonths(1)).toInstant();
        }
        if (end == null) {
            end = Instant.now();
        }
        return equipmentDataRepository.getData(start.toString(), end.toString(), equipmentId);
    }
}
