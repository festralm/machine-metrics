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
import ru.kpfu.machinemetrics.configuration.MessageSourceConfig;
import ru.kpfu.machinemetrics.model.EquipmentData;
import ru.kpfu.machinemetrics.service.EquipmentDataService;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
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
        Instant givenStart = Instant.now().minusSeconds(1_000_000);
        Instant givenStop = Instant.now();

        List<EquipmentData> givenList = List.of(
                EquipmentData.builder().equipmentId(givenId).enabled(true).build(),
                EquipmentData.builder().equipmentId(givenId).enabled(false).build()
        );

        when(equipmentDataService.getData(eq(givenId), eq(givenStart), eq(givenStop))).thenReturn(givenList);

        // expect
        mockMvc.perform(get("/api/v1/equipment-data")
                        .param("id", givenId.toString())
                        .param("start", givenStart.toString())
                        .param("stop", givenStop.toString())
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].equipmentId").value(givenId))
                .andExpect(jsonPath("$[0].enabled").value(true))
                .andExpect(jsonPath("$[1].equipmentId").value(givenId))
                .andExpect(jsonPath("$[1].enabled").value(false))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    List<EquipmentData> actualList = Arrays.asList(objectMapper.readValue(response,
                            EquipmentData[].class));

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualList).isNotNull();
                    softly.assertThat(actualList).hasSize(2);
                    softly.assertThat(actualList.get(0).getEquipmentId()).isEqualTo(givenId);
                    softly.assertThat(actualList.get(0).getEnabled()).isEqualTo(true);
                    softly.assertThat(actualList.get(1).getEquipmentId()).isEqualTo(givenId);
                    softly.assertThat(actualList.get(1).getEnabled()).isEqualTo(false);
                    softly.assertAll();
                });
    }
}
