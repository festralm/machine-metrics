package ru.kpfu.machinemetrics.service;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import ru.kpfu.machinemetrics.dto.StatisticsDto;
import ru.kpfu.machinemetrics.model.EquipmentData;
import ru.kpfu.machinemetrics.model.Schedule;
import ru.kpfu.machinemetrics.repository.EquipmentDataRepository;
import ru.kpfu.machinemetrics.repository.ScheduleRepository;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource("classpath:application.yml")
public class EquipmentDataServiceTest {

    @Autowired
    private EquipmentDataService equipmentDataService;

    @MockBean
    private EquipmentDataRepository equipmentDataRepositoryMock;

    @MockBean
    private ScheduleRepository scheduleRepositoryMock;

    // начало, конец пересекаются с графиком
    @Test
    void testGetData1() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.withHour(16).withMinute(30).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(20).withMinute(0).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStart)
                .enabled(true)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStop)
                .enabled(false)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2);

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        mockOnDate(givenId, givenStart);

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(1);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo((long) (3.5 * 60));
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo((long) (1.5 * 60));
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(2 * 60);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(0);

        softly.assertThat(actualDto.getUpSchedulePercent()).isEqualTo(100.0);
        softly.assertThat(actualDto.getDownSchedulePercent()).isEqualTo(0.0);

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment1.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment1.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment1.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    @Test
    void testGetData2() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.withHour(16).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(20).withMinute(30).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStart)
                .enabled(false)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStop)
                .enabled(true)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2);

        List<Schedule> scheduleList = List.of(
                Schedule.builder()
                        .id(1L)
                        .date(givenStart.truncatedTo(ChronoUnit.DAYS))
                        .equipmentId(givenId)
                        .startTime(10 * 60)
                        .endTime(18 * 60)
                        .build()
        );
        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        when(scheduleRepositoryMock.findAllByWeekdayAndEquipmentId(
                eq(givenStart.getDayOfWeek().getValue()),
                eq(givenId)
        )).thenReturn(scheduleList);

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(1);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo((long) (4.5 * 60));
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(2 * 60);

        softly.assertThat(actualDto.getUpSchedulePercent()).isEqualTo(0.0);
        softly.assertThat(actualDto.getDownSchedulePercent()).isEqualTo(100.0);

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment1.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment1.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment1.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(true);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(true);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    // 0 записей
    @Test
    void testGetData3() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.minusDays(2).withHour(16).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(20).withMinute(0).withSecond(0).withNano(0);

        List<EquipmentData> givenList = List.of();

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        mockOnDate(givenId, givenStart);
        mockOnDate(givenId, givenStart.plusDays(1));
        mockOnDate(givenId, givenStart.plusDays(2));

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(3);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.plusDays(1).truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.plusDays(2).truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo(52 * 60L);
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(18 * 60L);

        softly.assertThat(actualDto.getUpSchedulePercent()).isEqualTo(0.0);
        softly.assertThat(actualDto.getDownSchedulePercent()).isEqualTo(100.0);

        softly.assertThat(actualDto.getEquipmentData()).hasSize(0);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    // начало и конец вне графика
    @Test
    void testGetData4() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.withHour(10).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(20).withMinute(30).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStart)
                .enabled(true)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStop)
                .enabled(false)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2);

        List<Schedule> scheduleList = List.of(
                Schedule.builder()
                        .id(1L)
                        .date(givenStart.truncatedTo(ChronoUnit.DAYS))
                        .equipmentId(givenId)
                        .startTime(12 * 60)
                        .endTime(18 * 60)
                        .build()
        );
        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        when(scheduleRepositoryMock.findAllByDateAndEquipmentIdOrderByDateAscWeekdayAsc(
                eq(givenStart.truncatedTo(ChronoUnit.DAYS)),
                isNull()
        )).thenReturn(scheduleList);

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(1);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo((long) (10.5 * 60));
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo(6 * 60);
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo((long) (4.5 * 60));
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(0);

        softly.assertThat(actualDto.getUpSchedulePercent()).isEqualTo(100.0);
        softly.assertThat(actualDto.getDownSchedulePercent()).isEqualTo(0.0);

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment1.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment1.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment1.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(true);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    @Test
    void testGetData5() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.withHour(10).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(20).withMinute(30).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStart)
                .enabled(false)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStop)
                .enabled(true)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2);

        List<Schedule> scheduleList = List.of(
                Schedule.builder()
                        .id(1L)
                        .date(givenStart.truncatedTo(ChronoUnit.DAYS))
                        .equipmentId(givenId)
                        .startTime(12 * 60)
                        .endTime(18 * 60)
                        .build()
        );
        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        when(scheduleRepositoryMock.findAllByWeekdayAndEquipmentId(
                eq(givenStart.getDayOfWeek().getValue()),
                isNull()
        )).thenReturn(scheduleList);

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(1);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo(0L);
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo(0L);
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(0L);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo((long) (10.5 * 60));
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(6 * 60);

        softly.assertThat(actualDto.getUpSchedulePercent()).isEqualTo(0.0);
        softly.assertThat(actualDto.getDownSchedulePercent()).isEqualTo(100.0);

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment1.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment1.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment1.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(true);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    // начало и конец внутри графика
    @Test
    void testGetData6() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.withHour(12).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(18).withMinute(30).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStart)
                .enabled(true)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStop)
                .enabled(false)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2);

        List<Schedule> scheduleList = List.of(
                Schedule.builder()
                        .id(1L)
                        .equipmentId(givenId)
                        .startTime(10 * 60)
                        .endTime(20 * 60)
                        .build()
        );
        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        when(scheduleRepositoryMock.findAllByDateAndEquipmentIdOrderByDateAscWeekdayAsc(
                isNull(),
                isNull()
        )).thenReturn(scheduleList);

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(1);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo((long) (6.5 * 60));
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo((long) (6.5 * 60));
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(0);

        softly.assertThat(actualDto.getUpSchedulePercent()).isEqualTo(100.0);
        softly.assertThat(actualDto.getDownSchedulePercent()).isEqualTo(0.0);

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment1.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment1.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment1.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(true);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    @Test
    void testGetData7() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.withHour(12).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(18).withMinute(30).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStart)
                .enabled(false)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStop)
                .enabled(true)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2);

        List<Schedule> scheduleList = List.of(
                Schedule.builder()
                        .id(1L)
                        .date(givenStart.truncatedTo(ChronoUnit.DAYS))
                        .equipmentId(givenId)
                        .startTime(10 * 60)
                        .endTime(20 * 60)
                        .build()
        );
        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        when(scheduleRepositoryMock.findAllByDateAndEquipmentIdOrderByDateAscWeekdayAsc(
                eq(givenStart.truncatedTo(ChronoUnit.DAYS)),
                isNull()
        )).thenReturn(scheduleList);

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(1);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo(0L);
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo(0L);
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(0L);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo((long) (6.5 * 60));
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo((long) (6.5 * 60));

        softly.assertThat(actualDto.getUpSchedulePercent()).isEqualTo(0.0);
        softly.assertThat(actualDto.getDownSchedulePercent()).isEqualTo(100.0);

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment1.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment1.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment1.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(true);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    // первый день пустой
    @Test
    void testGetData8() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.minusDays(1).withHour(12).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(18).withMinute(30).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStart.plusDays(1))
                .enabled(true)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStop)
                .enabled(false)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2);

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        when(scheduleRepositoryMock.findAllByWeekdayAndEquipmentId(
                eq(givenStart.getDayOfWeek().getValue()),
                isNull()
        )).thenReturn(List.of(
                Schedule.builder()
                        .id(1L)
                        .date(givenStart.truncatedTo(ChronoUnit.DAYS))
                        .equipmentId(givenId)
                        .startTime(10 * 60)
                        .endTime(20 * 60)
                        .build()
        ));
        when(scheduleRepositoryMock.findAllByWeekdayAndEquipmentId(
                eq(givenStop.getDayOfWeek().getValue()),
                isNull()
        )).thenReturn(List.of(
                Schedule.builder()
                        .id(1L)
                        .date(givenStop.truncatedTo(ChronoUnit.DAYS))
                        .equipmentId(givenId)
                        .startTime(10 * 60)
                        .endTime(20 * 60)
                        .build()
        ));

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(2);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStop.truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo((long) (6.5 * 60));
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo((long) (6.5 * 60));
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo(24 * 60);
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(10 * 60);

        softly.assertThat(actualDto.getUpSchedulePercent()).isCloseTo(39.3, Offset.offset(0.1));
        softly.assertThat(actualDto.getDownSchedulePercent()).isCloseTo(60.6, Offset.offset(0.1));

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment1.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment1.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment1.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(true);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    @Test
    void testGetData9() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.withHour(12).minusDays(1).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(18).withMinute(30).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStart.plusDays(1))
                .enabled(false)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStop)
                .enabled(true)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2);

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        when(scheduleRepositoryMock.findAllByWeekdayAndEquipmentId(
                eq(givenStart.getDayOfWeek().getValue()),
                isNull()
        )).thenReturn(List.of(
                Schedule.builder()
                        .id(1L)
                        .date(givenStart.truncatedTo(ChronoUnit.DAYS))
                        .equipmentId(givenId)
                        .startTime(10 * 60)
                        .endTime(20 * 60)
                        .build()
        ));
        when(scheduleRepositoryMock.findAllByWeekdayAndEquipmentId(
                eq(givenStop.getDayOfWeek().getValue()),
                isNull()
        )).thenReturn(List.of(
                Schedule.builder()
                        .id(1L)
                        .date(givenStop.truncatedTo(ChronoUnit.DAYS))
                        .equipmentId(givenId)
                        .startTime(10 * 60)
                        .endTime(20 * 60)
                        .build()
        ));

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(2);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStop.truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo(0L);
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo(0L);
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(0L);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo((long) (30.5 * 60));
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo((long) (16.5 * 60));

        softly.assertThat(actualDto.getUpSchedulePercent()).isEqualTo(0.0);
        softly.assertThat(actualDto.getDownSchedulePercent()).isEqualTo(100.0);

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment1.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment1.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment1.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(true);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    // второй день пустой
    @Test
    void testGetData10() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.minusDays(1).withHour(12).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(18).withMinute(30).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStart)
                .enabled(true)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStop.minusDays(1))
                .enabled(false)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2);

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        when(scheduleRepositoryMock.findAllByWeekdayAndEquipmentId(
                eq(givenStart.getDayOfWeek().getValue()),
                isNull()
        )).thenReturn(List.of(
                Schedule.builder()
                        .id(1L)
                        .date(givenStart.truncatedTo(ChronoUnit.DAYS))
                        .equipmentId(givenId)
                        .startTime(10 * 60)
                        .endTime(20 * 60)
                        .build()
        ));
        when(scheduleRepositoryMock.findAllByWeekdayAndEquipmentId(
                eq(givenStop.getDayOfWeek().getValue()),
                isNull()
        )).thenReturn(List.of(
                Schedule.builder()
                        .id(1L)
                        .date(givenStop.truncatedTo(ChronoUnit.DAYS))
                        .equipmentId(givenId)
                        .startTime(10 * 60)
                        .endTime(20 * 60)
                        .build()
        ));

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(2);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStop.truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo((long) (6.5 * 60));
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo((long) (6.5 * 60));
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo((long) (0));
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo((long) (24 * 60));
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(10 * 60);

        softly.assertThat(actualDto.getUpSchedulePercent()).isCloseTo(39.3, Offset.offset(0.1));
        softly.assertThat(actualDto.getDownSchedulePercent()).isCloseTo(60.6, Offset.offset(0.1));

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment1.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment1.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment1.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(true);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    @Test
    void testGetData11() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.minusDays(1).withHour(12).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(18).withMinute(30).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStart)
                .enabled(false)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStop.minusDays(1))
                .enabled(true)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2);

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        when(scheduleRepositoryMock.findAllByWeekdayAndEquipmentId(
                eq(givenStart.getDayOfWeek().getValue()),
                isNull()
        )).thenReturn(List.of(
                Schedule.builder()
                        .id(1L)
                        .date(givenStart.truncatedTo(ChronoUnit.DAYS))
                        .equipmentId(givenId)
                        .startTime(10 * 60)
                        .endTime(20 * 60)
                        .build()
        ));
        when(scheduleRepositoryMock.findAllByWeekdayAndEquipmentId(
                eq(givenStop.getDayOfWeek().getValue()),
                isNull()
        )).thenReturn(List.of(
                Schedule.builder()
                        .id(1L)
                        .date(givenStop.truncatedTo(ChronoUnit.DAYS))
                        .equipmentId(givenId)
                        .startTime(10 * 60)
                        .endTime(20 * 60)
                        .build()
        ));

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(2);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStop.truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo((long) (24 * 60));
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo((long) (10 * 60));
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo((long) (14 * 60));
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo((long) (6.5 * 60));
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo((long) (6.5 * 60));

        softly.assertThat(actualDto.getUpSchedulePercent()).isCloseTo(60.6, Offset.offset(0.1));
        softly.assertThat(actualDto.getDownSchedulePercent()).isCloseTo(39.3, Offset.offset(0.1));

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment1.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment1.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment1.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(true);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    // начало и конец внутри периода
    @Test
    void testGetData12() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.withHour(16).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(20).withMinute(0).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStart.withHour(17))
                .enabled(true)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStop.withHour(19))
                .enabled(false)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2);

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        mockOnDate(givenId, givenStart);

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(1);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo((long) (2 * 60));
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo((long) (60));
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(60);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo(2 * 60);
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(60);

        softly.assertThat(actualDto.getUpSchedulePercent()).isEqualTo(50.0);
        softly.assertThat(actualDto.getDownSchedulePercent()).isEqualTo(50.0);

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment1.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment1.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment1.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    @Test
    void testGetData13() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.withHour(16).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(20).withMinute(0).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStart.withHour(17))
                .enabled(false)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStop.withHour(19))
                .enabled(true)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2);

        List<Schedule> scheduleList = List.of(
                Schedule.builder()
                        .id(1L)
                        .date(givenStart.truncatedTo(ChronoUnit.DAYS))
                        .equipmentId(givenId)
                        .startTime(10 * 60)
                        .endTime(18 * 60)
                        .build()
        );
        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        when(scheduleRepositoryMock.findAllByWeekdayAndEquipmentId(
                eq(givenStart.getDayOfWeek().getValue()),
                eq(givenId)
        )).thenReturn(scheduleList);

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(1);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo(60);
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(60);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo((long) (3 * 60));
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(2 * 60);

        softly.assertThat(actualDto.getUpSchedulePercent()).isEqualTo(0.0);
        softly.assertThat(actualDto.getDownSchedulePercent()).isEqualTo(100.0);

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment1.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment1.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment1.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(true);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(true);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    // 2 дня
    @Test
    void testGetData14() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.minusDays(1).withHour(16).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(20).withMinute(0).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStart)
                .enabled(true)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStop)
                .enabled(false)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2);

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        mockOnDate(givenId, givenStart);
        mockOnDate(givenId, givenStop);

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(2);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStop.truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo((long) (28 * 60));
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo((long) (10 * 60));
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(18 * 60);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(0);

        softly.assertThat(actualDto.getUpSchedulePercent()).isEqualTo(100.0);
        softly.assertThat(actualDto.getDownSchedulePercent()).isEqualTo(0.0);

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment1.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment1.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment1.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    @Test
    void testGetData15() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.minusDays(1).withHour(16).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(20).withMinute(0).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStart)
                .enabled(false)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStop)
                .enabled(true)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2);

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        when(scheduleRepositoryMock.findAllByWeekdayAndEquipmentId(
                eq(givenStart.getDayOfWeek().getValue()),
                eq(givenId)
        )).thenReturn(List.of(
                Schedule.builder()
                        .id(1L)
                        .date(givenStart.truncatedTo(ChronoUnit.DAYS))
                        .equipmentId(givenId)
                        .startTime(10 * 60)
                        .endTime(18 * 60)
                        .build()
        ));
        when(scheduleRepositoryMock.findAllByWeekdayAndEquipmentId(
                eq(givenStop.getDayOfWeek().getValue()),
                eq(givenId)
        )).thenReturn(List.of(
                Schedule.builder()
                        .id(1L)
                        .date(givenStop.truncatedTo(ChronoUnit.DAYS))
                        .equipmentId(givenId)
                        .startTime(10 * 60)
                        .endTime(18 * 60)
                        .build()
        ));

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(2);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStop.truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo((long) (28 * 60));
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(10 * 60);

        softly.assertThat(actualDto.getUpSchedulePercent()).isEqualTo(0.0);
        softly.assertThat(actualDto.getDownSchedulePercent()).isEqualTo(100.0);

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment1.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment1.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment1.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(true);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(true);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    // начало вне периода
    @Test
    void testGetData16() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.withHour(16).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(20).withMinute(0).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStart.withHour(15))
                .enabled(true)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStop.withHour(17))
                .enabled(false)
                .build();
        final EquipmentData equipment3 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStart.withHour(19))
                .enabled(true)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2, equipment3);

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        mockOnDate(givenId, givenStart);

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(1);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo((long) (2 * 60));
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo((long) (60));
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(60);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo(2 * 60);
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(60);

        softly.assertThat(actualDto.getUpSchedulePercent()).isEqualTo(50.0);
        softly.assertThat(actualDto.getDownSchedulePercent()).isEqualTo(50.0);

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(true);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment3.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment3.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment3.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(true);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    @Test
    void testGetData17() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.withHour(16).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(20).withMinute(0).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStart.withHour(15))
                .enabled(false)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStop.withHour(17))
                .enabled(false)
                .build();
        final EquipmentData equipment3 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStart.withHour(19))
                .enabled(true)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2, equipment3);

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        mockOnDate(givenId, givenStart);

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(1);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo((long) (60));
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo((long) 0);
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(60);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo(3 * 60);
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(2 * 60);

        softly.assertThat(actualDto.getUpSchedulePercent()).isEqualTo(0.0);
        softly.assertThat(actualDto.getDownSchedulePercent()).isEqualTo(100.0);

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(true);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment3.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment3.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment3.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(true);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    @Test
    void testGetData18() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.withHour(16).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(20).withMinute(0).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStart.withHour(15))
                .enabled(true)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStop.withHour(17))
                .enabled(true)
                .build();
        final EquipmentData equipment3 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStop.withHour(19))
                .enabled(false)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2, equipment3);

        List<Schedule> scheduleList = List.of(
                Schedule.builder()
                        .id(1L)
                        .date(givenStart.truncatedTo(ChronoUnit.DAYS))
                        .equipmentId(givenId)
                        .startTime(10 * 60)
                        .endTime(18 * 60)
                        .build()
        );
        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        when(scheduleRepositoryMock.findAllByWeekdayAndEquipmentId(
                eq(givenStart.getDayOfWeek().getValue()),
                eq(givenId)
        )).thenReturn(scheduleList);

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(1);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo(3 * 60);
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo(2 * 60);
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(60);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo((long) 60);
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(0);

        softly.assertThat(actualDto.getUpSchedulePercent()).isEqualTo(100.0);
        softly.assertThat(actualDto.getDownSchedulePercent()).isEqualTo(0.0);

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment3.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment3.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment3.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    @Test
    void testGetData19() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.withHour(16).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(20).withMinute(0).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStart.withHour(15))
                .enabled(false)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStop.withHour(17))
                .enabled(true)
                .build();
        final EquipmentData equipment3 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStop.withHour(19))
                .enabled(false)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2, equipment3);

        List<Schedule> scheduleList = List.of(
                Schedule.builder()
                        .id(1L)
                        .equipmentId(givenId)
                        .startTime(10 * 60)
                        .endTime(18 * 60)
                        .build()
        );
        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        when(scheduleRepositoryMock.findAllByDateAndEquipmentIdOrderByDateAscWeekdayAsc(
                isNull(),
                eq(givenId)
        )).thenReturn(scheduleList);

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(1);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo(2 * 60);
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo(60);
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(60);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo((long) (2 * 60));
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(60);

        softly.assertThat(actualDto.getUpSchedulePercent()).isEqualTo(50.0);
        softly.assertThat(actualDto.getDownSchedulePercent()).isEqualTo(50.0);

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment3.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment3.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment3.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    // есть только начало вне периода
    @Test
    void testGetData20() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.withHour(16).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(20).withMinute(0).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStart.withHour(15))
                .enabled(true)
                .build();

        List<EquipmentData> givenList = List.of(equipment1);

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        mockOnDate(givenId, givenStart);

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(1);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo((long) (4 * 60));
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo((long) (2 * 60));
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(2 * 60);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(0);

        softly.assertThat(actualDto.getUpSchedulePercent()).isEqualTo(100.0);
        softly.assertThat(actualDto.getDownSchedulePercent()).isEqualTo(0.0);

        softly.assertThat(actualDto.getEquipmentData()).hasSize(0);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    @Test
    void testGetData21() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.withHour(16).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(20).withMinute(0).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStart.withHour(15))
                .enabled(false)
                .build();

        List<EquipmentData> givenList = List.of(equipment1);

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        mockOnDate(givenId, givenStart);

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(1);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo((long) 0);
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo((long) 0);
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo(4 * 60);
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(2 * 60);

        softly.assertThat(actualDto.getUpSchedulePercent()).isEqualTo(0.0);
        softly.assertThat(actualDto.getDownSchedulePercent()).isEqualTo(100.0);

        softly.assertThat(actualDto.getEquipmentData()).hasSize(0);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    // on - off on
    @Test
    void testGetData22() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.withHour(16).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.plusDays(2).withHour(20).withMinute(0).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStart)
                .enabled(true)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStop.minusDays(1).withHour(18))
                .enabled(false)
                .build();
        final EquipmentData equipment3 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStop)
                .enabled(true)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2, equipment3);

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        mockOnDate(givenId, equipment1.getTime());
        mockOnDate(givenId, equipment2.getTime());
        mockOnDate(givenId, equipment3.getTime());

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(3);
        softly.assertThat(actualDto.getSchedules()).containsKey(equipment1.getTime().truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(equipment2.getTime().truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(equipment3.getTime().truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo((long) (26 * 60));
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo((long) (10 * 60));
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(16 * 60);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo(26 * 60);
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(8 * 60);

        softly.assertThat(actualDto.getUpSchedulePercent()).isCloseTo(55.5, Offset.offset(0.1));
        softly.assertThat(actualDto.getDownSchedulePercent()).isCloseTo(44.4, Offset.offset(0.1));

        softly.assertThat(actualDto.getEquipmentData()).hasSize(3);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment1.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment1.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment1.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(true);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(2).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(2).getEnabled()).isEqualTo(equipment3.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(2).getU()).isEqualTo(equipment3.getU());
        softly.assertThat(actualDto.getEquipmentData().get(2).getTime()).isEqualTo(equipment3.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(2).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(2).getEnabledDuringPassiveTime()).isEqualTo(true);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }


    // off - on - off
    @Test
    void testGetData23() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.withHour(16).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.plusDays(2).withHour(20).withMinute(0).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStart)
                .enabled(false)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStop.minusDays(1).withHour(18))
                .enabled(true)
                .build();
        final EquipmentData equipment3 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStop)
                .enabled(false)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2, equipment3);

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        mockOnDate(givenId, equipment1.getTime());
        mockOnDate(givenId, equipment2.getTime());
        mockOnDate(givenId, equipment3.getTime());

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(3);
        softly.assertThat(actualDto.getSchedules()).containsKey(equipment1.getTime().truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(equipment2.getTime().truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(equipment3.getTime().truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo((long) (26 * 60));
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo((long) (8 * 60));
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(18 * 60);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo(26 * 60);
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(10 * 60);

        softly.assertThat(actualDto.getUpSchedulePercent()).isCloseTo(44.4, Offset.offset(0.1));
        softly.assertThat(actualDto.getDownSchedulePercent()).isCloseTo(55.5, Offset.offset(0.1));

        softly.assertThat(actualDto.getEquipmentData()).hasSize(3);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment1.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment1.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment1.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(true);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(2).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(2).getEnabled()).isEqualTo(equipment3.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(2).getU()).isEqualTo(equipment3.getU());
        softly.assertThat(actualDto.getEquipmentData().get(2).getTime()).isEqualTo(equipment3.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(2).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(2).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    // on empty ...
    @Test
    void testGetData24() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.withHour(16).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.plusDays(4).withHour(20).withMinute(0).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStart)
                .enabled(true)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStop)
                .enabled(true)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2);

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        mockOnDate(givenId, equipment1.getTime());
        mockOnDate(givenId, equipment1.getTime().plusDays(1));
        mockOnDate(givenId, equipment1.getTime().plusDays(2));
        mockOnDate(givenId, equipment1.getTime().plusDays(3));
        mockOnDate(givenId, equipment1.getTime().plusDays(4));

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(5);
        softly.assertThat(actualDto.getSchedules()).containsKey(equipment1.getTime().truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(equipment1.getTime().plusDays(1).truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(equipment1.getTime().plusDays(2).truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(equipment1.getTime().plusDays(3).truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(equipment1.getTime().plusDays(4).truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo((long) (100 * 60));
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo((long) (34 * 60));
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(66 * 60);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(0);

        softly.assertThat(actualDto.getUpSchedulePercent()).isCloseTo(100.0, Offset.offset(0.1));
        softly.assertThat(actualDto.getDownSchedulePercent()).isCloseTo(0, Offset.offset(0.1));

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment1.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment1.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment1.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(true);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    // off empty ...
    @Test
    void testGetData25() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.withHour(16).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.plusDays(4).withHour(20).withMinute(0).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStart)
                .enabled(false)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStop)
                .enabled(true)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2);

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        mockOnDate(givenId, equipment1.getTime());
        mockOnDate(givenId, equipment1.getTime().plusDays(1));
        mockOnDate(givenId, equipment1.getTime().plusDays(2));
        mockOnDate(givenId, equipment1.getTime().plusDays(3));
        mockOnDate(givenId, equipment1.getTime().plusDays(4));

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(5);
        softly.assertThat(actualDto.getSchedules()).containsKey(equipment1.getTime().truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(equipment1.getTime().plusDays(1).truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(equipment1.getTime().plusDays(2).truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(equipment1.getTime().plusDays(3).truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(equipment1.getTime().plusDays(4).truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo((long) 0);
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo((long) 0);
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo(100 * 60);
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(34 * 60);

        softly.assertThat(actualDto.getUpSchedulePercent()).isCloseTo(0.0, Offset.offset(0.1));
        softly.assertThat(actualDto.getDownSchedulePercent()).isCloseTo(100.0, Offset.offset(0.1));

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment1.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment1.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment1.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(true);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(true);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    // empty ... on
    @Test
    void testGetData26() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.minusDays(2).withHour(16).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(20).withMinute(0).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStart.plusDays(2).withHour(17))
                .enabled(true)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStop.withHour(19))
                .enabled(false)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2);

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        mockOnDate(givenId, givenStart);
        mockOnDate(givenId, givenStart.plusDays(1));
        mockOnDate(givenId, givenStart.plusDays(2));

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(3);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.plusDays(1).truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.plusDays(2).truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo((long) (2 * 60));
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo((long) (60));
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(60);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo(50 * 60);
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(17 * 60);

        softly.assertThat(actualDto.getUpSchedulePercent()).isCloseTo(5.5, Offset.offset(0.1));
        softly.assertThat(actualDto.getDownSchedulePercent()).isCloseTo(94.4, Offset.offset(0.1));

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment1.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment1.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment1.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    @Test
    void testGetData27() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.minusDays(2).withHour(16).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(20).withMinute(0).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStart.plusDays(2).withHour(17))
                .enabled(false)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStop.withHour(19))
                .enabled(true)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2);

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        mockOnDate(givenId, givenStart);
        mockOnDate(givenId, givenStart.plusDays(1));
        mockOnDate(givenId, givenStart.plusDays(2));

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(3);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.plusDays(1).truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.plusDays(2).truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo(60);
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(60);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo((long) (51 * 60));
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(18 * 60);

        softly.assertThat(actualDto.getUpSchedulePercent()).isEqualTo(0.0);
        softly.assertThat(actualDto.getDownSchedulePercent()).isEqualTo(100.0);

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment1.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment1.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment1.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(true);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(true);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    // on ... empty
    @Test
    void testGetData28() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.withHour(16).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.plusDays(2).withHour(20).withMinute(0).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStart.withHour(17))
                .enabled(true)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStart.withHour(19))
                .enabled(false)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2);

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        mockOnDate(givenId, givenStart);
        mockOnDate(givenId, givenStart.plusDays(1));
        mockOnDate(givenId, givenStart.plusDays(2));

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(3);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.plusDays(1).truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.plusDays(2).truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo((long) (2 * 60));
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo((long) (60));
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(60);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo(50 * 60);
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(17 * 60);

        softly.assertThat(actualDto.getUpSchedulePercent()).isCloseTo(5.5, Offset.offset(0.1));
        softly.assertThat(actualDto.getDownSchedulePercent()).isCloseTo(94.4, Offset.offset(0.1));

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment1.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment1.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment1.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    @Test
    void testGetData29() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.withHour(16).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.plusDays(2).withHour(20).withMinute(0).withSecond(0).withNano(0);

        final EquipmentData equipment1 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(30d)
                .time(givenStart.withHour(17))
                .enabled(false)
                .build();
        final EquipmentData equipment2 = EquipmentData.builder()
                .equipmentId(givenId)
                .u(10d)
                .time(givenStart.withHour(19))
                .enabled(true)
                .build();

        List<EquipmentData> givenList = List.of(equipment1, equipment2);

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        mockOnDate(givenId, givenStart);
        mockOnDate(givenId, givenStart.plusDays(1));
        mockOnDate(givenId, givenStart.plusDays(2));

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(3);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.plusDays(1).truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.plusDays(2).truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo(49 * 60);
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo(16 * 60);
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(33 * 60);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo((long) (3 * 60));
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(2 * 60);

        softly.assertThat(actualDto.getUpSchedulePercent()).isCloseTo(88.8, Offset.offset(0.1));
        softly.assertThat(actualDto.getDownSchedulePercent()).isCloseTo(11.1, Offset.offset(0.1));

        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(equipment1.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(0).getU()).isEqualTo(equipment1.getU());
        softly.assertThat(actualDto.getEquipmentData().get(0).getTime()).isEqualTo(equipment1.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(0).getDisabledDuringActiveTime()).isEqualTo(true);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabledDuringPassiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(equipment2.getEnabled());
        softly.assertThat(actualDto.getEquipmentData().get(1).getU()).isEqualTo(equipment2.getU());
        softly.assertThat(actualDto.getEquipmentData().get(1).getTime()).isEqualTo(equipment2.getTime());
        softly.assertThat(actualDto.getEquipmentData().get(1).getDisabledDuringActiveTime()).isEqualTo(false);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabledDuringPassiveTime()).isEqualTo(true);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    // 0 записей за 2 дня
    @Test
    void testGetData30() {
        // given
        Long givenId = 1L;
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime givenStart = now.minusDays(1).withHour(16).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime givenStop = now.withHour(20).withMinute(0).withSecond(0).withNano(0);

        List<EquipmentData> givenList = List.of();

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);
        mockOnDate(givenId, givenStart);
        mockOnDate(givenId, givenStart.plusDays(1));

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);

        softly.assertThat(actualDto.getSchedules()).hasSize(2);
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.truncatedTo(ChronoUnit.DAYS));
        softly.assertThat(actualDto.getSchedules()).containsKey(givenStart.plusDays(1).truncatedTo(ChronoUnit.DAYS));

        softly.assertThat(actualDto.getUpMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getUpScheduleMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getUpNotScheduleMinutes()).isEqualTo(0);
        softly.assertThat(actualDto.getDownMinutes()).isEqualTo(28 * 60L);
        softly.assertThat(actualDto.getDownScheduleMinutes()).isEqualTo(10 * 60L);

        softly.assertThat(actualDto.getUpSchedulePercent()).isEqualTo(0.0);
        softly.assertThat(actualDto.getDownSchedulePercent()).isEqualTo(100.0);

        softly.assertThat(actualDto.getEquipmentData()).hasSize(0);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(any(), any(), any());
    }

    private void mockOnDate(Long givenId, OffsetDateTime date) {
        when(scheduleRepositoryMock.findAllByDateAndEquipmentIdOrderByDateAscWeekdayAsc(
                eq(date.truncatedTo(ChronoUnit.DAYS)),
                eq(givenId)
        )).thenReturn(List.of(
                Schedule.builder()
                        .id(1L)
                        .date(date.truncatedTo(ChronoUnit.DAYS))
                        .equipmentId(givenId)
                        .startTime(10 * 60)
                        .endTime(18 * 60)
                        .build()
        ));
    }

    @Test
    void testDelete() {
        // given
        Long givenId = 1L;

        // when
        try {
            equipmentDataService.delete(givenId);
        } catch (Exception e) {
            Assertions.fail("Exception should not have been thrown");
        }
    }
}
