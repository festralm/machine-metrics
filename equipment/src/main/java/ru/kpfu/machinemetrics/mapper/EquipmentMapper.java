package ru.kpfu.machinemetrics.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.kpfu.machinemetrics.dto.EquipmentCreateDto;
import ru.kpfu.machinemetrics.dto.EquipmentDetailsDto;
import ru.kpfu.machinemetrics.dto.EquipmentItemDto;
import ru.kpfu.machinemetrics.model.Country;
import ru.kpfu.machinemetrics.model.Equipment;
import ru.kpfu.machinemetrics.model.Purpose;
import ru.kpfu.machinemetrics.model.Status;
import ru.kpfu.machinemetrics.model.Unit;
import ru.kpfu.machinemetrics.model.UsageType;

import java.util.List;

@Mapper(uses = {UnitMapper.class})
public interface EquipmentMapper {

    @Mapping(source = "purpose", target = "purpose", qualifiedByName = "mapToPurpose")
    @Mapping(source = "usageType", target = "usageType", qualifiedByName = "mapToUsageType")
    @Mapping(source = "manufacturerCountry", target = "manufacturerCountry", qualifiedByName = "mapToCountry")
    @Mapping(source = "unit", target = "unit", qualifiedByName = "mapToUnit")
    @Mapping(source = "status", target = "status", qualifiedByName = "mapToStatus")
    Equipment toEquipment(EquipmentCreateDto dto);

    @Named("mapToPurpose")
    @Mapping(source = ".", target = "id")
    Purpose mapToPurpose(Long id);

    @Named("mapToUsageType")
    @Mapping(source = ".", target = "id")
    UsageType mapToUsageType(Long id);

    @Named("mapToCountry")
    @Mapping(source = ".", target = "id")
    Country mapToCountry(Long id);

    @Named("mapToUnit")
    @Mapping(source = ".", target = "id")
    Unit mapToUnit(Long id);

    @Named("mapToStatus")
    @Mapping(source = ".", target = "id")
    Status mapToStatus(Long id);

    EquipmentDetailsDto toEquipmentDetailsDto(Equipment equipment);

    List<EquipmentItemDto> toEquipmentItemDtos(List<Equipment> equipments);

    @Mapping(target = "status", source = "status.name")
    EquipmentItemDto toEquipmentItemDto(Equipment equipments);
}
