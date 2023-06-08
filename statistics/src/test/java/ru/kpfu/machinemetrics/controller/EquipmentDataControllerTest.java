package ru.kpfu.machinemetrics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.kpfu.machinemetrics.config.MessageSourceConfig;
import ru.kpfu.machinemetrics.dto.EquipmentDataDto;
import ru.kpfu.machinemetrics.dto.EquipmentStatisticsDto;
import ru.kpfu.machinemetrics.dto.StatisticsDto;
import ru.kpfu.machinemetrics.service.EquipmentDataService;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EquipmentDataController.class)
@ImportAutoConfiguration(MessageSourceConfig.class)
public class EquipmentDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EquipmentDataService equipmentDataService;

    @Test
    public void testListFiltered() throws Exception {
        // given
        Long givenId = 1L;
        OffsetDateTime givenStart = OffsetDateTime.now().minusSeconds(1_000_000);
        OffsetDateTime givenStop = OffsetDateTime.now();

        List<EquipmentDataDto> givenList = List.of(
                EquipmentDataDto.builder().equipmentId(givenId).enabled(true).build(),
                EquipmentDataDto.builder().equipmentId(givenId).enabled(false).build()
        );

        StatisticsDto givenDto = StatisticsDto.builder()
                .equipmentStatisticsDtos(List.of(EquipmentStatisticsDto.builder().equipmentData(givenList).build()))
                .start(givenStart)
                .end(givenStop)
                .build();

        when(equipmentDataService.getData(eq(List.of(givenId)), eq(givenStart), eq(givenStop))).thenReturn(givenDto);

        // expect
        mockMvc.perform(get("/api/v1/equipment-data")
                        .param("ids", givenId.toString())
                        .param("start", givenStart.toString())
                        .param("stop", givenStop.toString())
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.equipmentStatisticsDtos[0].equipmentData.length()").value(2))
                .andExpect(jsonPath("$.equipmentStatisticsDtos[0].equipmentData[0].equipmentId").value(givenId))
                .andExpect(jsonPath("$.equipmentStatisticsDtos[0].equipmentData[0].enabled").value(true))
                .andExpect(jsonPath("$.equipmentStatisticsDtos[0].equipmentData[1].equipmentId").value(givenId))
                .andExpect(jsonPath("$.equipmentStatisticsDtos[0].equipmentData[1].enabled").value(false))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    StatisticsDto actualDto = objectMapper.readValue(response, StatisticsDto.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualDto).isNotNull();
                    softly.assertThat(actualDto.getStart()).isEqualTo(givenStart);
                    softly.assertThat(actualDto.getEnd()).isEqualTo(givenStop);
                    softly.assertThat(actualDto.getEquipmentStatisticsDtos().get(0).getEquipmentData()).hasSize(2);
                    softly.assertThat(actualDto.getEquipmentStatisticsDtos().get(0).getEquipmentData().get(0).getEquipmentId()).isEqualTo(givenId);
                    softly.assertThat(actualDto.getEquipmentStatisticsDtos().get(0).getEquipmentData().get(0).getEnabled()).isEqualTo(true);
                    softly.assertThat(actualDto.getEquipmentStatisticsDtos().get(0).getEquipmentData().get(1).getEquipmentId()).isEqualTo(givenId);
                    softly.assertThat(actualDto.getEquipmentStatisticsDtos().get(0).getEquipmentData().get(1).getEnabled()).isEqualTo(false);
                    softly.assertAll();
                });
    }
}
