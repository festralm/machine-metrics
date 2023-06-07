package ru.kpfu.machinemetrics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kpfu.machinemetrics.constants.EquipmentInfoConstants;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.mapper.EquipmentInfoMapper;
import ru.kpfu.machinemetrics.model.DataService;
import ru.kpfu.machinemetrics.model.EquipmentInfo;
import ru.kpfu.machinemetrics.repository.EquipmentInfoRepository;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentInfoService {

    private final EquipmentInfoRepository equipmentScheduleRepository;
    private final EquipmentInfoMapper equipmentInfoMapper;
    private final MessageSource messageSource;
    private final RabbitTemplate rabbitTemplate;

    public EquipmentInfo getById(Long id) {
        return equipmentScheduleRepository.findById(id)
                .orElseThrow(() -> {
                    Locale locale = LocaleContextHolder.getLocale();
                    String message = messageSource.getMessage(
                            EquipmentInfoConstants.EQUIPMENT_INFO_NOT_FOUND_EXCEPTION_MESSAGE,
                            new Object[]{id},
                            locale
                    );
                    return new ResourceNotFoundException(message);
                });
    }

    public void delete(Long id) {
        EquipmentInfo equipmentInfo = getById(id);
        equipmentScheduleRepository.delete(equipmentInfo);
        rabbitTemplate.convertAndSend(
                String.format("rk-%s-delete", equipmentInfo.getDataService().getName()),
                id
        );
    }

    public EquipmentInfo save(EquipmentInfo updatedEquipmentInfo) {
        Optional<EquipmentInfo> oldEquipmentInfoOpt = equipmentScheduleRepository.findById(updatedEquipmentInfo.getId());

        DataService oldDataService = null;
        if (oldEquipmentInfoOpt.isPresent()) {
            oldDataService = oldEquipmentInfoOpt.get().getDataService();
        }
        final EquipmentInfo savedEquipmentInfo = equipmentScheduleRepository.save(updatedEquipmentInfo);

        boolean isNotSameService;
        if (savedEquipmentInfo.getDataService() != null) {
            rabbitTemplate.convertAndSend(
                        String.format("rk-%s", savedEquipmentInfo.getDataService().getName()),
                    equipmentInfoMapper.toEquipmentRabbitMqDto(savedEquipmentInfo)
            );
        }

        if (oldDataService != null) {
            isNotSameService = savedEquipmentInfo.getDataService() == null ||
                    !oldDataService.getId().equals(savedEquipmentInfo.getDataService().getId());
            if (isNotSameService) {
                rabbitTemplate.convertAndSend(
                        String.format("rk-%s-delete", oldDataService.getName()),
                        savedEquipmentInfo.getId()
                );
            }
        }

        return savedEquipmentInfo;
    }
}
