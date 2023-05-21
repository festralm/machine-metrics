package ru.kpfu.machinemetrics.mapper;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.kpfu.machinemetrics.dto.ScheduleCreateDto;
import ru.kpfu.machinemetrics.dto.ScheduleDto;
import ru.kpfu.machinemetrics.model.Schedule;

import java.time.Instant;
import java.util.List;

@SpringBootTest
public class ScheduleMapperTest {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Test
    public void testToSchedule() {
        // given
        ScheduleCreateDto dto = ScheduleCreateDto.builder()
                .startTime("11:00")
                .endTime("18:00")
                .date(Instant.now())
                .build();

        // when
        Schedule schedule = scheduleMapper.toSchedule(dto);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(schedule.getStartTime()).isEqualTo(dto.getStartTime());
        softly.assertThat(schedule.getEndTime()).isEqualTo(dto.getEndTime());
        softly.assertThat(schedule.getDate()).isEqualTo(dto.getDate());
        softly.assertAll();
    }

    @Test
    public void testToScheduleDto() {
        // given
        Schedule schedule = Schedule.builder()
                .id(1L)
                .startTime("11:00")
                .endTime("18:00")
                .date(Instant.now())
                .build();

        // when
        ScheduleDto dto = scheduleMapper.toScheduleDto(schedule);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(dto.getId()).isEqualTo(schedule.getId());
        softly.assertThat(dto.getStartTime()).isEqualTo(schedule.getStartTime());
        softly.assertThat(dto.getEndTime()).isEqualTo(schedule.getEndTime());
        softly.assertThat(dto.getDate()).isEqualTo(schedule.getDate());
        softly.assertAll();
    }

    @Test
    public void testToScheduleItemDtos() {
        // given
        Schedule schedule1 = Schedule.builder()
                .id(1L)
                .startTime("11:00")
                .endTime("18:00")
                .date(Instant.now())
                .build();
        Schedule schedule2 = Schedule.builder()
                .id(2L)
                .startTime("12:00")
                .endTime("19:00")
                .date(Instant.now())
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
            softly.assertThat(dto.getStartTime()).isEqualTo(schedule.getStartTime());
            softly.assertThat(dto.getEndTime()).isEqualTo(schedule.getEndTime());
            softly.assertThat(dto.getDate()).isEqualTo(schedule.getDate());
        }

        softly.assertAll();
    }
}
