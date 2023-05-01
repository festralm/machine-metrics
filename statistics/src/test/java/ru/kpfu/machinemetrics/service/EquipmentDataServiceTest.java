package ru.kpfu.machinemetrics.service;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.kpfu.machinemetrics.model.EquipmentData;
import ru.kpfu.machinemetrics.repository.EquipmentDataRepository;

import java.time.Instant;
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
    private void testGetData() {
        // given
        Long givenId = 1L;
        Instant givenStart = Instant.now().minusSeconds(1_000_000);
        Instant givenStop = Instant.now();

        List<EquipmentData> givenList = List.of(
                EquipmentData.builder().equipmentId(givenId).enabled(true).build(),
                EquipmentData.builder().equipmentId(givenId).enabled(false).build()
        );

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);

        // when
        List<EquipmentData> actualList = equipmentDataService.getData(givenId, givenStart, givenStop);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualList).isNotNull();
        softly.assertThat(actualList).hasSize(2);
        softly.assertThat(actualList.get(0).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualList.get(0).getEnabled()).isEqualTo(true);
        softly.assertThat(actualList.get(1).getEquipmentId()).isEqualTo(givenId);
        softly.assertThat(actualList.get(1).getEnabled()).isEqualTo(false);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(eq(givenStart.toString()), eq(givenStop.toString()), eq(givenId));
    }

    @Test
    private void testGetDataWithNullData() {
        // given
        List<EquipmentData> givenList = List.of(
                EquipmentData.builder().equipmentId(1L).enabled(true).build(),
                EquipmentData.builder().equipmentId(1L).enabled(false).build()
        );

        when(equipmentDataRepositoryMock.getData(any(String.class), any(String.class), any(Long.class)))
                .thenReturn(givenList);

        // when
        List<EquipmentData> actualList = equipmentDataService.getData(null, null, null);

        // expect
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualList).isNotNull();
        softly.assertThat(actualList).hasSize(2);
        softly.assertThat(actualList.get(0).getEquipmentId()).isEqualTo(1L);
        softly.assertThat(actualList.get(0).getEnabled()).isEqualTo(true);
        softly.assertThat(actualList.get(1).getEquipmentId()).isEqualTo(1L);
        softly.assertThat(actualList.get(1).getEnabled()).isEqualTo(false);
        softly.assertAll();
        verify(equipmentDataRepositoryMock, Mockito.times(1))
                .getData(isNotNull(), isNotNull(), isNull());
    }
}
