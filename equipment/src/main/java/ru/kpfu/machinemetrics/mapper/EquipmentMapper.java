package ru.kpfu.machinemetrics.mapper;

import org.mapstruct.Mapper;
import ru.kpfu.machinemetrics.dto.EquipmentCreateDto;
import ru.kpfu.machinemetrics.dto.EquipmentDetailsDto;
import ru.kpfu.machinemetrics.dto.EquipmentItemDto;
import ru.kpfu.machinemetrics.model.Equipment;

import java.util.List;

@Mapper
public interface EquipmentMapper {

    Equipment toEquipment(EquipmentCreateDto dto);

    EquipmentCreateDto toEquipmentCreateDto(Equipment equipment);

    EquipmentDetailsDto toEquipmentDetailsDto(Equipment equipment);

    List<EquipmentItemDto> toEquipmentItemDtos(List<Equipment> equipments);
}
