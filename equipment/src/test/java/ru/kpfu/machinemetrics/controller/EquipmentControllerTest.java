package ru.kpfu.machinemetrics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.kpfu.machinemetrics.config.MessageSourceConfig;
import ru.kpfu.machinemetrics.dto.EquipmentCreateDto;
import ru.kpfu.machinemetrics.dto.EquipmentDetailsDto;
import ru.kpfu.machinemetrics.dto.EquipmentItemDto;
import ru.kpfu.machinemetrics.dto.ErrorResponse;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.mapper.EquipmentMapper;
import ru.kpfu.machinemetrics.model.Equipment;
import ru.kpfu.machinemetrics.service.EquipmentService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.kpfu.machinemetrics.constants.EquipmentConstants.EQUIPMENT_NOT_FOUND_EXCEPTION_MESSAGE;

@WebMvcTest(EquipmentController.class)
@ImportAutoConfiguration(MessageSourceConfig.class)
public class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EquipmentService equipmentServiceMock;

    @MockBean
    private EquipmentMapper equipmentMapperMock;

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
        final PageImpl<Equipment> equipmentPage = new PageImpl<>(List.of(equipment1, equipment2));

        EquipmentItemDto equipmentItemDto1 = EquipmentItemDto.builder()
                .id(equipment1.getId())
                .name(equipment1.getName())
                .build();
        EquipmentItemDto equipmentCreateDto2 = EquipmentItemDto.builder()
                .id(equipment2.getId())
                .name(equipment2.getName())
                .build();
        PageImpl<EquipmentItemDto> expectedEquipmentItemDtoPage = new PageImpl<>(List.of(equipmentItemDto1, equipmentCreateDto2));

        when(equipmentServiceMock.getAllNotDeleted(any(Pageable.class))).thenReturn(equipmentPage);
        when(equipmentMapperMock.toEquipmentItemDtos(eq(equipmentPage))).thenReturn(expectedEquipmentItemDtoPage);

        // expect
        mockMvc.perform(get("/api/v1/equipment"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].id").value(equipment1.getId()))
                .andExpect(jsonPath("$.content[0].name").value(equipment1.getName()))
                .andExpect(jsonPath("$.content[1].id").value(equipment2.getId()))
                .andExpect(jsonPath("$.content[1].name").value(equipment2.getName()));
    }

    @Test
    public void testSearch() throws Exception {
        // given
        Equipment equipment1 = Equipment.builder()
                .id(0L)
                .name("Equipment 1")
                .build();
        Equipment equipment2 = Equipment.builder()
                .id(1L)
                .name("Equipment 2")
                .build();
        final PageImpl<Equipment> equipmentPage = new PageImpl<>(List.of(equipment1, equipment2));

        EquipmentItemDto equipmentItemDto1 = EquipmentItemDto.builder()
                .id(equipment1.getId())
                .name(equipment1.getName())
                .build();
        EquipmentItemDto equipmentCreateDto2 = EquipmentItemDto.builder()
                .id(equipment2.getId())
                .name(equipment2.getName())
                .build();
        PageImpl<EquipmentItemDto> expectedEquipmentItemDtoPage = new PageImpl<>(List.of(equipmentItemDto1, equipmentCreateDto2));

        when(equipmentServiceMock.search(eq("test"), any(Pageable.class))).thenReturn(equipmentPage);
        when(equipmentMapperMock.toEquipmentItemDtos(eq(equipmentPage))).thenReturn(expectedEquipmentItemDtoPage);

        // expect
        mockMvc.perform(get("/api/v1/equipment/search-pageable")
                        .param("name", "test")
                        .param("size", "10")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].id").value(equipment1.getId()))
                .andExpect(jsonPath("$.content[0].name").value(equipment1.getName()))
                .andExpect(jsonPath("$.content[1].id").value(equipment2.getId()))
                .andExpect(jsonPath("$.content[1].name").value(equipment2.getName()));
    }

    @Test
    public void testCreate() throws Exception {
        // given
        EquipmentCreateDto equipmentCreateDto = EquipmentCreateDto.builder()
                .name("name 1")
                .inventoryNumber("inventoryNumber 1")
                .acquisitionSource("acquisitionSource 1")
                .cost(100.0)
                .initialCost(200.0)
                .residualCost(300.0)
                .adName("adName 1")
                .ipAddress("ipAddress 1")
                .build();

        Equipment equipment = Equipment.builder()
                .name(equipmentCreateDto.getName())
                .inventoryNumber(equipmentCreateDto.getInventoryNumber())
                .acquisitionSource(equipmentCreateDto.getAcquisitionSource())
                .cost(equipmentCreateDto.getCost())
                .initialCost(equipmentCreateDto.getInitialCost())
                .residualCost(equipmentCreateDto.getResidualCost())
                .adName(equipmentCreateDto.getAdName())
                .ipAddress(equipmentCreateDto.getIpAddress())
                .deleted(false)
                .build();

        Equipment savedEquipment = Equipment.builder()
                .id(1L)
                .name(equipment.getName())
                .inventoryNumber(equipment.getInventoryNumber())
                .acquisitionSource(equipment.getAcquisitionSource())
                .cost(equipment.getCost())
                .initialCost(equipment.getInitialCost())
                .residualCost(equipment.getResidualCost())
                .adName(equipment.getAdName())
                .ipAddress(equipment.getIpAddress())
                .deleted(false)
                .build();

        EquipmentDetailsDto savedEquipmentDetailsDto = EquipmentDetailsDto.builder()
                .id(savedEquipment.getId())
                .name(savedEquipment.getName())
                .inventoryNumber(savedEquipment.getInventoryNumber())
                .acquisitionSource(savedEquipment.getAcquisitionSource())
                .cost(savedEquipment.getCost())
                .initialCost(savedEquipment.getInitialCost())
                .residualCost(savedEquipment.getResidualCost())
                .adName(savedEquipment.getAdName())
                .ipAddress(savedEquipment.getIpAddress())
                .build();

        when(equipmentMapperMock.toEquipment(any(EquipmentCreateDto.class))).thenReturn(equipment);
        when(equipmentServiceMock.save(eq(equipment))).thenReturn(savedEquipment);
        when(equipmentMapperMock.toEquipmentDetailsDto(eq(savedEquipment))).thenReturn(savedEquipmentDetailsDto);

        // expect
        mockMvc.perform(post("/api/v1/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipmentCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedEquipmentDetailsDto.getId()))
                .andExpect(jsonPath("$.name").value(savedEquipmentDetailsDto.getName()))
                .andExpect(jsonPath("$.inventoryNumber").value(savedEquipmentDetailsDto.getInventoryNumber()))
                .andExpect(jsonPath("$.acquisitionSource").value(savedEquipmentDetailsDto.getAcquisitionSource()))
                .andExpect(jsonPath("$.cost").value(savedEquipmentDetailsDto.getCost()))
                .andExpect(jsonPath("$.initialCost").value(savedEquipmentDetailsDto.getInitialCost()))
                .andExpect(jsonPath("$.residualCost").value(savedEquipmentDetailsDto.getResidualCost()))
                .andExpect(jsonPath("$.adName").value(savedEquipmentDetailsDto.getAdName()))
                .andExpect(jsonPath("$.ipAddress").value(savedEquipmentDetailsDto.getIpAddress()))
                .andExpect(header().string("Location", "/equipment/" + savedEquipmentDetailsDto.getId()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    EquipmentDetailsDto actualEquipmentCreateDto = objectMapper.readValue(response,
                            EquipmentDetailsDto.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualEquipmentCreateDto).isNotNull();
                    softly.assertThat(actualEquipmentCreateDto.getId()).isEqualTo(savedEquipmentDetailsDto.getId());
                    softly.assertThat(actualEquipmentCreateDto.getName()).isEqualTo(savedEquipmentDetailsDto.getName());
                    softly.assertThat(actualEquipmentCreateDto.getInventoryNumber()).isEqualTo(savedEquipmentDetailsDto.getInventoryNumber());
                    softly.assertThat(actualEquipmentCreateDto.getAcquisitionSource()).isEqualTo(savedEquipmentDetailsDto.getAcquisitionSource());
                    softly.assertThat(actualEquipmentCreateDto.getCost()).isEqualTo(savedEquipmentDetailsDto.getCost());
                    softly.assertThat(actualEquipmentCreateDto.getInitialCost()).isEqualTo(savedEquipmentDetailsDto.getInitialCost());
                    softly.assertThat(actualEquipmentCreateDto.getResidualCost()).isEqualTo(savedEquipmentDetailsDto.getResidualCost());
                    softly.assertThat(actualEquipmentCreateDto.getAdName()).isEqualTo(savedEquipmentDetailsDto.getAdName());
                    softly.assertThat(actualEquipmentCreateDto.getIpAddress()).isEqualTo(savedEquipmentDetailsDto.getIpAddress());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreateWithEmptyName() throws Exception {
        // given
        EquipmentCreateDto equipmentCreateDto = EquipmentCreateDto.builder()
                .name("")
                .inventoryNumber("inventoryNumber 1")
                .acquisitionSource("acquisitionSource 1")
                .cost(100.0)
                .initialCost(200.0)
                .residualCost(300.0)
                .adName("adName 1")
                .ipAddress("ipAddress 1")
                .build();

        String message = messageSource.getMessage("validation.equipment.name.empty", null, new Locale("ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipmentCreateDto)))
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
    public void testCreateWithEmptyInventoryNumber() throws Exception {
        // given
        EquipmentCreateDto equipmentCreateDto = EquipmentCreateDto.builder()
                .name("name 1")
                .acquisitionSource("acquisitionSource 1")
                .cost(100.0)
                .initialCost(200.0)
                .residualCost(300.0)
                .adName("adName 1")
                .ipAddress("ipAddress 1")
                .build();

        String message = messageSource.getMessage("validation.equipment.inventory-number.empty", null, new Locale("ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipmentCreateDto)))
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
    public void testCreateWithEmptyAcquisitionSource() throws Exception {
        // given
        EquipmentCreateDto equipmentCreateDto = EquipmentCreateDto.builder()
                .name("name 1")
                .inventoryNumber("inventoryNumber 1")
                .acquisitionSource("   ")
                .cost(100.0)
                .initialCost(200.0)
                .residualCost(300.0)
                .adName("adName 1")
                .ipAddress("ipAddress 1")
                .build();

        String message = messageSource.getMessage("validation.equipment.acquisition-source.empty", null, new Locale("ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipmentCreateDto)))
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
    public void testCreateWithEmptyCost() throws Exception {
        // given
        EquipmentCreateDto equipmentCreateDto = EquipmentCreateDto.builder()
                .name("name 1")
                .inventoryNumber("inventoryNumber 1")
                .acquisitionSource("acquisitionSource 1")
                .initialCost(200.0)
                .residualCost(300.0)
                .adName("adName 1")
                .ipAddress("ipAddress 1")
                .build();

        String message = messageSource.getMessage("validation.equipment.cost.empty", null, new Locale("ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipmentCreateDto)))
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
    public void testCreateWithEmptyInitialCost() throws Exception {
        // given
        EquipmentCreateDto equipmentCreateDto = EquipmentCreateDto.builder()
                .name("name 1")
                .inventoryNumber("inventoryNumber 1")
                .acquisitionSource("acquisitionSource 1")
                .cost(100.0)
                .residualCost(300.0)
                .adName("adName 1")
                .ipAddress("ipAddress 1")
                .build();

        String message = messageSource.getMessage("validation.equipment.initial-cost.empty", null, new Locale("ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipmentCreateDto)))
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
    public void testCreateWithEmptyResidualCost() throws Exception {
        // given
        EquipmentCreateDto equipmentCreateDto = EquipmentCreateDto.builder()
                .name("name 1")
                .inventoryNumber("inventoryNumber 1")
                .acquisitionSource("acquisitionSource 1")
                .cost(100.0)
                .initialCost(200.0)
                .adName("adName 1")
                .ipAddress("ipAddress 1")
                .build();

        String message = messageSource.getMessage("validation.equipment.residual-cost.empty", null, new Locale("ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipmentCreateDto)))
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
    public void testCreateWithEmptyAdName() throws Exception {
        // given
        EquipmentCreateDto equipmentCreateDto = EquipmentCreateDto.builder()
                .name("name 1")
                .inventoryNumber("inventoryNumber 1")
                .acquisitionSource("acquisitionSource 1")
                .cost(100.0)
                .initialCost(200.0)
                .residualCost(200.0)
                .adName("")
                .ipAddress("ipAddress 1")
                .build();

        String message = messageSource.getMessage("validation.equipment.ad-name.empty", null, new Locale("ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipmentCreateDto)))
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
    public void testCreateWithEmptyIpAddress() throws Exception {
        // given
        EquipmentCreateDto equipmentCreateDto = EquipmentCreateDto.builder()
                .name("name 1")
                .inventoryNumber("inventoryNumber 1")
                .acquisitionSource("acquisitionSource 1")
                .cost(100.0)
                .initialCost(200.0)
                .residualCost(200.0)
                .adName("adName 1")
                .build();

        String message = messageSource.getMessage("validation.equipment.ip-address.empty", null, new Locale("ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipmentCreateDto)))
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
    public void testGetExistingEquipment() throws Exception {
        // given
        Equipment equipment = Equipment.builder()
                .id(1L)
                .inventoryNumber("INV123")
                .name("Equipment 1")
                .build();

        EquipmentDetailsDto equipmentDetailsDto = EquipmentDetailsDto.builder()
                .id(equipment.getId())
                .inventoryNumber(equipment.getInventoryNumber())
                .name(equipment.getName())
                .build();

        when(equipmentServiceMock.getById(equipment.getId())).thenReturn(equipment);
        when(equipmentMapperMock.toEquipmentDetailsDto(equipment)).thenReturn(equipmentDetailsDto);

        // expect
        mockMvc.perform(get("/api/v1/equipment/{id}", equipment.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(equipment.getId()))
                .andExpect(jsonPath("$.inventoryNumber").value(equipment.getInventoryNumber()))
                .andExpect(jsonPath("$.name").value(equipment.getName()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    EquipmentDetailsDto actualEquipmentDetailsDto = objectMapper.readValue(response,
                            EquipmentDetailsDto.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualEquipmentDetailsDto).isNotNull();
                    softly.assertThat(actualEquipmentDetailsDto.getId()).isEqualTo(equipment.getId());
                    softly.assertThat(actualEquipmentDetailsDto.getInventoryNumber()).isEqualTo(equipment.getInventoryNumber());
                    softly.assertThat(actualEquipmentDetailsDto.getName()).isEqualTo(equipment.getName());
                    softly.assertAll();
                });
    }

    @Test
    public void testGetNonExistingEquipment() throws Exception {
        // given
        Long equipmentId = 1L;

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                EQUIPMENT_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{equipmentId},
                locale
        );

        when(equipmentServiceMock.getById(equipmentId)).thenThrow(new ResourceNotFoundException(message));

        ErrorResponse expectedResponseBody = new ErrorResponse(404, message);

        // expect
        mockMvc.perform(get("/api/v1/equipment/{id}", equipmentId))
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
    public void testDeleteExistingEquipment() throws Exception {
        // given
        Long equipmentId = 1L;
        doNothing().when(equipmentServiceMock).delete(equipmentId);

        // expect
        mockMvc.perform(delete("/api/v1/equipment/{id}", equipmentId))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteNonExistingEquipment() throws Exception {
        // given
        Long equipmentId = 1L;

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                EQUIPMENT_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{equipmentId},
                locale
        );

        doThrow(new ResourceNotFoundException(message)).when(equipmentServiceMock).delete(equipmentId);

        ErrorResponse expectedResponseBody = new ErrorResponse(404, message);

        // expect
        mockMvc.perform(delete("/api/v1/equipment/{id}", equipmentId))
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
    public void testEditExistingEquipment() throws Exception {
        // given
        Long equipmentId = 1L;

        EquipmentCreateDto equipmentCreateDto = EquipmentCreateDto.builder()
                .name("name 1")
                .inventoryNumber("inventoryNumber 1")
                .acquisitionSource("acquisitionSource 1")
                .cost(100.0)
                .initialCost(200.0)
                .residualCost(300.0)
                .adName("adName 1")
                .ipAddress("ipAddress 1")
                .build();

        Equipment updatedEquipment = Equipment.builder()
                .name(equipmentCreateDto.getName())
                .inventoryNumber(equipmentCreateDto.getInventoryNumber())
                .acquisitionSource(equipmentCreateDto.getAcquisitionSource())
                .cost(equipmentCreateDto.getCost())
                .initialCost(equipmentCreateDto.getInitialCost())
                .residualCost(equipmentCreateDto.getResidualCost())
                .adName(equipmentCreateDto.getAdName())
                .ipAddress(equipmentCreateDto.getIpAddress())
                .deleted(false)
                .build();

        Equipment editedEquipment = Equipment.builder()
                .id(1L)
                .name(updatedEquipment.getName())
                .inventoryNumber(updatedEquipment.getInventoryNumber())
                .acquisitionSource(updatedEquipment.getAcquisitionSource())
                .cost(updatedEquipment.getCost())
                .initialCost(updatedEquipment.getInitialCost())
                .residualCost(updatedEquipment.getResidualCost())
                .adName(updatedEquipment.getAdName())
                .ipAddress(updatedEquipment.getIpAddress())
                .deleted(false)
                .build();

        EquipmentDetailsDto editedEquipmentDetailsDto = EquipmentDetailsDto.builder()
                .id(editedEquipment.getId())
                .name(editedEquipment.getName())
                .inventoryNumber(editedEquipment.getInventoryNumber())
                .acquisitionSource(editedEquipment.getAcquisitionSource())
                .cost(editedEquipment.getCost())
                .initialCost(editedEquipment.getInitialCost())
                .residualCost(editedEquipment.getResidualCost())
                .adName(editedEquipment.getAdName())
                .ipAddress(editedEquipment.getIpAddress())
                .build();

        when(equipmentMapperMock.toEquipment(any(EquipmentCreateDto.class))).thenReturn(updatedEquipment);
        when(equipmentServiceMock.edit(eq(equipmentId), eq(updatedEquipment))).thenReturn(editedEquipment);
        when(equipmentMapperMock.toEquipmentDetailsDto(eq(editedEquipment))).thenReturn(editedEquipmentDetailsDto);

        // when
        mockMvc.perform(put("/api/v1/equipment/{id}", equipmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipmentCreateDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(editedEquipmentDetailsDto.getId()))
                .andExpect(jsonPath("$.name").value(editedEquipmentDetailsDto.getName()))
                .andExpect(jsonPath("$.inventoryNumber").value(editedEquipmentDetailsDto.getInventoryNumber()))
                .andExpect(jsonPath("$.acquisitionSource").value(editedEquipmentDetailsDto.getAcquisitionSource()))
                .andExpect(jsonPath("$.cost").value(editedEquipmentDetailsDto.getCost()))
                .andExpect(jsonPath("$.initialCost").value(editedEquipmentDetailsDto.getInitialCost()))
                .andExpect(jsonPath("$.residualCost").value(editedEquipmentDetailsDto.getResidualCost()))
                .andExpect(jsonPath("$.adName").value(editedEquipmentDetailsDto.getAdName()))
                .andExpect(jsonPath("$.ipAddress").value(editedEquipmentDetailsDto.getIpAddress()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    EquipmentDetailsDto actualEquipmentDetailsDto = objectMapper.readValue(response,
                            EquipmentDetailsDto.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualEquipmentDetailsDto).isNotNull();
                    softly.assertThat(actualEquipmentDetailsDto.getId()).isEqualTo(editedEquipmentDetailsDto.getId());
                    softly.assertThat(actualEquipmentDetailsDto.getName()).isEqualTo(editedEquipmentDetailsDto.getName());
                    softly.assertThat(actualEquipmentDetailsDto.getInventoryNumber()).isEqualTo(editedEquipmentDetailsDto.getInventoryNumber());
                    softly.assertThat(actualEquipmentDetailsDto.getAcquisitionSource()).isEqualTo(editedEquipmentDetailsDto.getAcquisitionSource());
                    softly.assertThat(actualEquipmentDetailsDto.getCost()).isEqualTo(editedEquipmentDetailsDto.getCost());
                    softly.assertThat(actualEquipmentDetailsDto.getInitialCost()).isEqualTo(editedEquipmentDetailsDto.getInitialCost());
                    softly.assertThat(actualEquipmentDetailsDto.getResidualCost()).isEqualTo(editedEquipmentDetailsDto.getResidualCost());
                    softly.assertThat(actualEquipmentDetailsDto.getAdName()).isEqualTo(editedEquipmentDetailsDto.getAdName());
                    softly.assertThat(actualEquipmentDetailsDto.getIpAddress()).isEqualTo(editedEquipmentDetailsDto.getIpAddress());
                    softly.assertAll();
                });
    }

    @Test
    public void testEditNonExistingEquipment() throws Exception {
        // given
        Long equipmentId = 1L;

        EquipmentCreateDto equipmentCreateDto = EquipmentCreateDto.builder()
                .name("name 1")
                .inventoryNumber("inventoryNumber 1")
                .acquisitionSource("acquisitionSource 1")
                .cost(100.0)
                .initialCost(200.0)
                .residualCost(300.0)
                .adName("adName 1")
                .ipAddress("ipAddress 1")
                .build();

        Equipment updatedEquipment = Equipment.builder()
                .name(equipmentCreateDto.getName())
                .inventoryNumber(equipmentCreateDto.getInventoryNumber())
                .acquisitionSource(equipmentCreateDto.getAcquisitionSource())
                .cost(equipmentCreateDto.getCost())
                .initialCost(equipmentCreateDto.getInitialCost())
                .residualCost(equipmentCreateDto.getResidualCost())
                .adName(equipmentCreateDto.getAdName())
                .ipAddress(equipmentCreateDto.getIpAddress())
                .deleted(false)
                .build();

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                EQUIPMENT_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{equipmentId},
                locale
        );

        when(equipmentMapperMock.toEquipment(any(EquipmentCreateDto.class))).thenReturn(updatedEquipment);
        doThrow(new ResourceNotFoundException(message)).when(equipmentServiceMock).edit(eq(equipmentId),
                eq(updatedEquipment));

        ErrorResponse expectedResponseBody = new ErrorResponse(404, message);

        // expect
        mockMvc.perform(put("/api/v1/equipment/{id}", equipmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipmentCreateDto)))
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
