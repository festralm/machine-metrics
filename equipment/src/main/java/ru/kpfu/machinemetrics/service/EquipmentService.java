package ru.kpfu.machinemetrics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.model.Equipment;
import ru.kpfu.machinemetrics.repository.EquipmentRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.kpfu.machinemetrics.constants.EquipmentConstants.EQUIPMENT_NOT_FOUND_EXCEPTION_MESSAGE;

@Service
@Transactional
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final MessageSource messageSource;
    private final RabbitTemplate rabbitTemplate;
    private final PhotoService photoService;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public Page<Equipment> getAllNotDeleted(Pageable pageable) {
        return equipmentRepository.findAllByDeletedFalseOrderByName(pageable);
    }

    public Page<Equipment> search(String name, Pageable pageable) {
        return equipmentRepository.searchAllByNameContainingOrderByName(name, pageable);
    }

    public Equipment save(Equipment equipment) {
        equipment.setLastOperationDate(OffsetDateTime.now());
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

        deleteUnusedPhotos();

        rabbitTemplate.convertAndSend("rk-equipment", "", id);
    }

    public Equipment edit(Long id, Equipment updatedEquipment) {
        Equipment equipment = getById(id);

        equipment.setName(updatedEquipment.getName());
        equipment.setInventoryNumber(updatedEquipment.getInventoryNumber());
        equipment.setAcquisitionSource(updatedEquipment.getAcquisitionSource());
        equipment.setCost(updatedEquipment.getCost());
        equipment.setInitialCost(updatedEquipment.getInitialCost());
        equipment.setResidualCost(updatedEquipment.getResidualCost());
        equipment.setAdName(updatedEquipment.getAdName());
        equipment.setIpAddress(updatedEquipment.getIpAddress());
        equipment.setKfuDevelopmentProgramApplication(updatedEquipment.getKfuDevelopmentProgramApplication());
        equipment.setWarrantyServiceForRepresentativesOfAForeignParty(
                updatedEquipment.isWarrantyServiceForRepresentativesOfAForeignParty()
        );
        equipment.setKfuDevelopmentProgramPriorityDirection(
                updatedEquipment.getKfuDevelopmentProgramPriorityDirection()
        );
        equipment.setRussiaDevelopmentPriorityDirection(
                updatedEquipment.getRussiaDevelopmentPriorityDirection()
        );
        equipment.setArea(updatedEquipment.getArea());
        equipment.setResearchObjects(updatedEquipment.getResearchObjects());
        equipment.setIndicators(updatedEquipment.getIndicators());
        equipment.setAdditionalFeatures(updatedEquipment.getAdditionalFeatures());
        equipment.setPurpose(updatedEquipment.getPurpose());
        equipment.setUsageType(updatedEquipment.getUsageType());
        equipment.setVerificationRequired(updatedEquipment.isVerificationRequired());
        equipment.setType(updatedEquipment.getType());
        equipment.setFactoryNumber(updatedEquipment.getFactoryNumber());
        equipment.setManufacturerCountry(updatedEquipment.getManufacturerCountry());
        equipment.setManufactureYear(updatedEquipment.getManufactureYear());
        equipment.setManufacturer(updatedEquipment.getManufacturer());
        equipment.setDeliveryDate(updatedEquipment.getDeliveryDate());
        equipment.setSupplier(updatedEquipment.getSupplier());
        equipment.setCommissioningDate(updatedEquipment.getCommissioningDate());
        equipment.setBrand(updatedEquipment.getBrand());
        equipment.setProvidingServicesToThirdPartiesPossibility(
                updatedEquipment.isProvidingServicesToThirdPartiesPossibility()
        );
        equipment.setCollectiveFederalCenterUse(updatedEquipment.isCollectiveFederalCenterUse());
        equipment.setUnique(updatedEquipment.isUnique());
        equipment.setCollectiveInterdisciplinaryCenterUse(updatedEquipment.isCollectiveInterdisciplinaryCenterUse());
        equipment.setPortalPublicationCardReadiness(updatedEquipment.isPortalPublicationCardReadiness());
        equipment.setInstallationLocation(updatedEquipment.getInstallationLocation());
        equipment.setUnit(updatedEquipment.getUnit());
        equipment.setResponsiblePerson(updatedEquipment.getResponsiblePerson());
        equipment.setStatus(updatedEquipment.getStatus());
        equipment.setLastOperationDate(OffsetDateTime.now());
        equipment.setPhotoPath(updatedEquipment.getPhotoPath());

        deleteUnusedPhotos();
        return equipmentRepository.save(equipment);
    }

    private void deleteUnusedPhotos() {
        List<String> photoNames = equipmentRepository.findAllByDeletedFalseOrderByName()
                .stream()
                .map(Equipment::getPhotoPath)
                .filter(StringUtils::hasText)
                .toList();

        executorService.submit(() -> photoService.deleteUnusedPhotos(photoNames));
    }
}
