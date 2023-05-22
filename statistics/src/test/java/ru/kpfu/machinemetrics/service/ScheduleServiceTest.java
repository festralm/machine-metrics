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

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    public void testListDefault() {
        // given
        Schedule schedule1 = Schedule.builder()
                .id(1L)
                .weekday(1)
                .date(OffsetDateTime.now())
                .equipmentId(1L)
                .startTime(60L)
                .endTime(18 * 60L)
                .build();
        Schedule schedule2 = Schedule.builder()
                .id(2L)
                .weekday(2)
                .date(OffsetDateTime.now())
                .equipmentId(2L)
                .startTime(2 * 60L + 30)
                .endTime(17 * 60L + 15)
                .build();
        List<Schedule> scheduleList = List.of(schedule1, schedule2);

        when(scheduleRepositoryMock.findAllByDateAndEquipmentId(null, null)).thenReturn(scheduleList);

        // when
        List<Schedule> result = scheduleService.listDefault();

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.size()).isEqualTo(2);
        softly.assertThat(result.get(0).getId()).isEqualTo(schedule1.getId());
        softly.assertThat(result.get(0).getWeekday()).isEqualTo(schedule1.getWeekday());
        softly.assertThat(result.get(0).getDate()).isEqualTo(schedule1.getDate());
        softly.assertThat(result.get(0).getEquipmentId()).isEqualTo(schedule1.getEquipmentId());
        softly.assertThat(result.get(0).getStartTime()).isEqualTo(schedule1.getStartTime());
        softly.assertThat(result.get(0).getEndTime()).isEqualTo(schedule1.getEndTime());
        softly.assertThat(result.get(1).getId()).isEqualTo(schedule2.getId());
        softly.assertThat(result.get(1).getWeekday()).isEqualTo(schedule2.getWeekday());
        softly.assertThat(result.get(1).getDate()).isEqualTo(schedule2.getDate());
        softly.assertThat(result.get(1).getEquipmentId()).isEqualTo(schedule2.getEquipmentId());
        softly.assertThat(result.get(1).getStartTime()).isEqualTo(schedule2.getStartTime());
        softly.assertThat(result.get(1).getEndTime()).isEqualTo(schedule2.getEndTime());
        softly.assertAll();
    }

    @Test
    public void testListNotDefault() {
        // given
        Schedule schedule1 = Schedule.builder()
                .id(1L)
                .weekday(1)
                .date(OffsetDateTime.now())
                .equipmentId(1L)
                .startTime(60L)
                .endTime(18 * 60L)
                .build();
        Schedule schedule2 = Schedule.builder()
                .id(2L)
                .weekday(2)
                .date(OffsetDateTime.now())
                .equipmentId(2L)
                .startTime(2 * 60L + 30)
                .endTime(17 * 60L + 15)
                .build();
        List<Schedule> scheduleList = List.of(schedule1, schedule2);

        when(scheduleRepositoryMock.findAllNotDefault()).thenReturn(scheduleList);

        // when
        List<Schedule> result = scheduleService.listNotDefault();

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.size()).isEqualTo(2);
        softly.assertThat(result.get(0).getId()).isEqualTo(schedule1.getId());
        softly.assertThat(result.get(0).getWeekday()).isEqualTo(schedule1.getWeekday());
        softly.assertThat(result.get(0).getDate()).isEqualTo(schedule1.getDate());
        softly.assertThat(result.get(0).getEquipmentId()).isEqualTo(schedule1.getEquipmentId());
        softly.assertThat(result.get(0).getStartTime()).isEqualTo(schedule1.getStartTime());
        softly.assertThat(result.get(0).getEndTime()).isEqualTo(schedule1.getEndTime());
        softly.assertThat(result.get(1).getId()).isEqualTo(schedule2.getId());
        softly.assertThat(result.get(1).getWeekday()).isEqualTo(schedule2.getWeekday());
        softly.assertThat(result.get(1).getDate()).isEqualTo(schedule2.getDate());
        softly.assertThat(result.get(1).getEquipmentId()).isEqualTo(schedule2.getEquipmentId());
        softly.assertThat(result.get(1).getStartTime()).isEqualTo(schedule2.getStartTime());
        softly.assertThat(result.get(1).getEndTime()).isEqualTo(schedule2.getEndTime());
        softly.assertAll();
    }

    @Test
    void testSave() {
        // given
        Schedule schedule = Schedule.builder()
                .id(1L)
                .weekday(1)
                .date(OffsetDateTime.now())
                .equipmentId(1L)
                .startTime(60L)
                .endTime(18 * 60L)
                .build();

        Schedule savedSchedule = Schedule.builder()
                .id(1L)
                .weekday(schedule.getWeekday())
                .date(schedule.getDate())
                .equipmentId(schedule.getEquipmentId())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .build();
        when(scheduleRepositoryMock.save(any(Schedule.class))).thenReturn(savedSchedule);

        // when
        Schedule actualSchedule = scheduleService.save(schedule);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualSchedule.getId()).isEqualTo(savedSchedule.getId());
        softly.assertThat(actualSchedule.getWeekday()).isEqualTo(savedSchedule.getWeekday());
        softly.assertThat(actualSchedule.getDate()).isEqualTo(savedSchedule.getDate());
        softly.assertThat(actualSchedule.getEquipmentId()).isEqualTo(savedSchedule.getEquipmentId());
        softly.assertThat(actualSchedule.getStartTime()).isEqualTo(savedSchedule.getStartTime());
        softly.assertThat(actualSchedule.getEndTime()).isEqualTo(savedSchedule.getEndTime());
        softly.assertAll();
    }

    @Test
    void testSaveWhenScheduleAlreadyExists() {
        // given
        Schedule schedule = Schedule.builder()
                .id(1L)
                .weekday(1)
                .date(OffsetDateTime.now())
                .equipmentId(1L)
                .startTime(60L)
                .endTime(18 * 60L)
                .build();

        Schedule savedSchedule = Schedule.builder()
                .id(1L)
                .weekday(schedule.getWeekday())
                .date(schedule.getDate())
                .equipmentId(schedule.getEquipmentId())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .build();

        Schedule existingSchedule = Schedule.builder()
                .id(2L)
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .build();
        when(scheduleRepositoryMock.save(any(Schedule.class))).thenReturn(savedSchedule);
        when(
                scheduleRepositoryMock.findByDateAndEquipmentIdAndWeekday(
                        eq(schedule.getDate().truncatedTo(ChronoUnit.DAYS)),
                        eq(schedule.getId()),
                        eq(schedule.getWeekday())
                )
        ).thenReturn(Optional.of(existingSchedule));

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
                .id(1L)
                .weekday(1)
                .date(OffsetDateTime.now())
                .equipmentId(1L)
                .startTime(60L)
                .endTime(18 * 60L)
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
                .id(1L)
                .weekday(1)
                .startTime(60L)
                .endTime(18 * 60L)
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
                .weekday(1)
                .date(OffsetDateTime.now())
                .equipmentId(1L)
                .startTime(60L)
                .endTime(18 * 60L)
                .build();

        Schedule updatedSchedule = Schedule.builder()
                .id(scheduleId)
                .weekday(2)
                .date(OffsetDateTime.now())
                .equipmentId(2L)
                .startTime(2 * 60L + 30)
                .endTime(17 * 60L + 15)
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
        softly.assertThat(actualSchedule.getWeekday()).isEqualTo(existingSchedule.getWeekday());
        softly.assertThat(actualSchedule.getDate()).isEqualTo(updatedSchedule.getDate().truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualSchedule.getEquipmentId()).isEqualTo(updatedSchedule.getEquipmentId());
        softly.assertThat(actualSchedule.getStartTime()).isEqualTo(updatedSchedule.getStartTime());
        softly.assertThat(actualSchedule.getEndTime()).isEqualTo(updatedSchedule.getEndTime());
        softly.assertAll();
    }

    @Test
    void testEditScheduleNotFound() {
        // given
        Long scheduleId = 1L;

        Schedule updatedSchedule = Schedule.builder()
                .id(1L)
                .weekday(1)
                .date(OffsetDateTime.now())
                .equipmentId(1L)
                .startTime(60L)
                .endTime(18 * 60L)
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
                .weekday(1)
                .startTime(60L)
                .endTime(18 * 60L)
                .build();

        Schedule updatedSchedule = Schedule.builder()
                .id(scheduleId)
                .weekday(1)
                .startTime(30L)
                .endTime(18 * 60L)
                .build();

        Schedule defaultSchedule = Schedule.builder()
                .id(2L)
                .weekday(1)
                .startTime(30L)
                .endTime(18 * 60L)
                .build();

        when(scheduleRepositoryMock.findById(scheduleId)).thenReturn(Optional.of(existingSchedule));
        when(scheduleRepositoryMock.findByDateAndEquipmentIdAndWeekday(
                null,
                null,
                1
        )).thenReturn(Optional.of(defaultSchedule));

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
                .weekday(1)
                .startTime(60L)
                .endTime(18 * 60L)
                .build();

        Schedule updatedSchedule = Schedule.builder()
                .id(scheduleId)
                .weekday(1)
                .startTime(30L)
                .endTime(18 * 60L)
                .date(OffsetDateTime.now())
                .build();

        when(scheduleRepositoryMock.findById(scheduleId)).thenReturn(Optional.of(existingSchedule));
        when(scheduleRepositoryMock.findByDateAndEquipmentIdAndWeekday(
                null,
                null,
                1
        )).thenReturn(Optional.of(existingSchedule));

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

    @Test
    void testEditWhenScheduleAlreadyExists() {
        // given
        Long scheduleId = 1L;

        Schedule schedule = Schedule.builder()
                .id(1L)
                .weekday(1)
                .date(OffsetDateTime.now())
                .equipmentId(1L)
                .startTime(60L)
                .endTime(18 * 60L)
                .build();

        Schedule updatedSchedule = Schedule.builder()
                .id(1L)
                .weekday(2)
                .date(OffsetDateTime.now())
                .equipmentId(2L)
                .startTime(2 * 60L + 30)
                .endTime(17 * 60L + 15)
                .build();

        Schedule existingSchedule = Schedule.builder()
                .id(2L)
                .weekday(2)
                .date(OffsetDateTime.now())
                .equipmentId(2L)
                .startTime(2 * 60L + 30)
                .endTime(17 * 60L + 15)
                .build();

        when(scheduleRepositoryMock.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(scheduleRepositoryMock.save(any(Schedule.class))).thenReturn(updatedSchedule);
        when(
                scheduleRepositoryMock.findByDateAndEquipmentIdAndWeekday(
                        eq(updatedSchedule.getDate().truncatedTo(ChronoUnit.DAYS)),
                        eq(updatedSchedule.getEquipmentId()),
                        eq(updatedSchedule.getWeekday())
                )
        ).thenReturn(Optional.of(existingSchedule));

        // when
        Throwable thrown = catchThrowable(() -> scheduleService.edit(scheduleId, updatedSchedule));

        // then
        String expectedMessage = messageSource.getMessage(
                SCHEDULE_CREATED_EXCEPTION_MESSAGE,
                new Object[]{},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ScheduleIsAlreadyCreatedException.class)
                .hasMessage(expectedMessage);
    }
}
