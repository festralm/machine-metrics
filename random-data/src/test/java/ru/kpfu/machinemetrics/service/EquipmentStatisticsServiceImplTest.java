package ru.kpfu.machinemetrics.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.kpfu.machinemetrics.repository.EquipmentStatisticsRepository;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class EquipmentStatisticsServiceImplTest {

    @Autowired
    private EquipmentStatisticsServiceImpl equipmentStatisticsService;


    @MockBean
    private EquipmentStatisticsRepository equipmentStatisticsRepositoryMock;

    @Test
    public void testProcess() {
        // given
        doNothing().when(equipmentStatisticsRepositoryMock).save(any());

        // expect
        assertThatCode(() -> equipmentStatisticsService.process(1L)).doesNotThrowAnyException();
        verify(equipmentStatisticsRepositoryMock).save(any());
    }

}
