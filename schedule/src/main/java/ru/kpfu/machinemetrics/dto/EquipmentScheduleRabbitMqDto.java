package ru.kpfu.machinemetrics.dto;

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
public class EquipmentScheduleRabbitMqDto {
    private Long id;

    private String cron;

    private boolean enabled;
}
