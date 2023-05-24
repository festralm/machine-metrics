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

    @Mapping(source = "startTime", target = "startTime", qualifiedByName = "stringHourToInt")
    @Mapping(source = "endTime", target = "endTime", qualifiedByName = "stringHourToInt")
    Schedule toSchedule(ScheduleCreateDto dto);

    @Mapping(source = "startTime", target = "startTime", qualifiedByName = "intHourToString")
    @Mapping(source = "endTime", target = "endTime", qualifiedByName = "intHourToString")
    ScheduleDto toScheduleDto(Schedule schedule);

    List<ScheduleDto> toScheduleDtos(List<Schedule> schedules);

    @Named("stringHourToInt")
    default Integer stringTimeToLong(String time) {
        var hourAndMinute = time.split(":");

        return Integer.parseInt(hourAndMinute[0]) * 60 + Integer.parseInt(hourAndMinute[1]);
    }

    @Named("intHourToString")
    default String intHourToString(Integer time) {
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
