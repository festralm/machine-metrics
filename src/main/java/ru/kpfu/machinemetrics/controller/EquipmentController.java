package ru.kpfu.machinemetrics.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.machinemetrics.model.Equipment;
import ru.kpfu.machinemetrics.service.EquipmentService;

import java.util.List;

@RestController
@RequestMapping("/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping
    public List<Equipment> listAll() {
        return equipmentService.getAllNotDeleted();
    }
}
