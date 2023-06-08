package ru.kpfu.machinemetrics.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.machinemetrics.dto.EquipmentCreateDto;
import ru.kpfu.machinemetrics.dto.EquipmentDetailsDto;
import ru.kpfu.machinemetrics.dto.EquipmentItemDto;
import ru.kpfu.machinemetrics.mapper.EquipmentMapper;
import ru.kpfu.machinemetrics.model.Equipment;
import ru.kpfu.machinemetrics.service.EquipmentService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "${app.api.prefix.v1}/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final EquipmentMapper equipmentMapper;

    @GetMapping
    public Page<EquipmentItemDto> listAll(@PageableDefault() Pageable pageable) {
        Page<Equipment> equipmentList = equipmentService.getAllNotDeleted(pageable);
        return equipmentMapper.toEquipmentItemDtos(equipmentList);
    }

    @GetMapping("/search")
    public List<EquipmentItemDto> search(
            @RequestParam(value = "unit", required = false) Long unit
    ) {
        List<Equipment> equipments = equipmentService.search(unit);
        return equipmentMapper.toEquipmentItemDtos(equipments);
    }

    @GetMapping("/search-pageable")
    public Page<EquipmentItemDto> search(
            @RequestParam(value = "name", required = false) String name,
            @PageableDefault Pageable pageable
    ) {
        Page<Equipment> equipmentPage = equipmentService.search(name, pageable);
        return equipmentMapper.toEquipmentItemDtos(equipmentPage);
    }

    @PostMapping
    public ResponseEntity<EquipmentDetailsDto> create(@Valid @RequestBody EquipmentCreateDto equipmentCreateDto) {
        Equipment equipment = equipmentMapper.toEquipment(equipmentCreateDto);
        Equipment savedEquipment = equipmentService.save(equipment);
        EquipmentDetailsDto savedEquipmentDetailsDto = equipmentMapper.toEquipmentDetailsDto(savedEquipment);
        return ResponseEntity.created(URI.create("/equipment/" + savedEquipment.getId())).body(savedEquipmentDetailsDto);
    }

    @GetMapping("/{id}")
    public EquipmentDetailsDto get(@PathVariable Long id) {
        Equipment equipment = equipmentService.getById(id);
        return equipmentMapper.toEquipmentDetailsDto(equipment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        equipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public EquipmentDetailsDto edit(@PathVariable Long id,
                                    @Valid @RequestBody EquipmentCreateDto equipmentCreateDto) {
        Equipment updatedEquipment = equipmentMapper.toEquipment(equipmentCreateDto);
        Equipment editedEquipment = equipmentService.edit(id, updatedEquipment);
        return equipmentMapper.toEquipmentDetailsDto(editedEquipment);
    }
}
