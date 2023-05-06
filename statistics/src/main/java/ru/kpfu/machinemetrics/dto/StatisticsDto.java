package ru.kpfu.machinemetrics.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.kpfu.machinemetrics.model.EquipmentData;

import java.time.Instant;
import java.util.List;

@Setter
@Getter
@Builder
public class StatisticsDto {

    private List<EquipmentData> equipmentData;
    private Long upHours;
    private Long downHours;
    private Long totalHours;
    private Double upPercent;
    private Instant start;
    private Instant end;
}