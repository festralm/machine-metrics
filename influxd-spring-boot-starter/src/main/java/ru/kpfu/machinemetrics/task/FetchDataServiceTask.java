package ru.kpfu.machinemetrics.task;

import lombok.AllArgsConstructor;
import ru.kpfu.machinemetrics.service.EquipmentStatisticsService;

@AllArgsConstructor
public class FetchDataServiceTask implements Runnable {

    private Long equipmentId;
    private EquipmentStatisticsService equipmentStatisticsService;

    @Override
    public void run() {
        equipmentStatisticsService.process(equipmentId);
    }
}