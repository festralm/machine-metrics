package ru.kpfu.machinemetrics.listener;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.kpfu.machinemetrics.service.EquipmentDataService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;


@SpringBootTest
public class DataFetchListenerTest {

    @Autowired
    private DataFetchListener dataFetchListener;

    @MockBean
    private EquipmentDataService equipmentDataService;

    @Test
    public void testListen() {
        // given
        String message = "123";
        doNothing().when(equipmentDataService).generateData(123L);

        // when
        dataFetchListener.listen(message);

        // then
        verify(equipmentDataService).generateData(123L);
    }
}
