package ru.kpfu.machinemetrics.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.kpfu.machinemetrics.repository.EquipmentDataRepository;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class EquipmentDataServiceTest {

    @Autowired
    private EquipmentDataService equipmentDataService;


    @MockBean
    private EquipmentDataRepository equipmentDataRepositoryMock;

    @Test
    public void testGenerateData() {
        // given
        doNothing().when(equipmentDataRepositoryMock).save(any());

        // expect
        assertThatCode(() -> equipmentDataService.generateData(1L)).doesNotThrowAnyException();
        verify(equipmentDataRepositoryMock).save(any());
    }

}
