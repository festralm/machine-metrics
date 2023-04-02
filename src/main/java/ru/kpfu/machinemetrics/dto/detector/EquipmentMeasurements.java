package ru.kpfu.machinemetrics.dto.detector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentMeasurements {

    private Double uParam;

    private Double iParam;

    private Double pParam;
}
