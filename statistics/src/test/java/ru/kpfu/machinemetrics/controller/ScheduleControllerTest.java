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
import ru.kpfu.machinemetrics.dto.ScheduleCreateDto;
import ru.kpfu.machinemetrics.dto.ScheduleDto;
import ru.kpfu.machinemetrics.exception.CannotDeleteScheduleException;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.exception.ScheduleIsAlreadyCreatedException;
import ru.kpfu.machinemetrics.mapper.ScheduleMapper;
import ru.kpfu.machinemetrics.model.Schedule;
import ru.kpfu.machinemetrics.service.ScheduleService;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
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
import static ru.kpfu.machinemetrics.constants.ScheduleConstants.SCHEDULE_CREATED_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.ScheduleConstants.SCHEDULE_DATE_NOT_EMPTY_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.ScheduleConstants.SCHEDULE_DELETE_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.ScheduleConstants.SCHEDULE_END_TIME_EMPTY_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.ScheduleConstants.SCHEDULE_END_TIME_NOT_EMPTY_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.ScheduleConstants.SCHEDULE_END_TIME_NOT_VALID_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.ScheduleConstants.SCHEDULE_NOT_FOUND_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.ScheduleConstants.SCHEDULE_START_TIME_EMPTY_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.ScheduleConstants.SCHEDULE_START_TIME_NOT_EMPTY_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.ScheduleConstants.SCHEDULE_START_TIME_NOT_VALID_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.ScheduleConstants.SCHEDULE_WEEKDAY_BOUNDARY_EXCEPTION_MESSAGE;

