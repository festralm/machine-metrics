package ru.kpfu.equipment.mapper;

import org.mapstruct.Mapper;
import ru.kpfu.equipment.dto.EquipmentCreateDto;
import ru.kpfu.equipment.dto.EquipmentDetailsDto;
import ru.kpfu.equipment.dto.EquipmentItemDto;
import ru.kpfu.equipment.model.Equipment;

import java.util.List;

@Mapper
public interface EquipmentMapper {

    Equipment toEquipment(EquipmentCreateDto dto);

    EquipmentCreateDto toEquipmentCreateDto(Equipment equipment);

    EquipmentDetailsDto toEquipmentDetailsDto(Equipment equipment);

    List<EquipmentItemDto> toEquipmentItemDtos(List<Equipment> equipments);
}
