package ru.kpfu.randomdata.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.kpfu.randomdata.model.EquipmentData;
import ru.kpfu.randomdata.repository.EquipmentDataRepository;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class DataService {
    private final EquipmentDataRepository equipmentDataRepository;

    private static final Random r = new Random();

    @Scheduled(cron = "0 * * * * *")
    private void generateData() {
        final double u = r.nextDouble() * 100;
        EquipmentData equipmentData = EquipmentData.builder().equipmentId(1L).u(u).enabled(u > 20).build();
        equipmentDataRepository.save(equipmentData);
    }
}
