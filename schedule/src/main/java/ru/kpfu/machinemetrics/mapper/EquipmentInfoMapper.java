package ru.kpfu.machinemetrics.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.kpfu.machinemetrics.dto.EquipmentInfoCreateDto;
import ru.kpfu.machinemetrics.model.Cron;
import ru.kpfu.machinemetrics.model.DataService;
import ru.kpfu.machinemetrics.model.EquipmentInfo;

@Mapper
public interface EquipmentInfoMapper {

    @Mapping(source = "dataServiceId", target = "dataService", qualifiedByName = "mapToDataService")
    @Mapping(source = "cronId", target = "cron", qualifiedByName = "mapToCron")
    EquipmentInfo toEquipmentInfo(EquipmentInfoCreateDto dto);

    @Named("mapToDataService")
    @Mapping(source = ".", target = "id")
    DataService mapToDataService(Long id);

    @Named("mapToCron")
    @Mapping(source = ".", target = "id")
    Cron mapToCron(String id);
}
