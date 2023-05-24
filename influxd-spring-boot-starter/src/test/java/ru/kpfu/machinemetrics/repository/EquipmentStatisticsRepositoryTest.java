package ru.kpfu.machinemetrics.repository;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.kpfu.machinemetrics.properties.InfluxDbProperties;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = {EquipmentStatisticsRepository.class, InfluxDbProperties.class}
)
public class EquipmentStatisticsRepositoryTest {

    @Autowired
    private EquipmentStatisticsRepository equipmentStatisticsRepository;

    @MockBean
    private InfluxDBClient influxDBClientMock;

    @Autowired
    private InfluxDbProperties influxDbProperties;

    @Test
    void testSave() {
        // given
        Point point = Point
                .measurement("equipment_statistics")
                .addTag("equipment_id", Long.toString(1L))
                .addField("u", 25)
                .addField("enabled", true)
                .time(OffsetDateTime.now().toInstant(), WritePrecision.NS);

        WriteApiBlocking writeApiBlockingMock = Mockito.mock(WriteApiBlocking.class);
        when(influxDBClientMock.getWriteApiBlocking()).thenReturn(writeApiBlockingMock);
        doNothing().when(writeApiBlockingMock).writePoint(any(String.class), any(String.class), eq(point));

        // when
        equipmentStatisticsRepository.save(point);

        // then
        verify(influxDBClientMock, times(1)).getWriteApiBlocking();
        verify(writeApiBlockingMock, times(1)).writePoint(any(), any(), any(Point.class));
    }
}
