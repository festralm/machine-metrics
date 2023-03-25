package ru.kpfu.machinemetrics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kpfu.machinemetrics.model.Equipment;
import ru.kpfu.machinemetrics.repository.EquipmentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public List<Equipment> getAllNotDeleted() {
        return equipmentRepository.findByDeletedFalse();
    }

    public Equipment save(Equipment equipment) {
        equipment.setDeleted(false);
        return equipmentRepository.save(equipment);
    }
}
