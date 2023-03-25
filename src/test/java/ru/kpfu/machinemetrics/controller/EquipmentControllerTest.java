package ru.kpfu.machinemetrics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.kpfu.machinemetrics.configuration.MessageSourceConfig;
import ru.kpfu.machinemetrics.dto.EquipmentDto;
import ru.kpfu.machinemetrics.dto.ErrorResponse;
import ru.kpfu.machinemetrics.mapper.EquipmentMapper;
import ru.kpfu.machinemetrics.model.Equipment;
import ru.kpfu.machinemetrics.service.EquipmentService;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EquipmentController.class)
@ImportAutoConfiguration(MessageSourceConfig.class)
public class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageSource messageSource;

    @MockBean
    private EquipmentService equipmentService;

    @MockBean
    private EquipmentMapper equipmentMapper;

    @Test
    public void testListAll() throws Exception {
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

        EquipmentDto equipmentDto1 = EquipmentDto.builder()
                .id(equipment1.getId())
                .name(equipment1.getName())
                .build();
        EquipmentDto equipmentDto2 = EquipmentDto.builder()
                .id(equipment2.getId())
                .name(equipment2.getName())
                .build();
        List<EquipmentDto> expectedEquipmentDtoList = List.of(equipmentDto1, equipmentDto2);

        when(equipmentService.getAllNotDeleted()).thenReturn(equipmentList);
        when(equipmentMapper.toEquipmentDtos(eq(equipmentList))).thenReturn(expectedEquipmentDtoList);

        // when
        mockMvc.perform(get("/api/v1/equipment"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(equipment1.getId()))
                .andExpect(jsonPath("$[0].name").value(equipment1.getName()))
                .andExpect(jsonPath("$[1].id").value(equipment2.getId()))
                .andExpect(jsonPath("$[1].name").value(equipment2.getName()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString();
                    List<EquipmentDto> actualList = Arrays.asList(new ObjectMapper().readValue(response, EquipmentDto[].class));

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualList).isNotNull();
                    softly.assertThat(actualList).hasSize(2);
                    softly.assertThat(actualList.get(0).getName()).isEqualTo(equipment1.getName());
                    softly.assertThat(actualList.get(1).getName()).isEqualTo(equipment2.getName());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreate() throws Exception {
        // given
        EquipmentDto equipmentDto = EquipmentDto.builder()
                .name("Equipment 1")
                .build();

        Equipment equipment = Equipment.builder()
                .name(equipmentDto.getName())
                .build();

        Equipment savedEquipment = Equipment.builder()
                .id(1L)
                .name(equipmentDto.getName())
                .build();

        EquipmentDto savedEquipmentDto = EquipmentDto.builder()
                .id(savedEquipment.getId())
                .name(equipmentDto.getName())
                .build();

        when(equipmentMapper.toEquipment(any(EquipmentDto.class))).thenReturn(equipment);
        when(equipmentService.save(eq(equipment))).thenReturn(savedEquipment);
        when(equipmentMapper.toEquipmentDto(eq(savedEquipment))).thenReturn(savedEquipmentDto);

        // when
        mockMvc.perform(post("/api/v1/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(equipmentDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedEquipmentDto.getId()))
                .andExpect(jsonPath("$.name").value(savedEquipmentDto.getName()))
                .andExpect(header().string("Location", "/equipment/" + savedEquipmentDto.getId()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString();
                    EquipmentDto actualEquipmentDto = new ObjectMapper().readValue(response, EquipmentDto.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualEquipmentDto).isNotNull();
                    softly.assertThat(actualEquipmentDto.getId()).isEqualTo(savedEquipmentDto.getId());
                    softly.assertThat(actualEquipmentDto.getName()).isEqualTo(savedEquipmentDto.getName());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreateWithEmptyName() throws Exception {
        // given
        EquipmentDto equipmentDto = EquipmentDto.builder()
                .name("")
                .build();

        String message = messageSource.getMessage("validation.equipment.name.empty", null, new Locale("ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // when
        mockMvc.perform(post("/api/v1/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(equipmentDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(expectedResponseBody.getStatus()))
                .andExpect(jsonPath("$.message").value(expectedResponseBody.getMessage()))
                .andDo(result -> {

                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    ErrorResponse actualResponseBody = new ObjectMapper().readValue(response, ErrorResponse.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualResponseBody).isNotNull();
                    softly.assertThat(actualResponseBody.getStatus()).isEqualTo(expectedResponseBody.getStatus());
                    softly.assertThat(actualResponseBody.getMessage()).isEqualTo(expectedResponseBody.getMessage());
                    softly.assertAll();
                });
    }
}
