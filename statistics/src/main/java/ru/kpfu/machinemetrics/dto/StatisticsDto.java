package ru.kpfu.machinemetrics.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Setter
@Getter
@Builder
public class StatisticsDto {

    private List<EquipmentDataDto> equipmentData;
    private Long upHours;
    private Long downHours;
    private Long totalHours;
    private Double upPercent;
    private OffsetDateTime start;
    private OffsetDateTime end;
    private List<ScheduleDto> schedules;
}
