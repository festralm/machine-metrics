package ru.kpfu.machinemetrics.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class EquipmentData {
    private Long equipmentId;
    private Double u;
    private Boolean enabled;
    private OffsetDateTime time;
}
