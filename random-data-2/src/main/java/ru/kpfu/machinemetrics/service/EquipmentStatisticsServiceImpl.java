package ru.kpfu.machinemetrics.service;

import org.springframework.stereotype.Service;
import ru.kpfu.machinemetrics.model.EquipmentInfluxDbData;
import ru.kpfu.machinemetrics.repository.EquipmentStatisticsRepository;

import java.util.Random;

@Service
public class EquipmentStatisticsServiceImpl extends EquipmentStatisticsService {

    private static final Random r = new Random();

    public EquipmentStatisticsServiceImpl(EquipmentStatisticsRepository equipmentStatisticsRepository) {
        super(equipmentStatisticsRepository);
    }

    @Override
    public EquipmentInfluxDbData getEquipmentInfluxDbData(Long equipmentId) {
        final double u = r.nextDouble() * 100;
        return EquipmentInfluxDbData.builder()
                .u(u)
                .enabled(u > 20)
                .build();
    }
}
