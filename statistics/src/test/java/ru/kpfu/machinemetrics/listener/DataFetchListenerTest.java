package ru.kpfu.machinemetrics.listener;


import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.kpfu.machinemetrics.model.EquipmentData;
import ru.kpfu.machinemetrics.repository.EquipmentDataRepository;
import ru.kpfu.machinemetrics.service.EquipmentDataService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
