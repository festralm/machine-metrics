package ru.kpfu.machinemetrics.repository;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.kpfu.machinemetrics.model.EquipmentData;
import ru.kpfu.machinemetrics.properties.InfluxDbProperties;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest({"influxdb.bucket=bucket", "influxdb.org=org"})
public class EquipmentDataRepositoryTest {

    @Autowired
    private EquipmentDataRepository equipmentDataRepository;

    @MockBean
    private InfluxDBClient influxDBClientMock;

    @Autowired
    private InfluxDbProperties influxDbProperties;

    @Test
    void testGetData() {
        // given
        Long givenId = 1L;
        String givenStart = "2023-03-24T01:00:00Z";
        String givenStop = "2023-04-24T01:00:00Z";

        String expectedQuery = "from(bucket: \"bucket\") " +
                "|> range(start: time(v: 2023-03-24T01:00:00Z), stop: time(v: 2023-04-24T01:00:00Z)) " +
                "|> filter(fn: (r) => r[\"_measurement\"] == \"equipment_data\")" +
                "|> filter(fn: (r) => r[\"equipment_id\"] == 1)" +
                "|> pivot(rowKey:[\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")";

        QueryApi queryApiMock = mock(QueryApi.class);
        when(influxDBClientMock.getQueryApi()).thenReturn(queryApiMock);

        doReturn(List.of())
                .when(queryApiMock)
                .query(any(String.class), any(String.class));

        // when
        List<EquipmentData> result = equipmentDataRepository.getData(givenStart, givenStop, givenId);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result).isNotNull();
        softly.assertThat(result).isEmpty();
        softly.assertAll();
        verify(influxDBClientMock, times(1)).getQueryApi();
        verify(queryApiMock, times(1)).query(eq(expectedQuery), eq("org"));
    }
    @Test
    void testGetDataWithEmptyId() {
        // given
        String givenStart = "2023-03-24T01:00:00Z";
        String givenStop = "2023-04-24T01:00:00Z";

        String expectedQuery = "from(bucket: \"bucket\") " +
                "|> range(start: time(v: 2023-03-24T01:00:00Z), stop: time(v: 2023-04-24T01:00:00Z)) " +
                "|> filter(fn: (r) => r[\"_measurement\"] == \"equipment_data\")" +
                "|> pivot(rowKey:[\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")";

        QueryApi queryApiMock = mock(QueryApi.class);
        when(influxDBClientMock.getQueryApi()).thenReturn(queryApiMock);

        doReturn(List.of())
                .when(queryApiMock)
                .query(any(String.class), any(String.class));

        // when
        List<EquipmentData> result = equipmentDataRepository.getData(givenStart, givenStop, null);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result).isNotNull();
        softly.assertThat(result).isEmpty();
        softly.assertAll();
        verify(influxDBClientMock, times(1)).getQueryApi();
        verify(queryApiMock, times(1)).query(eq(expectedQuery), eq("org"));
    }
}
