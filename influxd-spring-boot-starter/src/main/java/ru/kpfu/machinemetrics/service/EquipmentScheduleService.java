package ru.kpfu.machinemetrics.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kpfu.machinemetrics.model.EquipmentSchedule;
import ru.kpfu.machinemetrics.repository.EquipmentScheduleRepository;
import ru.kpfu.machinemetrics.task.FetchDataServiceTask;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentScheduleService {

    private final EquipmentScheduleRepository equipmentScheduleRepository;
    private final TaskScheduler taskScheduler;
    private final EquipmentStatisticsService equipmentStatisticsService;

    private ConcurrentMap<Long, ScheduledFuture<?>> equipmentIdToTask = new ConcurrentHashMap<>();

    @PostConstruct
    private void initTasks() {
        List<EquipmentSchedule> equipmentSchedules = equipmentScheduleRepository.findAllByEnabledIsTrue();
        equipmentSchedules.forEach(this::startTask);
    }

    public void delete(Long id) {
        var toDelete = equipmentScheduleRepository.findById(id);
        toDelete.ifPresent(equipmentScheduleRepository::delete);
        stopTask(id);
    }

    public EquipmentSchedule save(EquipmentSchedule updatedEquipmentSchedule) {
        final Long equipmentInfoId = updatedEquipmentSchedule.getId();
        final EquipmentSchedule savedEquipmentSchedule = equipmentScheduleRepository.save(updatedEquipmentSchedule);
        if (savedEquipmentSchedule.getEnabled()) {
            startTask(savedEquipmentSchedule);
        } else {
            stopTask(equipmentInfoId);
        }
        return savedEquipmentSchedule;
    }

    private void startTask(EquipmentSchedule equipmentSchedule) {
        stopTask(equipmentSchedule.getId());
        CronTrigger cronTrigger = new CronTrigger(equipmentSchedule.getCron());
        Runnable fetchDataServiceTask = new FetchDataServiceTask(equipmentSchedule.getId(), equipmentStatisticsService);
        ScheduledFuture<?> taskFuture = taskScheduler.schedule(fetchDataServiceTask, cronTrigger);
        equipmentIdToTask.put(equipmentSchedule.getId(), taskFuture);
    }

    private void stopTask(Long id) {
        final ScheduledFuture<?> taskFuture = equipmentIdToTask.get(id);
        if (taskFuture != null) {
            taskFuture.cancel(true);
            equipmentIdToTask.remove(id);
        }
    }
}
