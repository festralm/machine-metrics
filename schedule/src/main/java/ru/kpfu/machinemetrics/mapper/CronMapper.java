package ru.kpfu.machinemetrics.mapper;

import org.mapstruct.Mapper;
import ru.kpfu.machinemetrics.dto.CronCreateDto;
import ru.kpfu.machinemetrics.model.Cron;

@Mapper
public interface CronMapper {

    Cron toCron(CronCreateDto dto);
}
