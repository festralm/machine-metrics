package ru.kpfu.machinemetrics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDto {

    private Long id;

    private Integer weekday;

    private OffsetDateTime date;

    private Long equipmentId;

    private Boolean isWorkday;

    private String startTime;

    private String endTime;
}
