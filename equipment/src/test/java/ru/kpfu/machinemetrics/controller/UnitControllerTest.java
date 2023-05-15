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
import ru.kpfu.machinemetrics.config.MessageSourceConfig;
import ru.kpfu.machinemetrics.dto.ErrorResponse;
import ru.kpfu.machinemetrics.dto.UnitCreateDto;
import ru.kpfu.machinemetrics.dto.UnitDto;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.mapper.UnitMapper;
import ru.kpfu.machinemetrics.model.Unit;
import ru.kpfu.machinemetrics.service.UnitService;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
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
import static ru.kpfu.machinemetrics.constants.UnitConstants.UNIT_NOT_FOUND_EXCEPTION_MESSAGE;

@WebMvcTest(UnitController.class)
@ImportAutoConfiguration(MessageSourceConfig.class)
public class UnitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UnitService unitService;

    @MockBean
    private UnitMapper unitMapper;

    @Test
    public void testListAll() throws Exception {
        // given
        Unit unit1 = Unit.builder()
                .id(1L)
                .name("Unit 1")
                .build();
        Unit unit2 = Unit.builder()
                .id(2L)
                .name("Unit 2")
                .build();
        List<Unit> unitList = List.of(unit1, unit2);

        UnitDto unitDto1 = UnitDto.builder()
                .id(unit1.getId())
                .name(unit1.getName())
                .build();
        UnitDto unitDto2 = UnitDto.builder()
                .id(unit2.getId())
                .name(unit2.getName())
                .build();
        List<UnitDto> expectedUnitDtoList = List.of(unitDto1, unitDto2);

        when(unitService.getAll()).thenReturn(unitList);
        when(unitMapper.toUnitDtos(any())).thenReturn(expectedUnitDtoList);

        // expect
        mockMvc.perform(get("/api/v1/unit"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(unitDto1.getId()))
                .andExpect(jsonPath("$[0].name").value(unitDto1.getName()))
                .andExpect(jsonPath("$[1].id").value(unitDto2.getId()))
                .andExpect(jsonPath("$[1].name").value(unitDto2.getName()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    List<Unit> actualList = Arrays.asList(objectMapper.readValue(response,
                            Unit[].class));

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualList).isNotNull();
                    softly.assertThat(actualList).hasSize(2);
                    softly.assertThat(actualList.get(0).getName()).isEqualTo(unitDto1.getName());
                    softly.assertThat(actualList.get(1).getName()).isEqualTo(unitDto2.getName());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreate() throws Exception {
        // given
        UnitCreateDto dto = UnitCreateDto.builder()
                .name("name")
                .build();

        Unit unitCreate = Unit.builder()
                .name(dto.getName())
                .build();

        Unit savedUnit = Unit.builder()
                .id(1L)
                .name(unitCreate.getName())
                .build();

        UnitDto savedUnitDto = UnitDto.builder()
                .id(savedUnit.getId())
                .name(savedUnit.getName())
                .build();

        when(unitMapper.toUnit(any())).thenReturn(unitCreate);
        when(unitService.save(any())).thenReturn(savedUnit);
        when(unitMapper.toUnitDto(any())).thenReturn(savedUnitDto);

        // expect
        mockMvc.perform(post("/api/v1/unit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unitCreate)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedUnitDto.getId()))
                .andExpect(jsonPath("$.name").value(savedUnitDto.getName()))
                .andExpect(header().string("Location", "/unit"))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    Unit actualUnitCreate = objectMapper.readValue(response, Unit.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualUnitCreate).isNotNull();
                    softly.assertThat(actualUnitCreate.getId()).isEqualTo(savedUnitDto.getId());
                    softly.assertThat(actualUnitCreate.getName()).isEqualTo(savedUnitDto.getName());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreateWithEmptyName() throws Exception {
        // given
        UnitCreateDto dto = UnitCreateDto.builder()
                .name("")
                .build();

        String message = messageSource.getMessage("validation.unit.name.empty", null, new Locale("ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/unit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
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
    public void testDeleteExistingUnit() throws Exception {
        // given
        Long unitId = 1L;
        doNothing().when(unitService).delete(unitId);

        // expect
        mockMvc.perform(delete("/api/v1/unit/{id}", unitId))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteNonExistingUnit() throws Exception {
        // given
        Long unitId = 1L;

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                UNIT_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{unitId},
                locale
        );

        doThrow(new ResourceNotFoundException(message)).when(unitService).delete(unitId);

        ErrorResponse expectedResponseBody = new ErrorResponse(404, message);

        // expect
        mockMvc.perform(delete("/api/v1/unit/{id}", unitId))
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
