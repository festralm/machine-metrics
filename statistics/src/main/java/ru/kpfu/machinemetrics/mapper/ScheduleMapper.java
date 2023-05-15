package ru.kpfu.machinemetrics.mapper;

import org.mapstruct.Mapper;
import ru.kpfu.machinemetrics.dto.ScheduleCreateDto;
import ru.kpfu.machinemetrics.dto.ScheduleDto;
import ru.kpfu.machinemetrics.model.Schedule;

import java.util.List;

@Mapper
public interface ScheduleMapper {

    Schedule toSchedule(ScheduleCreateDto dto);

    ScheduleDto toScheduleDto(Schedule schedule);

    List<ScheduleDto> toScheduleDtos(List<Schedule> schedules);
}
