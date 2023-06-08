package ru.kpfu.machinemetrics.repository;

import com.influxdb.client.DeleteApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.DeletePredicateRequest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import ru.kpfu.machinemetrics.model.EquipmentData;
import ru.kpfu.machinemetrics.properties.InfluxDbProperties;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource("classpath:application.yml")
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

        String expectedPreviousQuery = "from(bucket: \"bucket\") " +
                "|> range(start: -inf, stop: time(v: 2023-03-24T01:00:00Z)) " +
                "|> filter(fn: (r) => r[\"_measurement\"] == \"equipment_statistics\")" +
                "|> filter(fn: (r) => r[\"equipment_id\"] == \"1\")" +
                "|> pivot(rowKey:[\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")" +
                "|> last(column: \"_time\")";

        String expectedGetAllQuery = "from(bucket: \"bucket\") " +
                "|> range(start: time(v: 2023-03-24T01:00:00Z), stop: time(v: 2023-04-24T01:00:00Z)) " +
                "|> filter(fn: (r) => r[\"_measurement\"] == \"equipment_statistics\")" +
                "|> filter(fn: (r) => r[\"equipment_id\"] == \"1\")" +
                "|> pivot(rowKey:[\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")";

        QueryApi queryApiMock = mock(QueryApi.class);
        when(influxDBClientMock.getQueryApi()).thenReturn(queryApiMock);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(4);
            runnable.run();

            return null;
        })
                .when(queryApiMock)
                .query(any(String.class), any(String.class), any(), any(), any(Runnable.class));

        // when
        List<EquipmentData> result = equipmentDataRepository.getData(givenStart, givenStop, givenId);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result).isNotNull();
        softly.assertThat(result).isEmpty();
        softly.assertAll();
        verify(influxDBClientMock, times(1)).getQueryApi();
        verify(queryApiMock, times(1)).query(
                eq(expectedPreviousQuery), eq("org"), any(), any(), any(Runnable.class)
        );
        verify(queryApiMock, times(1)).query(
                eq(expectedGetAllQuery), eq("org"), any(), any(), any(Runnable.class)
        );
    }

    @Test
    void testDeleteByEquipmentId() {
        // given
        Long givenId = 1L;
        final String expectedPredicate = "equipment_id=\"1\"";

        DeleteApi deleteApiMock = mock(DeleteApi.class);
        when(influxDBClientMock.getDeleteApi()).thenReturn(deleteApiMock);

        doNothing().when(deleteApiMock).delete(
                any(DeletePredicateRequest.class),
                any(String.class),
                any(String.class)
        );

        // when
        equipmentDataRepository.delete(givenId);

        // then
        verify(influxDBClientMock, times(1)).getDeleteApi();
        verify(deleteApiMock, times(1)).delete(
                argThat(arg -> arg != null && arg.getPredicate().equals(expectedPredicate)),
                eq("bucket"),
                eq("org")
        );
    }
}
