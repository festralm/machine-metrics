package ru.kpfu.machinemetrics.mapper;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.kpfu.machinemetrics.dto.ScheduleCreateDto;
import ru.kpfu.machinemetrics.dto.ScheduleDto;
import ru.kpfu.machinemetrics.model.Schedule;

import java.time.OffsetDateTime;
import java.util.List;

@SpringBootTest
public class ScheduleMapperTest {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Test
    public void testToSchedule() {
        // given
        ScheduleCreateDto dto = ScheduleCreateDto.builder()
                .weekday(1)
                .date(OffsetDateTime.now())
                .equipmentId(1L)
                .isWorkday(null)
                .startTime("01:00")
                .endTime("18:00")
                .build();

        // when
        Schedule schedule = scheduleMapper.toSchedule(dto);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(schedule.getWeekday()).isEqualTo(dto.getWeekday());
        softly.assertThat(schedule.getDate()).isEqualTo(dto.getDate());
        softly.assertThat(schedule.getEquipmentId()).isEqualTo(dto.getEquipmentId());
        softly.assertThat(schedule.getIsWorkday()).isEqualTo(dto.getIsWorkday());
        softly.assertThat(schedule.getStartTime()).isEqualTo(60);
        softly.assertThat(schedule.getEndTime()).isEqualTo(18 * 60);
        softly.assertAll();
    }

    @Test
    public void testToScheduleDto() {
        // given
        Schedule schedule = Schedule.builder()
                .id(1L)
                .weekday(2)
                .date(OffsetDateTime.now())
                .equipmentId(2L)
                .startTime(60)
                .endTime(18 * 60)
                .build();

        // when
        ScheduleDto dto = scheduleMapper.toScheduleDto(schedule);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(dto.getId()).isEqualTo(schedule.getId());
        softly.assertThat(dto.getWeekday()).isEqualTo(schedule.getWeekday());
        softly.assertThat(dto.getDate()).isEqualTo(schedule.getDate());
        softly.assertThat(dto.getEquipmentId()).isEqualTo(schedule.getEquipmentId());
        softly.assertThat(dto.getStartTime()).isEqualTo("01:00");
        softly.assertThat(dto.getEndTime()).isEqualTo("18:00");
        softly.assertAll();
    }

    @Test
    public void testToScheduleDtos() {
        // given
        Schedule schedule1 = Schedule.builder()
                .id(1L)
                .weekday(1)
                .date(OffsetDateTime.now())
                .equipmentId(1L)
                .startTime(60)
                .endTime(18 * 60)
                .build();
        Schedule schedule2 = Schedule.builder()
                .id(2L)
                .weekday(2)
                .date(OffsetDateTime.now())
                .equipmentId(2L)
                .startTime(2 * 60 + 30)
                .endTime(17 * 60 + 15)
                .build();
        List<Schedule> schedules = List.of(schedule1, schedule2);

        // when
        List<ScheduleDto> dtos = scheduleMapper.toScheduleDtos(schedules);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(dtos).hasSize(schedules.size());

        for (int i = 0; i < dtos.size(); i++) {
            ScheduleDto dto = dtos.get(i);
            Schedule schedule = schedules.get(i);

            softly.assertThat(dto.getId()).isEqualTo(schedule.getId());
            softly.assertThat(dto.getWeekday()).isEqualTo(schedule.getWeekday());
            softly.assertThat(dto.getDate()).isEqualTo(schedule.getDate());
            softly.assertThat(dto.getEquipmentId()).isEqualTo(schedule.getEquipmentId());
        }
        softly.assertThat(dtos.get(0).getStartTime()).isEqualTo("01:00");
        softly.assertThat(dtos.get(0).getEndTime()).isEqualTo("18:00");
        softly.assertThat(dtos.get(1).getStartTime()).isEqualTo("02:30");
        softly.assertThat(dtos.get(1).getEndTime()).isEqualTo("17:15");

        softly.assertAll();
    }
}
