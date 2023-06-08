package ru.kpfu.machinemetrics.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.machinemetrics.dto.StatisticsDto;
import ru.kpfu.machinemetrics.service.EquipmentDataService;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping(value = "${app.api.prefix.v1}/equipment-data")
@RequiredArgsConstructor
public class EquipmentDataController {

    private final EquipmentDataService equipmentDataService;

    @GetMapping
    public StatisticsDto listFiltered(@RequestParam List<Long> ids,
                                      @RequestParam(required = false) OffsetDateTime start,
                                      @RequestParam(required = false) OffsetDateTime stop) {
        return equipmentDataService.getData(ids, start, stop);
    }
}
