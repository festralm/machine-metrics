package ru.kpfu.machinemetrics.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.kpfu.machinemetrics.validation.annotation.EquipmentInfoConstraint;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EquipmentInfoConstraint
public class EquipmentInfoCreateDto {
    @NotNull(message = "{validation.equipment-info.id.empty}")
    private Long id;

    private Long dataServiceId;

    private String cronId;

    private boolean enabled;
}
