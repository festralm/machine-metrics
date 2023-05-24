package ru.kpfu.machinemetrics.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@Builder
public class StatisticsDto {

    private OffsetDateTime start;
    private OffsetDateTime end;
    private Map<OffsetDateTime, ScheduleDto> schedules;
    private List<EquipmentDataDto> equipmentData;

    private Long upMinutes;
    private Long upScheduleMinutes;
    private Long upNotScheduleMinutes;
    private Long downMinutes;
    private Long downScheduleMinutes;

    private Double upSchedulePercent;
    private Double downSchedulePercent;
}
