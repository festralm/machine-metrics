package ru.kpfu.machinemetrics.service;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import ru.kpfu.machinemetrics.model.EquipmentSchedule;
import ru.kpfu.machinemetrics.repository.EquipmentScheduleRepository;
import ru.kpfu.machinemetrics.task.FetchDataServiceTask;

import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {EquipmentScheduleService.class})
public class EquipmentScheduleServiceTest {

    @Autowired
    private EquipmentScheduleService equipmentScheduleService;

    @MockBean
    private EquipmentScheduleRepository equipmentScheduleRepository;

    @MockBean
    private EquipmentStatisticsService equipmentStatisticsService;

    @MockBean
    private TaskScheduler taskScheduler;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void testSaveAndEdit() {
        // given
        EquipmentSchedule equipmentSchedule =
                EquipmentSchedule.builder()
                        .id(1L)
                        .cron("* * * * * *")
                        .enabled(true)
                        .build();

        EquipmentSchedule savedEquipmentSchedule = EquipmentSchedule.builder()
                .id(equipmentSchedule.getId())
                .cron(equipmentSchedule.getCron())
                .enabled(equipmentSchedule.getEnabled())
                .build();

        when(equipmentScheduleRepository.save(any(EquipmentSchedule.class))).thenReturn(savedEquipmentSchedule);
        ScheduledFuture<?> taskFuture = mock(ScheduledFuture.class);
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return taskFuture;
        }).when(taskScheduler).schedule(any(Runnable.class), any(CronTrigger.class));

        // when
        EquipmentSchedule actualEquipmentSchedule = equipmentScheduleService.save(equipmentSchedule);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualEquipmentSchedule.getId()).isEqualTo(savedEquipmentSchedule.getId());
        softly.assertThat(actualEquipmentSchedule.getCron()).isEqualTo(savedEquipmentSchedule.getCron());
        softly.assertThat(actualEquipmentSchedule.getEnabled()).isEqualTo(savedEquipmentSchedule.getEnabled());
        verify(taskScheduler, times(1)).schedule(any(FetchDataServiceTask.class), any(CronTrigger.class));
        verify(equipmentStatisticsService, times(1)).process(equipmentSchedule.getId());

        // given
        equipmentSchedule.setCron(null);
        equipmentSchedule.setEnabled(false);

        savedEquipmentSchedule.setCron(equipmentSchedule.getCron());
        savedEquipmentSchedule.setEnabled(equipmentSchedule.getEnabled());

        when(equipmentScheduleRepository.save(any(EquipmentSchedule.class))).thenReturn(savedEquipmentSchedule);

        // when
        actualEquipmentSchedule = equipmentScheduleService.save(equipmentSchedule);

        // then
        softly.assertThat(actualEquipmentSchedule.getId()).isEqualTo(savedEquipmentSchedule.getId());
        softly.assertThat(actualEquipmentSchedule.getCron()).isEqualTo(savedEquipmentSchedule.getCron());
        softly.assertThat(actualEquipmentSchedule.getEnabled()).isEqualTo(savedEquipmentSchedule.getEnabled());
        softly.assertAll();
        verify(taskFuture, times(1)).cancel(any(Boolean.class));
    }

    @Test
    void testDeleteWithExistingEquipmentSchedule() {
        // given
        final Long equipmentScheduleId = 1L;
        EquipmentSchedule equipmentSchedule = EquipmentSchedule.builder()
                .id(equipmentScheduleId)
                .cron("* * * *")
                .enabled(true)
                .build();

        when(equipmentScheduleRepository.findById(equipmentScheduleId)).thenReturn(Optional.of(equipmentSchedule));

        // when
        equipmentScheduleService.delete(equipmentScheduleId);

        // then
        verify(equipmentScheduleRepository, times(1)).findById(equipmentScheduleId);
        verify(equipmentScheduleRepository, times(1)).delete(equipmentSchedule);
    }

    @Test
    void testDeleteWithNonExistingEquipmentSchedule() {
        // given
        Long equipmentScheduleId = 1L;
        when(equipmentScheduleRepository.findById(equipmentScheduleId)).thenReturn(Optional.empty());

        // when
        equipmentScheduleService.delete(equipmentScheduleId);

        // then
        verify(equipmentScheduleRepository, times(1)).findById(equipmentScheduleId);
        verify(equipmentScheduleRepository, Mockito.never()).delete(Mockito.any(EquipmentSchedule.class));
    }
}
