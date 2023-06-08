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
    private List<EquipmentStatisticsDto> equipmentStatisticsDtos;
}
