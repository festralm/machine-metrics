package ru.kpfu.equipment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.equipment.dto.EquipmentCreateDto;
import ru.kpfu.equipment.dto.EquipmentDetailsDto;
import ru.kpfu.equipment.dto.EquipmentItemDto;
import ru.kpfu.equipment.mapper.EquipmentMapper;
import ru.kpfu.equipment.model.Equipment;
import ru.kpfu.equipment.service.EquipmentService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "${api.prefix.v1}/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final EquipmentMapper equipmentMapper;

    @GetMapping
    public List<EquipmentItemDto> listAll() {
        List<Equipment> equipmentList = equipmentService.getAllNotDeleted();
        return equipmentMapper.toEquipmentItemDtos(equipmentList);
    }

    @PostMapping
    public ResponseEntity<EquipmentDetailsDto> create(@Valid @RequestBody EquipmentCreateDto equipmentCreateDto) {
        Equipment equipment = equipmentMapper.toEquipment(equipmentCreateDto);
        Equipment savedEquipment = equipmentService.save(equipment);
        EquipmentDetailsDto savedEquipmentDetailsDto = equipmentMapper.toEquipmentDetailsDto(savedEquipment);
        return ResponseEntity.created(URI.create("/equipment/" + savedEquipment.getId())).body(savedEquipmentDetailsDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentDetailsDto> get(@PathVariable Long id) {
        Equipment equipment = equipmentService.getById(id);
        EquipmentDetailsDto equipmentDetailsDto = equipmentMapper.toEquipmentDetailsDto(equipment);
        return ResponseEntity.ok(equipmentDetailsDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        equipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipmentDetailsDto> edit(@PathVariable Long id, @Valid @RequestBody EquipmentCreateDto equipmentCreateDto) {
        Equipment updatedEquipment = equipmentMapper.toEquipment(equipmentCreateDto);
        Equipment editedEquipment = equipmentService.edit(id, updatedEquipment);
        EquipmentDetailsDto editedEquipmentDetailsDto = equipmentMapper.toEquipmentDetailsDto(editedEquipment);
        return ResponseEntity.ok(editedEquipmentDetailsDto);
    }
}
