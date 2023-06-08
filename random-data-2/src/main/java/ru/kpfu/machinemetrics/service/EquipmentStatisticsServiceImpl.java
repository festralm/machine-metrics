package ru.kpfu.machinemetrics.service;

import org.springframework.stereotype.Service;
import ru.kpfu.machinemetrics.model.EquipmentInfluxDbData;
import ru.kpfu.machinemetrics.repository.EquipmentStatisticsRepository;

import java.util.Random;

@Service
public class EquipmentStatisticsServiceImpl extends EquipmentStatisticsService {

    private static final Random r = new Random();
    private static Double currValue = 0.0;
    private static int t = 1;
    private static int rate = 1;
    public EquipmentStatisticsServiceImpl(EquipmentStatisticsRepository equipmentStatisticsRepository) {
        super(equipmentStatisticsRepository);
    }

    @Override
    public EquipmentInfluxDbData getEquipmentInfluxDbData(Long equipmentId) {
        final double k = r.nextDouble();
        double u = currValue + (r.nextDouble() * 10) * rate * (k < 0.25 ? (-1) : 1) * t;
        if (u > 100) {
            u = 100;
            t = -1;
            rate = 5;
        } else if (u > 50) {
            rate = 1;
        } else if (u <= 0) {
            u = 0;
            t = 1;
            rate = r.nextDouble() < 1.0 / 3 ? 3 : 0;
        }
        currValue = u;
        return EquipmentInfluxDbData.builder()
                .u(u)
                .enabled(u > 20)
                .build();
    }
}
