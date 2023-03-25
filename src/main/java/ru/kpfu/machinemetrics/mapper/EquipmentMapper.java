package ru.kpfu.machinemetrics.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.kpfu.machinemetrics.dto.EquipmentDto;
import ru.kpfu.machinemetrics.model.Equipment;

import java.util.List;

@Mapper
public interface EquipmentMapper {

    EquipmentMapper INSTANCE = Mappers.getMapper(EquipmentMapper.class);

    Equipment toEquipment(EquipmentDto dto);

    EquipmentDto toEquipmentDto(Equipment equipment);

    List<EquipmentDto> toEquipmentDtos(List<Equipment> equipments);
}
