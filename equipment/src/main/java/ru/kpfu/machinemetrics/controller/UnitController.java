package ru.kpfu.machinemetrics.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.machinemetrics.dto.UnitCreateDto;
import ru.kpfu.machinemetrics.dto.UnitDto;
import ru.kpfu.machinemetrics.mapper.UnitMapper;
import ru.kpfu.machinemetrics.model.Unit;
import ru.kpfu.machinemetrics.service.UnitService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "${app.api.prefix.v1}/unit")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;
    private final UnitMapper unitMapper;

    @GetMapping
    public List<UnitDto> listAll() {
        List<Unit> unitList = unitService.getAll();
        List<UnitDto> savedDtoList = unitMapper.toUnitDtos(unitList);
        return savedDtoList;
    }

    @PostMapping
    public ResponseEntity<UnitDto> create(@Valid @RequestBody UnitCreateDto dto) {
        Unit unit = unitMapper.toUnit(dto);
        Unit savedUnit = unitService.save(unit);
        UnitDto savedDto = unitMapper.toUnitDto(savedUnit);
        return ResponseEntity.created(URI.create("/unit")).body(savedDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        unitService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
