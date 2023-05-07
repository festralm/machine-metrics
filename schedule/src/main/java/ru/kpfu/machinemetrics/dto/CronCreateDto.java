package ru.kpfu.machinemetrics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CronCreateDto {
    @NotBlank(message = "{validation.cron.empty}")
    private String expression;

    @NotNull(message = "{validation.cron.order.empty}")
    private Integer order;

    @NotBlank(message = "{validation.cron.name.empty}")
    private String name;
}
