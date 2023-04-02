package ru.kpfu.machinemetrics.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.kpfu.machinemetrics.dto.equipment.EquipmentCreateDto;
import ru.kpfu.machinemetrics.dto.equipment.EquipmentDetailsDto;
import ru.kpfu.machinemetrics.dto.equipment.EquipmentItemDto;
import ru.kpfu.machinemetrics.model.Equipment;

import java.util.List;

@Mapper
public interface EquipmentMapper {

    Equipment toEquipment(EquipmentCreateDto dto);

    EquipmentCreateDto toEquipmentCreateDto(Equipment equipment);

    EquipmentDetailsDto toEquipmentDetailsDto(Equipment equipment);

    List<EquipmentCreateDto> toEquipmentCreateDtos(List<Equipment> equipments);

    List<EquipmentItemDto> toEquipmentItemDtos(List<Equipment> equipments);
}
