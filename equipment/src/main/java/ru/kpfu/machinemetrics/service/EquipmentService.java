package ru.kpfu.machinemetrics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.model.Equipment;
import ru.kpfu.machinemetrics.repository.EquipmentRepository;

import java.util.List;
import java.util.Locale;

import static ru.kpfu.machinemetrics.constants.EquipmentConstants.EQUIPMENT_NOT_FOUND_EXCEPTION_MESSAGE;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final MessageSource messageSource;

    public List<Equipment> getAllNotDeleted() {
        return equipmentRepository.findAllByDeletedFalse();
    }

    public Equipment save(Equipment equipment) {
        return equipmentRepository.save(equipment);
    }

    public Equipment getById(Long id) {
        return equipmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> {
                    Locale locale = LocaleContextHolder.getLocale();
                    String message = messageSource.getMessage(
                            EQUIPMENT_NOT_FOUND_EXCEPTION_MESSAGE,
                            new Object[]{id},
                            locale
                    );
                    return new ResourceNotFoundException(message);
                });
    }

    public void delete(Long id) {
        Equipment equipment = getById(id);
        equipment.setDeleted(true);
        equipmentRepository.save(equipment);

        // todo delete from schedule
    }

    public Equipment edit(Long id, Equipment updatedEquipment) {
        Equipment equipment = getById(id);

        equipment.setPhotoPath(updatedEquipment.getPhotoPath());
        equipment.setInventoryNumber(updatedEquipment.getInventoryNumber());
        equipment.setName(updatedEquipment.getName());
        equipment.setCost(updatedEquipment.getCost());
        equipment.setSource(updatedEquipment.getSource());
        equipment.setDepartment(updatedEquipment.getDepartment());
        equipment.setResponsiblePerson(updatedEquipment.getResponsiblePerson());
        equipment.setStatus(updatedEquipment.getStatus());
        equipment.setReceiptDate(updatedEquipment.getReceiptDate());
        equipment.setLastOperationDate(updatedEquipment.getLastOperationDate());

        return equipmentRepository.save(equipment);
    }
}
