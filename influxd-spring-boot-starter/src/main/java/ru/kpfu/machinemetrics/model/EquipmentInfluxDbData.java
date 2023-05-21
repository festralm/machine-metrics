package ru.kpfu.machinemetrics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentInfluxDbData {
    private Double u;
    private Boolean enabled;
}
