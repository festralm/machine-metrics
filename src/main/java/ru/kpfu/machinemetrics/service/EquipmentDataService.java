package ru.kpfu.machinemetrics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kpfu.machinemetrics.model.EquipmentData;
import ru.kpfu.machinemetrics.repository.EquipmentDataRepository;

@Service
@RequiredArgsConstructor
public class EquipmentDataService {
    private final EquipmentDataRepository equipmentDataRepository;

    // todo test
    public EquipmentData save(EquipmentData equipmentData) {
        return equipmentDataRepository.save(equipmentData);
    }
}
