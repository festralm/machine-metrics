package ru.kpfu.machinemetrics.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.kpfu.machinemetrics.dto.ScheduleCreateDto;
import ru.kpfu.machinemetrics.dto.ScheduleDto;
import ru.kpfu.machinemetrics.model.Schedule;

import java.util.List;

@Mapper
public interface ScheduleMapper {

    @Mapping(source = "startTime", target = "startTime", qualifiedByName = "stringHourToLong")
    @Mapping(source = "endTime", target = "endTime", qualifiedByName = "stringHourToLong")
    Schedule toSchedule(ScheduleCreateDto dto);

    @Mapping(source = "startTime", target = "startTime", qualifiedByName = "longHourToString")
    @Mapping(source = "endTime", target = "endTime", qualifiedByName = "longHourToString")
    ScheduleDto toScheduleDto(Schedule schedule);

    List<ScheduleDto> toScheduleDtos(List<Schedule> schedules);

    @Named("stringHourToLong")
    default Long stringTimeToLong(String time) {
        var hourAndMinute = time.split(":");

        return Long.parseLong(hourAndMinute[0]) * 60 + Long.parseLong(hourAndMinute[1]);
    }

    @Named("longHourToString")
    default String longHourToString(Long time) {
        long hours = time / 60;

        String hoursString = Long.toString(hours);
        if (hoursString.length() == 1) {
            hoursString = "0" + hoursString;
        }

        String minutesString = String.valueOf(time - (hours * 60));
        if (minutesString.length() == 1) {
            minutesString = "0" + minutesString;
        }

        return hoursString + ":" + minutesString;
    }
}
