package ru.kpfu.machinemetrics.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.machinemetrics.dto.EquipmentDto;
import ru.kpfu.machinemetrics.mapper.EquipmentMapper;
import ru.kpfu.machinemetrics.model.Equipment;
import ru.kpfu.machinemetrics.service.EquipmentService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("${api.prefix.v1}/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final EquipmentMapper equipmentMapper;

    @GetMapping
    public List<EquipmentDto> listAll() {
        List<Equipment> equipmentList = equipmentService.getAllNotDeleted();
        return equipmentMapper.toEquipmentDtos(equipmentList);
    }

    @PostMapping
    public ResponseEntity<EquipmentDto> create(@Valid @RequestBody EquipmentDto equipmentDto) {
        Equipment equipment = equipmentMapper.toEquipment(equipmentDto);
        Equipment savedEquipment = equipmentService.save(equipment);
        EquipmentDto savedEquipmentDto = equipmentMapper.toEquipmentDto(savedEquipment);
        return ResponseEntity.created(URI.create("/equipment/" + savedEquipmentDto.getId())).body(savedEquipmentDto);
    }
}
