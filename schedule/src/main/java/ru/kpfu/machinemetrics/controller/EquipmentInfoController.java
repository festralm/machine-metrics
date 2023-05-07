package ru.kpfu.machinemetrics.controller;

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
import ru.kpfu.machinemetrics.dto.EquipmentInfoCreateDto;
import ru.kpfu.machinemetrics.mapper.EquipmentInfoMapper;
import ru.kpfu.machinemetrics.model.EquipmentInfo;
import ru.kpfu.machinemetrics.service.EquipmentInfoService;

import java.net.URI;

@RestController
@RequestMapping(value = "${api.prefix.v1}/equipment-info")
@RequiredArgsConstructor
public class EquipmentInfoController {

    private final EquipmentInfoService equipmentInfoService;
    private final EquipmentInfoMapper equipmentInfoMapper;


    @GetMapping("/{id}")
    public EquipmentInfo get(@PathVariable Long id) {
        return equipmentInfoService.getById(id);
    }

    @PostMapping
    public ResponseEntity<EquipmentInfo> save(@Valid @RequestBody EquipmentInfoCreateDto equipmentInfoCreateDto) {
        EquipmentInfo equipment = equipmentInfoMapper.toEquipmentInfo(equipmentInfoCreateDto);
        EquipmentInfo savedEquipmentInfo = equipmentInfoService.save(equipment);
        return ResponseEntity.created(URI.create("/equipment/" + savedEquipmentInfo.getId())).body(savedEquipmentInfo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        equipmentInfoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
