package ru.kpfu.machinemetrics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.SoftAssertions;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.kpfu.machinemetrics.config.MessageSourceConfig;
import ru.kpfu.machinemetrics.dto.EquipmentInfoCreateDto;
import ru.kpfu.machinemetrics.dto.ErrorResponse;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.mapper.EquipmentInfoMapper;
import ru.kpfu.machinemetrics.model.Cron;
import ru.kpfu.machinemetrics.model.DataService;
import ru.kpfu.machinemetrics.model.EquipmentInfo;
import ru.kpfu.machinemetrics.service.EquipmentInfoService;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.kpfu.machinemetrics.constants.EquipmentInfoConstants.EQUIPMENT_INFO_NOT_FOUND_EXCEPTION_MESSAGE;

@WebMvcTest(EquipmentInfoController.class)
@ImportAutoConfiguration(MessageSourceConfig.class)
public class EquipmentInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EquipmentInfoService equipmentInfoService;

    @MockBean
    private EquipmentInfoMapper equipmentInfoMapper;

    @Test
    public void testCreate() throws Exception {
        // given
        EquipmentInfoCreateDto equipmentInfoCreateDto = EquipmentInfoCreateDto.builder()
                .id(1L)
                .dataServiceId(2L)
                .cronId(1L)
                .enabled(true)
                .build();

        EquipmentInfo savedEquipmentInfo = EquipmentInfo.builder()
                .id(1L)
                .dataService(DataService.builder().id(equipmentInfoCreateDto.getDataServiceId()).build())
                .cron(Cron.builder().id(equipmentInfoCreateDto.getCronId()).build())
                .enabled(equipmentInfoCreateDto.isEnabled())
                .build();

        when(equipmentInfoMapper.toEquipmentInfo(any(EquipmentInfoCreateDto.class))).thenReturn(savedEquipmentInfo);
        when(equipmentInfoService.save(eq(savedEquipmentInfo))).thenReturn(savedEquipmentInfo);

        // expect
        mockMvc.perform(post("/api/v1/equipment-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipmentInfoCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedEquipmentInfo.getId()))
                .andExpect(jsonPath("$.dataService.id").value(savedEquipmentInfo.getDataService().getId()))
                .andExpect(jsonPath("$.cron.id").value(savedEquipmentInfo.getCron().getId()))
                .andExpect(jsonPath("$.enabled").value(savedEquipmentInfo.getEnabled()))
                .andExpect(header().string("Location", "/equipment/" + savedEquipmentInfo.getId()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    EquipmentInfo actualEquipmentInfo = objectMapper.readValue(response, EquipmentInfo.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualEquipmentInfo).isNotNull();
                    softly.assertThat(actualEquipmentInfo.getId()).isEqualTo(savedEquipmentInfo.getId());
                    softly.assertThat(actualEquipmentInfo.getDataService().getId()).isEqualTo(savedEquipmentInfo.getDataService().getId());
                    softly.assertThat(actualEquipmentInfo.getCron().getId()).isEqualTo(savedEquipmentInfo.getCron().getId());
                    softly.assertThat(actualEquipmentInfo.getEnabled()).isEqualTo(savedEquipmentInfo.getEnabled());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreateWithEmptyId() throws Exception {
        // given
        EquipmentInfoCreateDto equipmentInfoCreateDto = EquipmentInfoCreateDto.builder()
                .dataServiceId(2L)
                .cronId(1L)
                .enabled(true)
                .build();

        String message = messageSource.getMessage("validation.equipment-info.id.empty", null, new Locale("ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/equipment-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipmentInfoCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(expectedResponseBody.getStatus()))
                .andExpect(jsonPath("$.message").value(expectedResponseBody.getMessage()))
                .andDo(result -> {

                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    ErrorResponse actualResponseBody = objectMapper.readValue(response, ErrorResponse.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualResponseBody).isNotNull();
                    softly.assertThat(actualResponseBody.getStatus()).isEqualTo(expectedResponseBody.getStatus());
                    softly.assertThat(actualResponseBody.getMessage()).isEqualTo(expectedResponseBody.getMessage());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreateWithEmptyDataServiceId() throws Exception {
        // given
        EquipmentInfoCreateDto equipmentInfoCreateDto = EquipmentInfoCreateDto.builder()
                .id(1L)
                .cronId(1L)
                .enabled(true)
                .build();

        String message = messageSource.getMessage("validation.equipment-info.data-service.empty", null, new Locale(
                "ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/equipment-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipmentInfoCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(expectedResponseBody.getStatus()))
                .andExpect(jsonPath("$.message").value(expectedResponseBody.getMessage()))
                .andDo(result -> {

                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    ErrorResponse actualResponseBody = objectMapper.readValue(response, ErrorResponse.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualResponseBody).isNotNull();
                    softly.assertThat(actualResponseBody.getStatus()).isEqualTo(expectedResponseBody.getStatus());
                    softly.assertThat(actualResponseBody.getMessage()).isEqualTo(expectedResponseBody.getMessage());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreateWithEmptyCronId() throws Exception {
        // given
        EquipmentInfoCreateDto equipmentInfoCreateDto = EquipmentInfoCreateDto.builder()
                .id(1L)
                .dataServiceId(1L)
                .cronId(null)
                .enabled(true)
                .build();

        String message = messageSource.getMessage("validation.equipment-info.cron.empty", null, new Locale(
                "ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/equipment-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipmentInfoCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(expectedResponseBody.getStatus()))
                .andExpect(jsonPath("$.message").value(expectedResponseBody.getMessage()))
                .andDo(result -> {

                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    ErrorResponse actualResponseBody = objectMapper.readValue(response, ErrorResponse.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualResponseBody).isNotNull();
                    softly.assertThat(actualResponseBody.getStatus()).isEqualTo(expectedResponseBody.getStatus());
                    softly.assertThat(actualResponseBody.getMessage()).isEqualTo(expectedResponseBody.getMessage());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreateWithEmptyDataServiceIdAndCronId() throws Exception {
        // given
        EquipmentInfoCreateDto equipmentInfoCreateDto = EquipmentInfoCreateDto.builder()
                .id(1L)
                .build();

        EquipmentInfo savedEquipmentInfo = EquipmentInfo.builder()
                .id(1L)
                .enabled(equipmentInfoCreateDto.isEnabled())
                .build();

        when(equipmentInfoMapper.toEquipmentInfo(any(EquipmentInfoCreateDto.class))).thenReturn(savedEquipmentInfo);
        when(equipmentInfoService.save(eq(savedEquipmentInfo))).thenReturn(savedEquipmentInfo);

        // expect
        mockMvc.perform(post("/api/v1/equipment-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipmentInfoCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedEquipmentInfo.getId()))
                .andExpect(jsonPath("$.dataService").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.cron").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.enabled").value(savedEquipmentInfo.getEnabled()))
                .andExpect(header().string("Location", "/equipment/" + savedEquipmentInfo.getId()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    EquipmentInfo actualEquipmentInfo = objectMapper.readValue(response, EquipmentInfo.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualEquipmentInfo).isNotNull();
                    softly.assertThat(actualEquipmentInfo.getId()).isEqualTo(savedEquipmentInfo.getId());
                    softly.assertThat(actualEquipmentInfo.getDataService()).isNull();
                    softly.assertThat(actualEquipmentInfo.getCron()).isNull();
                    softly.assertThat(actualEquipmentInfo.getEnabled()).isEqualTo(savedEquipmentInfo.getEnabled());
                    softly.assertAll();
                });
    }

    @Test
    public void testGetExistingEquipmentInfo() throws Exception {
        // given
        EquipmentInfo equipmentInfo = EquipmentInfo.builder()
                .id(1L)
                .dataService(DataService.builder().id(1L).build())
                .cron(Cron.builder().id(1L).build())
                .enabled(false)
                .build();

        when(equipmentInfoService.getById(equipmentInfo.getId())).thenReturn(equipmentInfo);

        // expect
        mockMvc.perform(get("/api/v1/equipment-info/{id}", equipmentInfo.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(equipmentInfo.getId()))
                .andExpect(jsonPath("$.dataService.id").value(equipmentInfo.getDataService().getId()))
                .andExpect(jsonPath("$.cron.id").value(equipmentInfo.getCron().getId()))
                .andExpect(jsonPath("$.enabled").value(equipmentInfo.getEnabled()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    EquipmentInfo actualEquipmentInfo = objectMapper.readValue(response, EquipmentInfo.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualEquipmentInfo).isNotNull();
                    softly.assertThat(actualEquipmentInfo.getId()).isEqualTo(equipmentInfo.getId());
                    softly.assertThat(actualEquipmentInfo.getDataService().getId()).isEqualTo(equipmentInfo.getDataService().getId());
                    softly.assertThat(actualEquipmentInfo.getCron().getId()).isEqualTo(equipmentInfo.getCron().getId());
                    softly.assertThat(actualEquipmentInfo.getEnabled()).isEqualTo(equipmentInfo.getEnabled());
                    softly.assertAll();
                });
    }

    @Test
    public void testGetNonExistingEquipment() throws Exception {
        // given
        Long equipmentInfoId = 1L;

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                EQUIPMENT_INFO_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{equipmentInfoId},
                locale
        );

        when(equipmentInfoService.getById(equipmentInfoId)).thenThrow(new ResourceNotFoundException(message));

        ErrorResponse expectedResponseBody = new ErrorResponse(404, message);

        // expect
        mockMvc.perform(get("/api/v1/equipment-info/{id}", equipmentInfoId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(expectedResponseBody.getStatus()))
                .andExpect(jsonPath("$.message").value(expectedResponseBody.getMessage()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(errorResponse).isNotNull();
                    softly.assertThat(errorResponse.getStatus()).isEqualTo(expectedResponseBody.getStatus());
                    softly.assertThat(errorResponse.getMessage()).isEqualTo(expectedResponseBody.getMessage());
                    softly.assertAll();
                });
    }

    @Test
    public void testDeleteExistingEquipmentInfo() throws Exception {
        // given
        Long equipmentInfoId = 1L;
        doNothing().when(equipmentInfoService).delete(equipmentInfoId);

        // expect
        mockMvc.perform(delete("/api/v1/equipment-info/{id}", equipmentInfoId))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteNonExistingEquipmentInfo() throws Exception {
        // given
        Long equipmentInfoId = 1L;

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                EQUIPMENT_INFO_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{equipmentInfoId},
                locale
        );

        doThrow(new ResourceNotFoundException(message)).when(equipmentInfoService).delete(equipmentInfoId);

        ErrorResponse expectedResponseBody = new ErrorResponse(404, message);

        // expect
        mockMvc.perform(delete("/api/v1/equipment-info/{id}", equipmentInfoId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(expectedResponseBody.getStatus()))
                .andExpect(jsonPath("$.message").value(expectedResponseBody.getMessage()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(errorResponse).isNotNull();
                    softly.assertThat(errorResponse.getStatus()).isEqualTo(expectedResponseBody.getStatus());
                    softly.assertThat(errorResponse.getMessage()).isEqualTo(expectedResponseBody.getMessage());
                    softly.assertAll();
                });
    }
}
