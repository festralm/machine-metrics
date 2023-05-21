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
import ru.kpfu.machinemetrics.config.MessageSourceConfig;
import ru.kpfu.machinemetrics.exception.CannotDeleteScheduleException;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.exception.ScheduleIsAlreadyCreatedException;
import ru.kpfu.machinemetrics.model.Schedule;
import ru.kpfu.machinemetrics.repository.ScheduleRepository;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static ru.kpfu.machinemetrics.constants.ScheduleConstants.SCHEDULE_CREATED_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.ScheduleConstants.SCHEDULE_DELETE_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.ScheduleConstants.SCHEDULE_NOT_FOUND_EXCEPTION_MESSAGE;

@SpringBootTest
@ImportAutoConfiguration(MessageSourceConfig.class)
public class ScheduleServiceTest {

    @Autowired
    private MessageSource messageSource;

    @MockBean
    private ScheduleRepository scheduleRepositoryMock;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ScheduleService scheduleService;

    @Test
    public void testGetAll() {
        // given
        Schedule schedule1 = Schedule.builder()
                .id(1L)
                .startTime("11:00")
                .endTime("18:00")
                .build();
        Schedule schedule2 = Schedule.builder()
                .id(2L)
                .startTime("11:00")
                .endTime("18:00")
                .date(Instant.now())
                .build();
        List<Schedule> scheduleList = List.of(schedule1, schedule2);

        when(scheduleRepositoryMock.findAll()).thenReturn(scheduleList);

        // when
        List<Schedule> result = scheduleService.getAll();

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.size()).isEqualTo(2);
        softly.assertThat(result.get(0).getId()).isEqualTo(schedule1.getId());
        softly.assertThat(result.get(0).getStartTime()).isEqualTo(schedule1.getStartTime());
        softly.assertThat(result.get(0).getEndTime()).isEqualTo(schedule1.getEndTime());
        softly.assertThat(result.get(0).getDate()).isEqualTo(schedule1.getDate());
        softly.assertThat(result.get(1).getId()).isEqualTo(schedule2.getId());
        softly.assertThat(result.get(1).getStartTime()).isEqualTo(schedule2.getStartTime());
        softly.assertThat(result.get(1).getEndTime()).isEqualTo(schedule2.getEndTime());
        softly.assertThat(result.get(1).getDate()).isEqualTo(schedule2.getDate());
        softly.assertAll();
    }

    @Test
    void testSave() {
        // given
        Schedule schedule = Schedule.builder()
                .startTime("11:00")
                .endTime("18:00")
                .date(Instant.now())
                .build();

        Schedule savedSchedule = Schedule.builder()
                .id(1L)
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .date(schedule.getDate())
                .build();
        when(scheduleRepositoryMock.save(any(Schedule.class))).thenReturn(savedSchedule);

        // when
        Schedule actualSchedule = scheduleService.save(schedule);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualSchedule.getId()).isEqualTo(savedSchedule.getId());
        softly.assertThat(actualSchedule.getStartTime()).isEqualTo(savedSchedule.getStartTime());
        softly.assertThat(actualSchedule.getEndTime()).isEqualTo(savedSchedule.getEndTime());
        softly.assertThat(actualSchedule.getDate()).isEqualTo(savedSchedule.getDate());
        softly.assertAll();
    }

    @Test
    void testSaveWhenDefaultScheduleAlreadyExists() {
        // given
        Schedule schedule = Schedule.builder()
                .startTime("11:00")
                .endTime("18:00")
                .build();

        Schedule savedSchedule = Schedule.builder()
                .id(1L)
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .date(schedule.getDate())
                .build();

        Schedule defaultSchedule = Schedule.builder()
                .id(2L)
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .build();
        when(scheduleRepositoryMock.save(any(Schedule.class))).thenReturn(savedSchedule);
        when(scheduleRepositoryMock.findByDate(any())).thenReturn(Optional.of(defaultSchedule));

        // when
        Throwable thrown = catchThrowable(() -> scheduleService.save(schedule));

        // then
        String expectedMessage = messageSource.getMessage(
                SCHEDULE_CREATED_EXCEPTION_MESSAGE,
                new Object[]{},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ScheduleIsAlreadyCreatedException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    void testDeleteWithExistingSchedule() {
        // given
        Long scheduleId = 1L;

        Schedule schedule = Schedule.builder()
                .id(scheduleId)
                .startTime("11:00")
                .endTime("18:00")
                .date(Instant.now())
                .build();

        when(scheduleRepositoryMock.findById(scheduleId)).thenReturn(Optional.of(schedule));

        // when
        scheduleService.delete(scheduleId);

        // then
        verify(scheduleRepositoryMock, Mockito.times(1)).findById(scheduleId);
        verify(scheduleRepositoryMock, Mockito.times(1)).delete(schedule);
    }

    @Test
    void testDeleteWithNonExistingSchedule() {
        // given
        Long scheduleId = 1L;
        when(scheduleRepositoryMock.findById(scheduleId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> scheduleService.delete(scheduleId));

        // then
        verify(scheduleRepositoryMock, Mockito.times(1)).findById(scheduleId);
        verifyNoInteractions(rabbitTemplate);
        String expectedMessage = messageSource.getMessage(
                SCHEDULE_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{scheduleId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    void testDeleteDefaultSchedule() {
        // given
        Long scheduleId = 1L;

        Schedule schedule = Schedule.builder()
                .id(scheduleId)
                .startTime("11:00")
                .endTime("18:00")
                .build();
        when(scheduleRepositoryMock.findById(scheduleId)).thenReturn(Optional.of(schedule));

        // when
        Throwable thrown = catchThrowable(() -> scheduleService.delete(scheduleId));

        // then
        verify(scheduleRepositoryMock, Mockito.times(1)).findById(scheduleId);
        verifyNoInteractions(rabbitTemplate);
        String expectedMessage = messageSource.getMessage(
                SCHEDULE_DELETE_EXCEPTION_MESSAGE,
                new Object[]{scheduleId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(CannotDeleteScheduleException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    void testEdit() {
        // given
        Long scheduleId = 1L;

        Schedule existingSchedule = Schedule.builder()
                .id(scheduleId)
                .startTime("11:00")
                .endTime("18:00")
                .date(Instant.now())
                .build();

        Schedule updatedSchedule = Schedule.builder()
                .id(scheduleId)
                .startTime("10:00")
                .endTime("19:00")
                .date(Instant.now())
                .build();

        when(scheduleRepositoryMock.findById(scheduleId)).thenReturn(Optional.of(existingSchedule));
        when(scheduleRepositoryMock.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Schedule actualSchedule = scheduleService.edit(scheduleId, updatedSchedule);

        // then
        verify(scheduleRepositoryMock).findById(scheduleId);
        verify(scheduleRepositoryMock).save(existingSchedule);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualSchedule.getId()).isEqualTo(existingSchedule.getId());
        softly.assertThat(actualSchedule.getStartTime()).isEqualTo(updatedSchedule.getStartTime());
        softly.assertThat(actualSchedule.getEndTime()).isEqualTo(updatedSchedule.getEndTime());
        softly.assertThat(actualSchedule.getDate()).isEqualTo(updatedSchedule.getDate());
        softly.assertAll();
    }

    @Test
    void testEditScheduleNotFound() {
        // given
        Long scheduleId = 1L;

        Schedule updatedSchedule = Schedule.builder()
                .id(scheduleId)
                .startTime("10:00")
                .endTime("19:00")
                .date(Instant.now())
                .build();

        when(scheduleRepositoryMock.findById(scheduleId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> scheduleService.edit(scheduleId, updatedSchedule));

        // then
        String expectedMessage = messageSource.getMessage(
                SCHEDULE_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{scheduleId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    void testEditExistingDefaultSchedule() {
        // given
        Long scheduleId = 1L;

        Schedule existingSchedule = Schedule.builder()
                .id(scheduleId)
                .startTime("11:00")
                .endTime("18:00")
                .date(Instant.now())
                .build();

        Schedule updatedSchedule = Schedule.builder()
                .id(scheduleId)
                .startTime("10:00")
                .endTime("19:00")
                .build();

        Schedule defaultSchedule = Schedule.builder()
                .id(2L)
                .startTime("10:00")
                .endTime("19:00")
                .build();

        when(scheduleRepositoryMock.findById(scheduleId)).thenReturn(Optional.of(existingSchedule));
        when(scheduleRepositoryMock.findByDate(null)).thenReturn(Optional.of(defaultSchedule));

        // when
        Throwable thrown = catchThrowable(() -> scheduleService.edit(scheduleId, updatedSchedule));

        // then
        String expectedMessage = messageSource.getMessage(
                SCHEDULE_CREATED_EXCEPTION_MESSAGE,
                new Object[]{scheduleId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ScheduleIsAlreadyCreatedException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    void testEditExistingScheduleToDefault() {
        // given
        Long scheduleId = 1L;

        Schedule existingSchedule = Schedule.builder()
                .id(scheduleId)
                .startTime("11:00")
                .endTime("18:00")
                .build();

        Schedule updatedSchedule = Schedule.builder()
                .id(scheduleId)
                .startTime("10:00")
                .endTime("19:00")
                .date(Instant.now())
                .build();

        when(scheduleRepositoryMock.findById(scheduleId)).thenReturn(Optional.of(existingSchedule));
        when(scheduleRepositoryMock.findByDate(any())).thenReturn(Optional.of(existingSchedule));

        // when
        Throwable thrown = catchThrowable(() -> scheduleService.edit(scheduleId, updatedSchedule));

        // then
        String expectedMessage = messageSource.getMessage(
                SCHEDULE_DELETE_EXCEPTION_MESSAGE,
                new Object[]{scheduleId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(CannotDeleteScheduleException.class)
                .hasMessage(expectedMessage);
    }
}