@WebMvcTest(ScheduleController.class)
@ImportAutoConfiguration(MessageSourceConfig.class)
public class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ScheduleService scheduleServiceMock;

    @MockBean
    private ScheduleMapper scheduleMapperMock;

    @Test
    public void testListAll() throws Exception {
        // given
        Schedule schedule1 = Schedule.builder()
                .id(1L)
                .weekday(1)
                .date(OffsetDateTime.now())
                .equipmentId(1L)
                .startTime(11 * 60)
                .endTime(18 * 60)
                .build();
        Schedule schedule2 = Schedule.builder()
                .id(2L)
                .weekday(2)
                .date(OffsetDateTime.now())
                .equipmentId(2L)
                .isWorkday(false)
                .build();
        List<Schedule> scheduleList = List.of(schedule1, schedule2);

        ScheduleDto scheduleDto1 = ScheduleDto.builder()
                .id(schedule1.getId())
                .weekday(schedule1.getWeekday())
                .date(schedule1.getDate())
                .equipmentId(schedule1.getEquipmentId())
                .startTime("11:00")
                .endTime("18:00")
                .build();

        ScheduleDto scheduleDto2 = ScheduleDto.builder()
                .id(schedule2.getId())
                .weekday(schedule2.getWeekday())
                .date(schedule2.getDate())
                .equipmentId(schedule2.getEquipmentId())
                .isWorkday(schedule2.getIsWorkday())
                .build();
        List<ScheduleDto> scheduleDtoList = List.of(scheduleDto1, scheduleDto2);

        when(scheduleServiceMock.listNotDefault()).thenReturn(scheduleList);
        when(scheduleMapperMock.toScheduleDtos(scheduleList)).thenReturn(scheduleDtoList);

        // expect
        mockMvc.perform(get("/api/v1/schedule"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(scheduleDto1.getId()))
                .andExpect(jsonPath("$[0].weekday").value(scheduleDto1.getWeekday()))
                .andExpect(jsonPath("$[0].equipmentId").value(scheduleDto1.getEquipmentId()))
                .andExpect(jsonPath("$[0].startTime").value(scheduleDto1.getStartTime()))
                .andExpect(jsonPath("$[0].endTime").value(scheduleDto1.getEndTime()))
                .andExpect(jsonPath("$[1].id").value(scheduleDto2.getId()))
                .andExpect(jsonPath("$[1].weekday").value(scheduleDto2.getWeekday()))
                .andExpect(jsonPath("$[1].equipmentId").value(scheduleDto2.getEquipmentId()))
                .andExpect(jsonPath("$[1].isWorkday").value(scheduleDto2.getIsWorkday()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    List<ScheduleDto> actualList = Arrays.asList(objectMapper.readValue(response, ScheduleDto[].class));

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualList).isNotNull();
                    softly.assertThat(actualList).hasSize(2);
                    softly.assertThat(actualList.get(0).getId()).isEqualTo(scheduleDto1.getId());
                    softly.assertThat(actualList.get(0).getWeekday()).isEqualTo(scheduleDto1.getWeekday());
                    softly.assertThat(actualList.get(0).getDate()).isEqualTo(scheduleDto1.getDate());
                    softly.assertThat(actualList.get(0).getEquipmentId()).isEqualTo(scheduleDto1.getEquipmentId());
                    softly.assertThat(actualList.get(0).getStartTime()).isEqualTo(scheduleDto1.getStartTime());
                    softly.assertThat(actualList.get(0).getEndTime()).isEqualTo(scheduleDto1.getEndTime());
                    softly.assertThat(actualList.get(1).getId()).isEqualTo(scheduleDto2.getId());
                    softly.assertThat(actualList.get(1).getWeekday()).isEqualTo(scheduleDto2.getWeekday());
                    softly.assertThat(actualList.get(1).getDate()).isEqualTo(scheduleDto2.getDate());
                    softly.assertThat(actualList.get(1).getEquipmentId()).isEqualTo(scheduleDto2.getEquipmentId());
                    softly.assertThat(actualList.get(1).getIsWorkday()).isEqualTo(scheduleDto2.getIsWorkday());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreate() throws Exception {
        // given
        ScheduleCreateDto dto = ScheduleCreateDto.builder()
                .weekday(1)
                .startTime("11:00")
                .endTime("18:00")
                .build();

        Schedule schedule = Schedule.builder()
                .weekday(dto.getWeekday())
                .startTime(11 * 60)
                .endTime(18 * 60)
                .build();

        Schedule savedSchedule = Schedule.builder()
                .id(1L)
                .weekday(schedule.getWeekday())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .build();

        ScheduleDto savedScheduleDto = ScheduleDto.builder()
                .id(savedSchedule.getId())
                .weekday(savedSchedule.getWeekday())
                .startTime("11:00")
                .endTime("18:00")
                .build();

        when(scheduleMapperMock.toSchedule(any())).thenReturn(schedule);
        when(scheduleServiceMock.save(any())).thenReturn(savedSchedule);
        when(scheduleMapperMock.toScheduleDto(any())).thenReturn(savedScheduleDto);

        // expect
        mockMvc.perform(post("/api/v1/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedScheduleDto.getId()))
                .andExpect(jsonPath("$.weekday").value(savedScheduleDto.getWeekday()))
                .andExpect(jsonPath("$.startTime").value(savedScheduleDto.getStartTime()))
                .andExpect(jsonPath("$.endTime").value(savedScheduleDto.getEndTime()))
                .andExpect(header().string("Location", "/schedule"))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    ScheduleDto actualScheduleDto = objectMapper.readValue(response, ScheduleDto.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualScheduleDto).isNotNull();
                    softly.assertThat(actualScheduleDto.getId()).isEqualTo(savedScheduleDto.getId());
                    softly.assertThat(actualScheduleDto.getWeekday()).isEqualTo(savedScheduleDto.getWeekday());
                    softly.assertThat(actualScheduleDto.getStartTime()).isEqualTo(savedScheduleDto.getStartTime());
                    softly.assertThat(actualScheduleDto.getEndTime()).isEqualTo(savedScheduleDto.getEndTime());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreateWhenDefaultScheduleIsAlreadyExists() throws Exception {
        // given
        Long scheduleId = 1L;

        ScheduleCreateDto dto = ScheduleCreateDto.builder()
                .weekday(1)
                .startTime("11:00")
                .endTime("18:00")
                .build();

        Schedule schedule = Schedule.builder()
                .weekday(dto.getWeekday())
                .startTime(11 * 60)
                .endTime(18 * 60)
                .build();

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                SCHEDULE_CREATED_EXCEPTION_MESSAGE,
                new Object[]{scheduleId},
                locale
        );

        when(scheduleMapperMock.toSchedule(any(ScheduleCreateDto.class))).thenReturn(schedule);
        doThrow(new ScheduleIsAlreadyCreatedException(message)).when(scheduleServiceMock).edit(eq(scheduleId),
                eq(schedule));

        ErrorResponse expectedResponseBody = new ErrorResponse(400, message);

        // expect
        mockMvc.perform(put("/api/v1/schedule/{id}", scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
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
    public void testCreateWithDateWhenWeekdayIsNotEmpty() throws Exception {
        // given
        ScheduleCreateDto scheduleCreateDto = ScheduleCreateDto.builder()
                .startTime("11:00")
                .endTime("18:00")
                .date(OffsetDateTime.now())
                .weekday(2)
                .build();

        String message = messageSource.getMessage(
                SCHEDULE_DATE_NOT_EMPTY_EXCEPTION_MESSAGE,
                null,
                new Locale("ru")
        );
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleCreateDto)))
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
    public void testCreateWithWeekdayMoreThan7() throws Exception {
        // given
        ScheduleCreateDto scheduleCreateDto = ScheduleCreateDto.builder()
                .weekday(8)
                .startTime("11:00")
                .endTime("18:00")
                .build();

        String message = messageSource.getMessage(
                SCHEDULE_WEEKDAY_BOUNDARY_EXCEPTION_MESSAGE,
                null,
                new Locale("ru")
        );
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleCreateDto)))
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
    public void testCreateWithWorkdayIsEmptyAndSoAreTimes() throws Exception {
        // given
        ScheduleCreateDto scheduleCreateDto = ScheduleCreateDto.builder()
                .weekday(5)
                .build();

        String message1 = messageSource.getMessage(
                SCHEDULE_START_TIME_EMPTY_EXCEPTION_MESSAGE,
                null,
                new Locale("ru")
        );
        String message2 = messageSource.getMessage(
                SCHEDULE_END_TIME_EMPTY_EXCEPTION_MESSAGE,
                null,
                new Locale("ru")
        );

        // expect
        mockMvc.perform(post("/api/v1/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andDo(result -> {

                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    ErrorResponse actualResponseBody = objectMapper.readValue(response, ErrorResponse.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualResponseBody).isNotNull();
                    softly.assertThat(actualResponseBody.getStatus()).isEqualTo(400);
                    softly.assertThat(actualResponseBody.getMessage().split(", ")).containsExactlyInAnyOrder(
                            "\"" + message1 + "\"",
                            "\"" + message2 + "\""
                    );
                    ;
                    softly.assertAll();
                });
    }

    @Test
    public void testCreateWithWorkdayIsNotEmptyAndSoAreTimes() throws Exception {
        // given
        ScheduleCreateDto scheduleCreateDto = ScheduleCreateDto.builder()
                .weekday(5)
                .isWorkday(true)
                .startTime("11:00")
                .endTime("12:00")
                .build();

        String message1 = messageSource.getMessage(
                SCHEDULE_START_TIME_NOT_EMPTY_EXCEPTION_MESSAGE,
                null,
                new Locale("ru")
        );
        String message2 = messageSource.getMessage(
                SCHEDULE_END_TIME_NOT_EMPTY_EXCEPTION_MESSAGE,
                null,
                new Locale("ru")
        );

        // expect
        mockMvc.perform(post("/api/v1/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andDo(result -> {

                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    ErrorResponse actualResponseBody = objectMapper.readValue(response, ErrorResponse.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualResponseBody).isNotNull();
                    softly.assertThat(actualResponseBody.getStatus()).isEqualTo(400);
                    softly.assertThat(actualResponseBody.getMessage().split(", ")).containsExactlyInAnyOrder(
                            "\"" + message1 + "\"",
                            "\"" + message2 + "\""
                    );
                    softly.assertAll();
                });
    }

    @Test
    public void testCreateWitStartTimeIsNotValid() throws Exception {
        // given
        ScheduleCreateDto scheduleCreateDto = ScheduleCreateDto.builder()
                .weekday(5)
                .startTime("11:0")
                .endTime("12:00")
                .build();

        String message = messageSource.getMessage(
                SCHEDULE_START_TIME_NOT_VALID_EXCEPTION_MESSAGE,
                null,
                new Locale("ru")
        );
        ErrorResponse expectedResponseBody = new ErrorResponse(
                400,
                "\"" + message + "\""
        );

        // expect
        mockMvc.perform(post("/api/v1/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleCreateDto)))
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
    public void testCreateWitEndTimeIsNotValid() throws Exception {
        // given
        ScheduleCreateDto scheduleCreateDto = ScheduleCreateDto.builder()
                .weekday(5)
                .startTime("11:00")
                .endTime("blabla")
                .build();

        String message = messageSource.getMessage(
                SCHEDULE_END_TIME_NOT_VALID_EXCEPTION_MESSAGE,
                null,
                new Locale("ru")
        );
        ErrorResponse expectedResponseBody = new ErrorResponse(
                400,
                "\"" + message + "\""
        );

        // expect
        mockMvc.perform(post("/api/v1/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleCreateDto)))
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
    public void testCreateWitStartTimeIsNotValid2() throws Exception {
        // given
        ScheduleCreateDto scheduleCreateDto = ScheduleCreateDto.builder()
                .weekday(5)
                .startTime("11:rg")
                .endTime("12:00")
                .build();

        String message = messageSource.getMessage(
                SCHEDULE_START_TIME_NOT_VALID_EXCEPTION_MESSAGE,
                null,
                new Locale("ru")
        );
        ErrorResponse expectedResponseBody = new ErrorResponse(
                400,
                "\"" + message + "\""
        );

        // expect
        mockMvc.perform(post("/api/v1/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleCreateDto)))
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
    public void testCreateWitEndTimeIsNotValid2() throws Exception {
        // given
        ScheduleCreateDto scheduleCreateDto = ScheduleCreateDto.builder()
                .weekday(5)
                .startTime("11:00")
                .endTime("60:60")
                .build();

        String message = messageSource.getMessage(
                SCHEDULE_END_TIME_NOT_VALID_EXCEPTION_MESSAGE,
                null,
                new Locale("ru")
        );
        ErrorResponse expectedResponseBody = new ErrorResponse(
                400,
                "\"" + message + "\""
        );

        // expect
        mockMvc.perform(post("/api/v1/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleCreateDto)))
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
    public void testDeleteExistingSchedule() throws Exception {
        // given
        Long scheduleId = 1L;

        doNothing().when(scheduleServiceMock).delete(scheduleId);

        // expect
        mockMvc.perform(delete("/api/v1/schedule/{id}", scheduleId))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteNonExistingSchedule() throws Exception {
        // given
        Long scheduleId = 1L;

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                SCHEDULE_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{scheduleId},
                locale
        );

        doThrow(new ResourceNotFoundException(message)).when(scheduleServiceMock).delete(scheduleId);

        ErrorResponse expectedResponseBody = new ErrorResponse(404, message);

        // expect
        mockMvc.perform(delete("/api/v1/schedule/{id}", scheduleId))
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
    public void testDeleteDefaultSchedule() throws Exception {
        // given
        Long scheduleId = 1L;

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                SCHEDULE_DELETE_EXCEPTION_MESSAGE,
                new Object[]{scheduleId},
                locale
        );

        doThrow(new CannotDeleteScheduleException(message)).when(scheduleServiceMock).delete(scheduleId);

        ErrorResponse expectedResponseBody = new ErrorResponse(400, message);

        // expect
        mockMvc.perform(delete("/api/v1/schedule/{id}", scheduleId))
                .andExpect(status().isBadRequest())
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
    public void testEditExistingSchedule() throws Exception {
        // given
        Long scheduleId = 1L;

        ScheduleCreateDto dto = ScheduleCreateDto.builder()
                .date(OffsetDateTime.now())
                .startTime("11:00")
                .endTime("18:00")
                .build();

        Schedule updatedSchedule = Schedule.builder()
                .date(dto.getDate())
                .startTime(11 * 60)
                .endTime(18 * 60)
                .build();

        Schedule editedSchedule = Schedule.builder()
                .id(1L)
                .date(updatedSchedule.getDate())
                .startTime(updatedSchedule.getStartTime())
                .endTime(updatedSchedule.getEndTime())
                .build();

        ScheduleDto editedScheduleDto = ScheduleDto.builder()
                .id(editedSchedule.getId())
                .date(editedSchedule.getDate())
                .startTime("11:00")
                .endTime("18:00")
                .build();

        when(scheduleMapperMock.toSchedule(any(ScheduleCreateDto.class))).thenReturn(updatedSchedule);
        when(scheduleServiceMock.edit(eq(scheduleId), eq(updatedSchedule))).thenReturn(editedSchedule);
        when(scheduleMapperMock.toScheduleDto(eq(editedSchedule))).thenReturn(editedScheduleDto);

        // when
        mockMvc.perform(put("/api/v1/schedule/{id}", scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(editedScheduleDto.getId()))
                .andExpect(jsonPath("$.startTime").value(editedScheduleDto.getStartTime()))
                .andExpect(jsonPath("$.endTime").value(editedScheduleDto.getEndTime()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    ScheduleDto actualScheduleDto = objectMapper.readValue(response,
                            ScheduleDto.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualScheduleDto).isNotNull();
                    softly.assertThat(actualScheduleDto.getId()).isEqualTo(editedScheduleDto.getId());
                    softly.assertThat(actualScheduleDto.getDate()).isEqualTo(editedScheduleDto.getDate());
                    softly.assertThat(actualScheduleDto.getStartTime()).isEqualTo(editedScheduleDto.getStartTime());
                    softly.assertThat(actualScheduleDto.getEndTime()).isEqualTo(editedScheduleDto.getEndTime());
                    softly.assertAll();
                });
    }

    @Test
    public void testEditNonExistingSchedule() throws Exception {
        // given
        Long scheduleId = 1L;

        ScheduleCreateDto dto = ScheduleCreateDto.builder()
                .weekday(3)
                .startTime("11:00")
                .endTime("18:00")
                .build();

        Schedule updatedSchedule = Schedule.builder()
                .weekday(dto.getWeekday())
                .startTime(11 * 60)
                .endTime(18 * 60)
                .build();

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                SCHEDULE_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{scheduleId},
                locale
        );

        when(scheduleMapperMock.toSchedule(any(ScheduleCreateDto.class))).thenReturn(updatedSchedule);
        doThrow(new ResourceNotFoundException(message)).when(scheduleServiceMock).edit(eq(scheduleId),
                eq(updatedSchedule));

        ErrorResponse expectedResponseBody = new ErrorResponse(404, message);

        // expect
        mockMvc.perform(put("/api/v1/schedule/{id}", scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
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
