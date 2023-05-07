package ru.kpfu.machinemetrics.service;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.model.EquipmentInfo;
import ru.kpfu.machinemetrics.repository.EquipmentInfoRepository;
import ru.kpfu.machinemetrics.task.FetchDataServiceTask;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;

import static ru.kpfu.machinemetrics.constants.EquipmentInfoConstants.EQUIPMENT_INFO_NOT_FOUND_EXCEPTION_MESSAGE;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentInfoService {

    private final EquipmentInfoRepository equipmentInfoRepository;
    private final MessageSource messageSource;
    private final TaskScheduler taskScheduler;
    private final RabbitTemplate rabbitTemplate;

    private ConcurrentMap<Long, ScheduledFuture<?>> equipmentIdToTask = new ConcurrentHashMap<>();

    @PostConstruct
    private void initTasks() {
        List<EquipmentInfo> equipmentInfos = equipmentInfoRepository.findAllByEnabledIsTrue();
        equipmentInfos.forEach(this::startTask);
    }

    public EquipmentInfo getById(@NotNull Long id) {
        return equipmentInfoRepository.findById(id)
                .orElseThrow(() -> {
                    Locale locale = LocaleContextHolder.getLocale();
                    String message = messageSource.getMessage(
                            EQUIPMENT_INFO_NOT_FOUND_EXCEPTION_MESSAGE,
                            new Object[]{id},
                            locale
                    );
                    return new ResourceNotFoundException(message);
                });
    }

    public void delete(@NotNull Long id) {
        EquipmentInfo equipmentInfo = getById(id);
        equipmentInfoRepository.delete(equipmentInfo);
        stopTask(id);
    }

    public EquipmentInfo save(@NotNull EquipmentInfo updatedEquipmentInfo) {
        final Long equipmentInfoId = updatedEquipmentInfo.getId();
        final EquipmentInfo savedEquipmentInfo = equipmentInfoRepository.save(updatedEquipmentInfo);
        if (savedEquipmentInfo.getEnabled()) {
            startTask(savedEquipmentInfo);
        } else {
            stopTask(equipmentInfoId);
        }
        return savedEquipmentInfo;
    }

    private void startTask(EquipmentInfo equipmentInfo) {
        stopTask(equipmentInfo.getId());
        CronTrigger cronTrigger = new CronTrigger(equipmentInfo.getCron().getId());
        Runnable fetchDataServiceTask = new FetchDataServiceTask(equipmentInfo.getDataService().getName(),
                equipmentInfo.getId(), rabbitTemplate);
        ScheduledFuture<?> taskFuture = taskScheduler.schedule(fetchDataServiceTask, cronTrigger);
        equipmentIdToTask.put(equipmentInfo.getId(), taskFuture);
    }

    private void stopTask(Long id) {
        final ScheduledFuture<?> taskFuture = equipmentIdToTask.get(id);
        if (taskFuture != null) {
            taskFuture.cancel(true);
            equipmentIdToTask.remove(id);
        }
    }
}
