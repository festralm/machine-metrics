package ru.kpfu.machinemetrics.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.machinemetrics.model.EquipmentData;
import ru.kpfu.machinemetrics.service.EquipmentDataService;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping(value = "${api.prefix.v1}/equipment-data")
@RequiredArgsConstructor
public class EquipmentDataController {

    private final EquipmentDataService equipmentDataService;

    @GetMapping
    public List<EquipmentData> listFiltered(@RequestParam(required = false) Long id,
                                            @RequestParam(required = false) Instant start,
                                            @RequestParam(required = false) Instant stop) {
        return equipmentDataService.getData(id, start, stop);
    }
}
