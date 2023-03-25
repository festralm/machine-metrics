package ru.kpfu.machinemetrics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.kpfu.machinemetrics.model.Equipment;
import ru.kpfu.machinemetrics.service.EquipmentService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EquipmentController.class)
public class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EquipmentService equipmentService;

    @Test
    public void testListAll_expectedList() throws Exception {
        // given
        Equipment equipment1 = Equipment.builder()
                .id(0L)
                .name("Equipment 1")
                .build();
        Equipment equipment2 = Equipment.builder()
                .id(1L)
                .name("Equipment 2")
                .build();
        List<Equipment> equipmentList = List.of(equipment1, equipment2);

        when(equipmentService.getAllNotDeleted()).thenReturn(equipmentList);

        // when
        mockMvc.perform(get("/equipment"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(equipment1.getId()))
                .andExpect(jsonPath("$[0].name").value(equipment1.getName()))
                .andExpect(jsonPath("$[1].id").value(equipment2.getId()))
                .andExpect(jsonPath("$[1].name").value(equipment2.getName()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString();
                    List<Equipment> actualList = Arrays.asList(new ObjectMapper().readValue(response, Equipment[].class));

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualList).isNotNull();
                    softly.assertThat(actualList).hasSize(2);
                    softly.assertThat(actualList.get(0).getName()).isEqualTo(equipment1.getName());
                    softly.assertThat(actualList.get(1).getName()).isEqualTo(equipment2.getName());
                    softly.assertAll();
                });
    }
}
