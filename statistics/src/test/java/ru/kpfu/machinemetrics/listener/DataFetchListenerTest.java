package ru.kpfu.machinemetrics.listener;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.kpfu.machinemetrics.service.EquipmentDataService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class DataFetchListenerTest {

    @Autowired
    private DataFetchListener dataFetchListener;

    @MockBean
    private EquipmentDataService equipmentDataService;

    @Test
    void testListen() {
        // given
        String givenIn = "1";

        doNothing().when(equipmentDataService).delete(1L);

        // when
        dataFetchListener.listen(givenIn);

        // then
        verify(equipmentDataService, times(1)).delete(1L);
    }
}
