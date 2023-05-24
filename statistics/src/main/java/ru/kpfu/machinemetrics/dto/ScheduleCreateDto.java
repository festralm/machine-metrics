package ru.kpfu.machinemetrics.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.kpfu.machinemetrics.validation.annotation.ScheduleConstraint;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ScheduleConstraint
public class ScheduleCreateDto {

    @Min(value = 1, message = "{validation.schedule.weekday.boundary}")
    @Max(value = 7, message = "{validation.schedule.weekday.boundary}")
    private Integer weekday;

    private OffsetDateTime date;

    private Long equipmentId;

    private Boolean isWorkday;

    private String startTime;

    private String endTime;
}
