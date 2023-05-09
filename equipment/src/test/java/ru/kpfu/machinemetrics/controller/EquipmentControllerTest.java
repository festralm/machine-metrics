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
import ru.kpfu.machinemetrics.dto.EquipmentCreateDto;
import ru.kpfu.machinemetrics.dto.EquipmentDetailsDto;
import ru.kpfu.machinemetrics.dto.EquipmentItemDto;
import ru.kpfu.machinemetrics.dto.ErrorResponse;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.mapper.EquipmentMapper;
import ru.kpfu.machinemetrics.model.Equipment;
import ru.kpfu.machinemetrics.service.EquipmentService;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
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
        List<Equipment> equipmentList = List.of(equipment1, equipment2);

        EquipmentItemDto equipmentItemDto1 = EquipmentItemDto.builder()
                .id(equipment1.getId())
                .name(equipment1.getName())
                .build();
        EquipmentItemDto equipmentCreateDto2 = EquipmentItemDto.builder()
                .id(equipment2.getId())
                .name(equipment2.getName())
                .build();
        List<EquipmentItemDto> expectedEquipmentItemDtoList = List.of(equipmentItemDto1, equipmentCreateDto2);

        when(equipmentServiceMock.getAllNotDeleted()).thenReturn(equipmentList);
        when(equipmentMapperMock.toEquipmentItemDtos(eq(equipmentList))).thenReturn(expectedEquipmentItemDtoList);

        // expect
        mockMvc.perform(get("/api/v1/equipment"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(equipment1.getId()))
                .andExpect(jsonPath("$[0].name").value(equipment1.getName()))
                .andExpect(jsonPath("$[1].id").value(equipment2.getId()))
                .andExpect(jsonPath("$[1].name").value(equipment2.getName()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    List<EquipmentItemDto> actualList = Arrays.asList(objectMapper.readValue(response,
                            EquipmentItemDto[].class));

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
        EquipmentCreateDto equipmentCreateDto = EquipmentCreateDto.builder()
                .photoPath("path/to/photo")
                .inventoryNumber("INV123")
                .name("Equipment 1")
                .cost(BigDecimal.valueOf(1000.00))
                .source("Supplier A")
                .department("Department 1")
                .responsiblePerson("John Doe")
                .status("Operational")
                .receiptDate(Instant.now())
                .lastOperationDate(Instant.now())
                .build();

        Equipment equipment = Equipment.builder()
                .photoPath(equipmentCreateDto.getPhotoPath())
                .inventoryNumber(equipmentCreateDto.getInventoryNumber())
                .name(equipmentCreateDto.getName())
                .cost(equipmentCreateDto.getCost())
                .source(equipmentCreateDto.getSource())
                .department(equipmentCreateDto.getDepartment())
                .responsiblePerson(equipmentCreateDto.getResponsiblePerson())
                .status(equipmentCreateDto.getStatus())
                .receiptDate(equipmentCreateDto.getReceiptDate())
                .lastOperationDate(equipmentCreateDto.getLastOperationDate())
                .build();

        Equipment savedEquipment = Equipment.builder()
                .id(1L)
                .photoPath(equipment.getPhotoPath())
                .inventoryNumber(equipment.getInventoryNumber())
                .name(equipment.getName())
                .cost(equipment.getCost())
                .source(equipment.getSource())
                .department(equipment.getDepartment())
                .responsiblePerson(equipment.getResponsiblePerson())
                .status(equipment.getStatus())
                .receiptDate(equipment.getReceiptDate())
                .lastOperationDate(equipment.getLastOperationDate())
                .build();

        EquipmentDetailsDto savedEquipmentDetailsDto = EquipmentDetailsDto.builder()
                .id(savedEquipment.getId())
                .photoPath(savedEquipment.getPhotoPath())
                .inventoryNumber(savedEquipment.getInventoryNumber())
                .name(savedEquipment.getName())
                .cost(savedEquipment.getCost())
                .source(savedEquipment.getSource())
                .department(savedEquipment.getDepartment())
                .responsiblePerson(savedEquipment.getResponsiblePerson())
                .status(savedEquipment.getStatus())
                .receiptDate(savedEquipment.getReceiptDate())
                .lastOperationDate(savedEquipment.getLastOperationDate())
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
                .andExpect(jsonPath("$.photoPath").value(savedEquipmentDetailsDto.getPhotoPath()))
                .andExpect(jsonPath("$.inventoryNumber").value(savedEquipmentDetailsDto.getInventoryNumber()))
                .andExpect(jsonPath("$.name").value(savedEquipmentDetailsDto.getName()))
                .andExpect(jsonPath("$.cost").value(savedEquipmentDetailsDto.getCost()))
                .andExpect(jsonPath("$.source").value(savedEquipmentDetailsDto.getSource()))
                .andExpect(jsonPath("$.department").value(savedEquipmentDetailsDto.getDepartment()))
                .andExpect(jsonPath("$.responsiblePerson").value(savedEquipmentDetailsDto.getResponsiblePerson()))
                .andExpect(jsonPath("$.status").value(savedEquipmentDetailsDto.getStatus()))
                .andExpect(header().string("Location", "/equipment/" + savedEquipmentDetailsDto.getId()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    EquipmentDetailsDto actualEquipmentCreateDto = objectMapper.readValue(response,
                            EquipmentDetailsDto.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualEquipmentCreateDto).isNotNull();
                    softly.assertThat(actualEquipmentCreateDto.getId()).isEqualTo(savedEquipmentDetailsDto.getId());
                    softly.assertThat(actualEquipmentCreateDto.getPhotoPath()).isEqualTo(savedEquipmentDetailsDto.getPhotoPath());
                    softly.assertThat(actualEquipmentCreateDto.getInventoryNumber()).isEqualTo(savedEquipmentDetailsDto.getInventoryNumber());
                    softly.assertThat(actualEquipmentCreateDto.getName()).isEqualTo(savedEquipmentDetailsDto.getName());
                    softly.assertThat(actualEquipmentCreateDto.getCost()).isEqualTo(savedEquipmentDetailsDto.getCost());
                    softly.assertThat(actualEquipmentCreateDto.getSource()).isEqualTo(savedEquipmentDetailsDto.getSource());
                    softly.assertThat(actualEquipmentCreateDto.getDepartment()).isEqualTo(savedEquipmentDetailsDto.getDepartment());
                    softly.assertThat(actualEquipmentCreateDto.getResponsiblePerson()).isEqualTo(savedEquipmentDetailsDto.getResponsiblePerson());
                    softly.assertThat(actualEquipmentCreateDto.getStatus()).isEqualTo(savedEquipmentDetailsDto.getStatus());
                    softly.assertThat(actualEquipmentCreateDto.getReceiptDate()).isEqualTo(savedEquipmentDetailsDto.getReceiptDate());
                    softly.assertThat(actualEquipmentCreateDto.getLastOperationDate()).isEqualTo(savedEquipmentDetailsDto.getLastOperationDate());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreateWithEmptyName() throws Exception {
        // given
        EquipmentCreateDto equipmentCreateDto = EquipmentCreateDto.builder()
                .name("")
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
    public void testGetExistingEquipment() throws Exception {
        // given
        Equipment equipment = Equipment.builder()
                .id(1L)
                .photoPath("path/to/photo")
                .inventoryNumber("INV123")
                .name("Equipment 1")
                .cost(BigDecimal.valueOf(1000.00))
                .source("Supplier A")
                .department("Department 1")
                .responsiblePerson("John Doe")
                .status("Operational")
                .receiptDate(Instant.now())
                .lastOperationDate(Instant.now())
                .build();

        EquipmentDetailsDto equipmentDetailsDto = EquipmentDetailsDto.builder()
                .id(equipment.getId())
                .photoPath(equipment.getPhotoPath())
                .inventoryNumber(equipment.getInventoryNumber())
                .name(equipment.getName())
                .cost(equipment.getCost())
                .source(equipment.getSource())
                .department(equipment.getDepartment())
                .responsiblePerson(equipment.getResponsiblePerson())
                .status(equipment.getStatus())
                .receiptDate(equipment.getReceiptDate())
                .lastOperationDate(equipment.getLastOperationDate())
                .build();

        when(equipmentServiceMock.getById(equipment.getId())).thenReturn(equipment);
        when(equipmentMapperMock.toEquipmentDetailsDto(equipment)).thenReturn(equipmentDetailsDto);

        // expect
        mockMvc.perform(get("/api/v1/equipment/{id}", equipment.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(equipment.getId()))
                .andExpect(jsonPath("$.photoPath").value(equipment.getPhotoPath()))
                .andExpect(jsonPath("$.inventoryNumber").value(equipment.getInventoryNumber()))
                .andExpect(jsonPath("$.name").value(equipment.getName()))
                .andExpect(jsonPath("$.cost").value(equipment.getCost()))
                .andExpect(jsonPath("$.source").value(equipment.getSource()))
                .andExpect(jsonPath("$.department").value(equipment.getDepartment()))
                .andExpect(jsonPath("$.responsiblePerson").value(equipment.getResponsiblePerson()))
                .andExpect(jsonPath("$.status").value(equipment.getStatus()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    EquipmentDetailsDto actualEquipmentDetailsDto = objectMapper.readValue(response,
                            EquipmentDetailsDto.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualEquipmentDetailsDto).isNotNull();
                    softly.assertThat(actualEquipmentDetailsDto.getId()).isEqualTo(equipment.getId());
                    softly.assertThat(actualEquipmentDetailsDto.getPhotoPath()).isEqualTo(equipment.getPhotoPath());
                    softly.assertThat(actualEquipmentDetailsDto.getInventoryNumber()).isEqualTo(equipment.getInventoryNumber());
                    softly.assertThat(actualEquipmentDetailsDto.getName()).isEqualTo(equipment.getName());
                    softly.assertThat(actualEquipmentDetailsDto.getCost()).isEqualByComparingTo(equipment.getCost());
                    softly.assertThat(actualEquipmentDetailsDto.getSource()).isEqualTo(equipment.getSource());
                    softly.assertThat(actualEquipmentDetailsDto.getDepartment()).isEqualTo(equipment.getDepartment());
                    softly.assertThat(actualEquipmentDetailsDto.getResponsiblePerson()).isEqualTo(equipment.getResponsiblePerson());
                    softly.assertThat(actualEquipmentDetailsDto.getStatus()).isEqualTo(equipment.getStatus());
                    softly.assertThat(actualEquipmentDetailsDto.getReceiptDate()).isEqualTo(equipment.getReceiptDate());
                    softly.assertThat(actualEquipmentDetailsDto.getLastOperationDate()).isEqualTo(equipment.getLastOperationDate());
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
                .photoPath("path/to/photo")
                .inventoryNumber("INV123")
                .name("Equipment 1")
                .cost(BigDecimal.valueOf(1000.00))
                .source("Supplier A")
                .department("Department 1")
                .responsiblePerson("John Doe")
                .status("Operational")
                .receiptDate(Instant.now())
                .lastOperationDate(Instant.now())
                .build();

        Equipment updatedEquipment = Equipment.builder()
                .photoPath(equipmentCreateDto.getPhotoPath())
                .inventoryNumber(equipmentCreateDto.getInventoryNumber())
                .name(equipmentCreateDto.getName())
                .cost(equipmentCreateDto.getCost())
                .source(equipmentCreateDto.getSource())
                .department(equipmentCreateDto.getDepartment())
                .responsiblePerson(equipmentCreateDto.getResponsiblePerson())
                .status(equipmentCreateDto.getStatus())
                .receiptDate(equipmentCreateDto.getReceiptDate())
                .lastOperationDate(equipmentCreateDto.getLastOperationDate())
                .build();

        Equipment editedEquipment = Equipment.builder()
                .id(equipmentId)
                .photoPath(updatedEquipment.getPhotoPath())
                .inventoryNumber(updatedEquipment.getInventoryNumber())
                .name(updatedEquipment.getName())
                .cost(updatedEquipment.getCost())
                .source(updatedEquipment.getSource())
                .department(updatedEquipment.getDepartment())
                .responsiblePerson(updatedEquipment.getResponsiblePerson())
                .status(updatedEquipment.getStatus())
                .receiptDate(updatedEquipment.getReceiptDate())
                .lastOperationDate(updatedEquipment.getLastOperationDate())
                .build();

        EquipmentDetailsDto editedEquipmentDetailsDto = EquipmentDetailsDto.builder()
                .id(editedEquipment.getId())
                .photoPath(editedEquipment.getPhotoPath())
                .inventoryNumber(editedEquipment.getInventoryNumber())
                .name(editedEquipment.getName())
                .cost(editedEquipment.getCost())
                .source(editedEquipment.getSource())
                .department(editedEquipment.getDepartment())
                .responsiblePerson(editedEquipment.getResponsiblePerson())
                .status(editedEquipment.getStatus())
                .receiptDate(editedEquipment.getReceiptDate())
                .lastOperationDate(editedEquipment.getLastOperationDate())
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
                .andExpect(jsonPath("$.id").value(equipmentId))
                .andExpect(jsonPath("$.photoPath").value(editedEquipmentDetailsDto.getPhotoPath()))
                .andExpect(jsonPath("$.inventoryNumber").value(editedEquipmentDetailsDto.getInventoryNumber()))
                .andExpect(jsonPath("$.name").value(editedEquipmentDetailsDto.getName()))
                .andExpect(jsonPath("$.cost").value(editedEquipmentDetailsDto.getCost()))
                .andExpect(jsonPath("$.source").value(editedEquipmentDetailsDto.getSource()))
                .andExpect(jsonPath("$.department").value(editedEquipmentDetailsDto.getDepartment()))
                .andExpect(jsonPath("$.responsiblePerson").value(editedEquipmentDetailsDto.getResponsiblePerson()))
                .andExpect(jsonPath("$.status").value(editedEquipmentDetailsDto.getStatus()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    EquipmentDetailsDto actualEquipmentDetailsDto = objectMapper.readValue(response,
                            EquipmentDetailsDto.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualEquipmentDetailsDto).isNotNull();
                    softly.assertThat(actualEquipmentDetailsDto.getId()).isEqualTo(editedEquipmentDetailsDto.getId());
                    softly.assertThat(actualEquipmentDetailsDto.getPhotoPath()).isEqualTo(editedEquipmentDetailsDto.getPhotoPath());
                    softly.assertThat(actualEquipmentDetailsDto.getInventoryNumber()).isEqualTo(editedEquipmentDetailsDto.getInventoryNumber());
                    softly.assertThat(actualEquipmentDetailsDto.getName()).isEqualTo(editedEquipmentDetailsDto.getName());
                    softly.assertThat(actualEquipmentDetailsDto.getCost()).isEqualByComparingTo(editedEquipmentDetailsDto.getCost());
                    softly.assertThat(actualEquipmentDetailsDto.getSource()).isEqualTo(editedEquipmentDetailsDto.getSource());
                    softly.assertThat(actualEquipmentDetailsDto.getDepartment()).isEqualTo(editedEquipmentDetailsDto.getDepartment());
                    softly.assertThat(actualEquipmentDetailsDto.getResponsiblePerson()).isEqualTo(editedEquipmentDetailsDto.getResponsiblePerson());
                    softly.assertThat(actualEquipmentDetailsDto.getStatus()).isEqualTo(editedEquipmentDetailsDto.getStatus());
                    softly.assertThat(actualEquipmentDetailsDto.getReceiptDate()).isEqualTo(editedEquipmentDetailsDto.getReceiptDate());
                    softly.assertThat(actualEquipmentDetailsDto.getLastOperationDate()).isEqualTo(editedEquipmentDetailsDto.getLastOperationDate());
                    softly.assertAll();
                });
    }

    @Test
    public void testEditNonExistingEquipment() throws Exception {
        // given
        Long equipmentId = 1L;


        EquipmentCreateDto equipmentCreateDto = EquipmentCreateDto.builder()
                .photoPath("path/to/photo")
                .inventoryNumber("INV123")
                .name("Equipment 1")
                .cost(BigDecimal.valueOf(1000.00))
                .source("Supplier A")
                .department("Department 1")
                .responsiblePerson("John Doe")
                .status("Operational")
                .receiptDate(Instant.now())
                .lastOperationDate(Instant.now())
                .build();

        Equipment updatedEquipment = Equipment.builder()
                .photoPath(equipmentCreateDto.getPhotoPath())
                .inventoryNumber(equipmentCreateDto.getInventoryNumber())
                .name(equipmentCreateDto.getName())
                .cost(equipmentCreateDto.getCost())
                .source(equipmentCreateDto.getSource())
                .department(equipmentCreateDto.getDepartment())
                .responsiblePerson(equipmentCreateDto.getResponsiblePerson())
                .status(equipmentCreateDto.getStatus())
                .receiptDate(equipmentCreateDto.getReceiptDate())
                .lastOperationDate(equipmentCreateDto.getLastOperationDate())
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
