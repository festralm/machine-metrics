package ru.kpfu.machinemetrics.dto;

import jakarta.validation.constraints.NotBlank;
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
public class ScheduleCreateDto {

    @NotBlank(message = "{validation.schedule.start-time.empty}")
    private String startTime;

    @NotBlank(message = "{validation.schedule.end-time.empty}")
    private String endTime;

    private Instant date;
}
