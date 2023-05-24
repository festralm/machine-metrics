package ru.kpfu.machinemetrics.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
public class EquipmentDataDto {
    private Long equipmentId;
    private Double u;
    private Boolean enabled;
    private Boolean disabledDuringActiveTime;
    private Boolean enabledDuringPassiveTime;
    private OffsetDateTime time;
}
