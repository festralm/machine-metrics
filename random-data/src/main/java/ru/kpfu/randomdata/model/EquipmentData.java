package ru.kpfu.randomdata.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EquipmentData {
    Long equipmentId;
    Double u;
    Boolean enabled;
}
