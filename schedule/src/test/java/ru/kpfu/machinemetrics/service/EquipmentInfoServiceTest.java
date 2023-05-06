package ru.kpfu.machinemetrics.service;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import ru.kpfu.machinemetrics.configuration.MessageSourceConfig;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.model.Cron;
import ru.kpfu.machinemetrics.model.DataService;
import ru.kpfu.machinemetrics.model.EquipmentInfo;
import ru.kpfu.machinemetrics.repository.EquipmentInfoRepository;
import ru.kpfu.machinemetrics.task.FetchDataServiceTask;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.kpfu.machinemetrics.constants.EquipmentInfoConstants.EQUIPMENT_INFO_NOT_FOUND_EXCEPTION_MESSAGE;

@SpringBootTest
@ImportAutoConfiguration(MessageSourceConfig.class)
public class EquipmentInfoServiceTest {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private EquipmentInfoService equipmentInfoService;

    @MockBean
    private EquipmentInfoRepository equipmentInfoRepository;

    @MockBean
    private TaskScheduler taskScheduler;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void testSave() {
        // given
        EquipmentInfo equipmentInfo =
                EquipmentInfo.builder()
                        .id(1L)
                        .dataService(DataService.builder().id(1L).name("name").build())
                        .cron(Cron.builder().id("1 * * * * ?").build())
                        .enabled(true)
                        .build();

        EquipmentInfo savedEquipmentInfo =
                EquipmentInfo.builder().id(equipmentInfo.getId()).dataService(equipmentInfo.getDataService()).cron(equipmentInfo.getCron()).enabled(equipmentInfo.getEnabled()).build();

        when(equipmentInfoRepository.save(any(EquipmentInfo.class))).thenReturn(savedEquipmentInfo);
        ScheduledFuture<?> taskFuture = mock(ScheduledFuture.class);
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return taskFuture;
        }).when(taskScheduler).schedule(any(Runnable.class), any(CronTrigger.class));

        // when
        EquipmentInfo actualEquipmentInfo = equipmentInfoService.save(equipmentInfo);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualEquipmentInfo.getId()).isEqualTo(savedEquipmentInfo.getId());
        softly.assertThat(actualEquipmentInfo.getDataService().getId()).isEqualTo(savedEquipmentInfo.getDataService().getId());
        softly.assertThat(actualEquipmentInfo.getCron().getId()).isEqualTo(savedEquipmentInfo.getCron().getId());
        softly.assertThat(actualEquipmentInfo.getEnabled()).isEqualTo(savedEquipmentInfo.getEnabled());
        softly.assertAll();
        verify(taskScheduler, times(1)).schedule(any(FetchDataServiceTask.class), any(CronTrigger.class));
        verify(rabbitTemplate, times(1)).convertAndSend(eq("rk-name"), eq(equipmentInfo.getId()));
    }

    @Test
    public void testGetByIdFound() {
        // given
        EquipmentInfo equipmentInfo =
                EquipmentInfo.builder()
                        .id(1L)
                        .dataService(DataService.builder().id(1L).build())
                        .cron(Cron.builder().id("1 * * * * ?").build())
                        .enabled(true)
                        .build();

        when(equipmentInfoRepository.findById(equipmentInfo.getId())).thenReturn(Optional.of(equipmentInfo));

        // when
        EquipmentInfo actualEquipmentInfo = equipmentInfoService.getById(equipmentInfo.getId());

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualEquipmentInfo.getId()).isEqualTo(equipmentInfo.getId());
        softly.assertThat(actualEquipmentInfo.getDataService().getId()).isEqualTo(equipmentInfo.getDataService().getId());
        softly.assertThat(actualEquipmentInfo.getCron().getId()).isEqualTo(equipmentInfo.getCron().getId());
        softly.assertThat(actualEquipmentInfo.getEnabled()).isEqualTo(equipmentInfo.getEnabled());
        softly.assertAll();
    }

    @Test
    public void testGetByIdNotFound() {
        // given
        Long givenId = 1L;

        when(equipmentInfoRepository.findById(givenId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> equipmentInfoService.getById(givenId));

        // then
        String expectedMessage = messageSource.getMessage(EQUIPMENT_INFO_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{givenId}, new Locale("ru"));
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class).hasMessage(expectedMessage);
    }

    @Test
    void testDeleteWithExistingEquipmentInfo() {
        // given
        final Long equipmentInfoId = 1L;
        EquipmentInfo equipmentInfo = EquipmentInfo.builder()
                .id(equipmentInfoId)
                .dataService(DataService.builder().id(1L).build())
                .cron(Cron.builder().id("1 * * * * ?").build())
                .enabled(true)
                .build();

        when(equipmentInfoRepository.findById(equipmentInfoId)).thenReturn(Optional.of(equipmentInfo));

        // when
        equipmentInfoService.delete(equipmentInfoId);

        // then
        verify(equipmentInfoRepository, times(1)).findById(equipmentInfoId);
        verify(equipmentInfoRepository, times(1)).delete(equipmentInfo);
    }

    @Test
    void testDeleteWithNonExistingEquipmentInfo() {
        // given
        Long equipmentInfoId = 1L;
        when(equipmentInfoRepository.findById(equipmentInfoId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> equipmentInfoService.delete(equipmentInfoId));

        // then
        verify(equipmentInfoRepository, times(1)).findById(equipmentInfoId);
        verify(equipmentInfoRepository, Mockito.never()).save(Mockito.any(EquipmentInfo.class));
        String expectedMessage = messageSource.getMessage(EQUIPMENT_INFO_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{equipmentInfoId}, new Locale("ru"));
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class).hasMessage(expectedMessage);
    }

    @Test
    void testEdit() {
        // given
        final Long equipmentInfoId = 1L;
        EquipmentInfo updatedEquipmentInfo =
                EquipmentInfo.builder()
                        .id(equipmentInfoId)
                        .dataService(DataService.builder().id(1L).build())
                        .cron(Cron.builder().id("1 * * * * ?").build())
                        .enabled(false)
                        .build();

        when(equipmentInfoRepository.findById(equipmentInfoId)).thenReturn(Optional.of(updatedEquipmentInfo));
        when(equipmentInfoRepository.save(any(EquipmentInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        EquipmentInfo actualEquipmentInfo = equipmentInfoService.edit(updatedEquipmentInfo);

        // then
        verify(equipmentInfoRepository).findById(equipmentInfoId);
        verify(equipmentInfoRepository).save(updatedEquipmentInfo);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualEquipmentInfo.getId()).isEqualTo(updatedEquipmentInfo.getId());
        softly.assertThat(actualEquipmentInfo.getDataService().getId()).isEqualTo(updatedEquipmentInfo.getDataService().getId());
        softly.assertThat(actualEquipmentInfo.getCron().getId()).isEqualTo(updatedEquipmentInfo.getCron().getId());
        softly.assertThat(actualEquipmentInfo.getEnabled()).isEqualTo(updatedEquipmentInfo.getEnabled());
        softly.assertAll();
    }

    @Test
    void testEditEnable() {
        // given
        final Long equipmentInfoId = 1L;
        EquipmentInfo updatedEquipmentInfo =
                EquipmentInfo.builder()
                        .id(equipmentInfoId)
                        .dataService(DataService.builder().id(1L).name("name2").build())
                        .cron(Cron.builder().id("1 * * * * ?").build())
                        .enabled(true)
                        .build();

        when(equipmentInfoRepository.findById(equipmentInfoId)).thenReturn(Optional.of(updatedEquipmentInfo));
        when(equipmentInfoRepository.save(any(EquipmentInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ScheduledFuture<?> taskFuture = mock(ScheduledFuture.class);
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return taskFuture;
        }).when(taskScheduler).schedule(any(Runnable.class), any(CronTrigger.class));

        // when
        EquipmentInfo actualEquipmentInfo = equipmentInfoService.edit(updatedEquipmentInfo);

        // then
        verify(equipmentInfoRepository).findById(equipmentInfoId);
        verify(equipmentInfoRepository).save(updatedEquipmentInfo);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualEquipmentInfo.getId()).isEqualTo(updatedEquipmentInfo.getId());
        softly.assertThat(actualEquipmentInfo.getDataService().getId()).isEqualTo(updatedEquipmentInfo.getDataService().getId());
        softly.assertThat(actualEquipmentInfo.getCron().getId()).isEqualTo(updatedEquipmentInfo.getCron().getId());
        softly.assertThat(actualEquipmentInfo.getEnabled()).isEqualTo(updatedEquipmentInfo.getEnabled());
        softly.assertAll();
        verify(taskScheduler, times(1)).schedule(any(FetchDataServiceTask.class), any(CronTrigger.class));
        verify(rabbitTemplate, times(1)).convertAndSend(eq("rk-name2"), eq(updatedEquipmentInfo.getId()));
    }

    @Test
    void testEditEquipmentNotFound() {
        // given
        Long equipmentInfoId = 1L;

        when(equipmentInfoRepository.findById(equipmentInfoId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> equipmentInfoService.delete(equipmentInfoId));

        // then
        String expectedMessage = messageSource.getMessage(EQUIPMENT_INFO_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{equipmentInfoId}, new Locale("ru"));
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class).hasMessage(expectedMessage);
    }
}