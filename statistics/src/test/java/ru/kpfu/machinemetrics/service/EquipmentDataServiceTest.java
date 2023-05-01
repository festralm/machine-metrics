package ru.kpfu.machinemetrics.service;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.kpfu.machinemetrics.StatisticsDto;
import ru.kpfu.machinemetrics.model.EquipmentData;
import ru.kpfu.machinemetrics.repository.EquipmentDataRepository;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class EquipmentDataServiceTest {

    @Autowired
    private EquipmentDataService equipmentDataService;

    @MockBean
    private EquipmentDataRepository equipmentDataRepositoryMock;

    @Test
    void testGetData() {
        // given
        Long givenId = 1L;
        final Instant now = Instant.now();
        Instant givenStart = now.minusSeconds(60 * 60 * 4);
        Instant givenStop = now;

        List<EquipmentData> givenList = List.of(
                EquipmentData.builder().equipmentId(givenId).time(givenStart).enabled(true).build(),
                EquipmentData.builder().equipmentId(givenId).time(givenStop).enabled(false).build()
        );
        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);

        // when
        StatisticsDto actualDto = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
        softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);
        softly.assertThat(actualDto.getUpHours()).isEqualTo(4);
        softly.assertThat(actualDto.getDownHours()).isEqualTo(0);
        softly.assertThat(actualDto.getTotalHours()).isEqualTo(4);
        softly.assertThat(actualDto.getUpPercent()).isEqualTo(100);
        softly.assertThat(actualDto.getEquipmentData()).hasSize(2);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(true);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(false);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(eq(givenStart.toString()), eq(givenStop.toString()), eq(givenId));
    }

    @Test
    void testGetDataWithNullData() {
        // given
        final Instant now = Instant.now();
        final Instant expectedStart = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).minus(Period.ofMonths(1)).toInstant();
        final Instant expectedEnd = Instant.now();
        Long hoursBetween = Duration.between(expectedStart, expectedEnd).toHours();
        Instant givenTime1 = now.minusSeconds(60 * 60 * 10);
        Instant givenTime2 = now.minusSeconds(60 * 60 * 6);
        Instant givenTime3 = now.minusSeconds(60 * 60 * 3);
        List<EquipmentData> givenList = List.of(
                EquipmentData.builder().equipmentId(1L).time(givenTime1).enabled(true).build(),
                EquipmentData.builder().equipmentId(1L).time(givenTime2).enabled(false).build(),
                EquipmentData.builder().equipmentId(1L).time(givenTime3).enabled(true).build()
        );

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), isNull()))
                .thenReturn(givenList);

        // when
        StatisticsDto actualDto = equipmentDataService.getData(null, null, null);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDto).isNotNull();
        softly.assertThat(actualDto.getUpHours()).isEqualTo(7);
        softly.assertThat(actualDto.getDownHours()).isEqualTo(hoursBetween - 7);
        softly.assertThat(actualDto.getTotalHours()).isEqualTo(hoursBetween);
        softly.assertThat(actualDto.getUpPercent()).isEqualTo(7 * 100.0 / hoursBetween);
        softly.assertThat(actualDto.getEquipmentData()).hasSize(3);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEquipmentId()).isEqualTo(1L);
        softly.assertThat(actualDto.getEquipmentData().get(0).getEnabled()).isEqualTo(true);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEquipmentId()).isEqualTo(1L);
        softly.assertThat(actualDto.getEquipmentData().get(1).getEnabled()).isEqualTo(false);;
        softly.assertThat(actualDto.getEquipmentData().get(2).getEquipmentId()).isEqualTo(1L);
        softly.assertThat(actualDto.getEquipmentData().get(2).getEnabled()).isEqualTo(true);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(isNotNull(), isNotNull(), isNull());
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
