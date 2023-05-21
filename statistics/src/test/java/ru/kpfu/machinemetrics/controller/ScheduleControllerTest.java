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
import static ru.kpfu.machinemetrics.constants.ScheduleConstants.SCHEDULE_CREATED_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.ScheduleConstants.SCHEDULE_DELETE_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.ScheduleConstants.SCHEDULE_NOT_FOUND_EXCEPTION_MESSAGE;

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
                .startTime("11:00")
                .endTime("18:00")
                .date(Instant.now())
                .build();
        Schedule schedule2 = Schedule.builder()
                .id(2L)
                .startTime("12:00")
                .endTime("19:00")
                .date(Instant.now())
                .build();
        List<Schedule> scheduleList = List.of(schedule1, schedule2);

        ScheduleDto scheduleDto1 = ScheduleDto.builder()
                .id(schedule1.getId())
                .startTime(schedule1.getStartTime())
                .endTime(schedule1.getEndTime())
                .date(schedule1.getDate())
                .build();

        ScheduleDto scheduleDto2 = ScheduleDto.builder()
                .id(schedule2.getId())
                .startTime(schedule2.getStartTime())
                .endTime(schedule2.getEndTime())
                .date(schedule2.getDate())
                .build();
        List<ScheduleDto> scheduleDtoList = List.of(scheduleDto1, scheduleDto2);

        when(scheduleServiceMock.getAll()).thenReturn(scheduleList);
        when(scheduleMapperMock.toScheduleDtos(scheduleList)).thenReturn(scheduleDtoList);

        // expect
        mockMvc.perform(get("/api/v1/schedule"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(scheduleDto1.getId()))
                .andExpect(jsonPath("$[0].startTime").value(scheduleDto1.getStartTime()))
                .andExpect(jsonPath("$[0].endTime").value(scheduleDto1.getEndTime()))
                .andExpect(jsonPath("$[1].id").value(scheduleDto2.getId()))
                .andExpect(jsonPath("$[1].startTime").value(scheduleDto2.getStartTime()))
                .andExpect(jsonPath("$[1].endTime").value(scheduleDto2.getEndTime()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    List<Schedule> actualList = Arrays.asList(objectMapper.readValue(response,
                            Schedule[].class));

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualList).isNotNull();
                    softly.assertThat(actualList).hasSize(2);
                    softly.assertThat(actualList.get(0).getId()).isEqualTo(scheduleDto1.getId());
                    softly.assertThat(actualList.get(0).getStartTime()).isEqualTo(scheduleDto1.getStartTime());
                    softly.assertThat(actualList.get(0).getEndTime()).isEqualTo(scheduleDto1.getEndTime());
                    softly.assertThat(actualList.get(0).getDate()).isEqualTo(scheduleDto1.getDate());
                    softly.assertThat(actualList.get(1).getId()).isEqualTo(scheduleDto2.getId());
                    softly.assertThat(actualList.get(1).getStartTime()).isEqualTo(scheduleDto2.getStartTime());
                    softly.assertThat(actualList.get(1).getEndTime()).isEqualTo(scheduleDto2.getEndTime());
                    softly.assertThat(actualList.get(1).getDate()).isEqualTo(scheduleDto2.getDate());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreate() throws Exception {
        // given
        ScheduleCreateDto dto = ScheduleCreateDto.builder()
                .startTime("11:00")
                .endTime("18:00")
                .date(Instant.now())
                .build();

        Schedule schedule = Schedule.builder()
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .date(dto.getDate())
                .build();

        Schedule savedSchedule = Schedule.builder()
                .id(1L)
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .date(schedule.getDate())
                .build();

        ScheduleDto savedScheduleDto = ScheduleDto.builder()
                .id(savedSchedule.getId())
                .startTime(savedSchedule.getStartTime())
                .endTime(savedSchedule.getEndTime())
                .date(savedSchedule.getDate())
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
                .andExpect(jsonPath("$.startTime").value(savedScheduleDto.getStartTime()))
                .andExpect(jsonPath("$.endTime").value(savedScheduleDto.getEndTime()))
                .andExpect(header().string("Location", "/schedule"))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    Schedule actualScheduleCreate = objectMapper.readValue(response, Schedule.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualScheduleCreate).isNotNull();
                    softly.assertThat(actualScheduleCreate.getId()).isEqualTo(savedScheduleDto.getId());
                    softly.assertThat(actualScheduleCreate.getStartTime()).isEqualTo(savedScheduleDto.getStartTime());
                    softly.assertThat(actualScheduleCreate.getEndTime()).isEqualTo(savedScheduleDto.getEndTime());
                    softly.assertThat(actualScheduleCreate.getDate()).isEqualTo(savedScheduleDto.getDate());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreateWhenDefaultScheduleIsAlreadyExists() throws Exception {
        // given
        Long scheduleId = 1L;

        ScheduleCreateDto dto = ScheduleCreateDto.builder()
                .startTime("11:00")
                .endTime("18:00")
                .build();

        Schedule updatedSchedule = Schedule.builder()
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                SCHEDULE_CREATED_EXCEPTION_MESSAGE,
                new Object[]{scheduleId},
                locale
        );

        when(scheduleMapperMock.toSchedule(any(ScheduleCreateDto.class))).thenReturn(updatedSchedule);
        doThrow(new ScheduleIsAlreadyCreatedException(message)).when(scheduleServiceMock).edit(eq(scheduleId),
                eq(updatedSchedule));

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
    public void testCreateWithEmptyStartTime() throws Exception {
        // given
        ScheduleCreateDto scheduleCreateDto = ScheduleCreateDto.builder()
                .endTime("18:00")
                .date(Instant.now())
                .build();

        String message = messageSource.getMessage("validation.schedule.start-time.empty", null, new Locale("ru"));
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
    public void testCreateWithEmptyEndTime() throws Exception {
        // given
        ScheduleCreateDto scheduleCreateDto = ScheduleCreateDto.builder()
                .startTime("11:00")
                .endTime("")
                .date(Instant.now())
                .build();

        String message = messageSource.getMessage("validation.schedule.end-time.empty", null, new Locale("ru"));
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
                .startTime("11:00")
                .endTime("18:00")
                .date(Instant.now())
                .build();

        Schedule updatedSchedule = Schedule.builder()
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .date(dto.getDate())
                .build();

        Schedule editedSchedule = Schedule.builder()
                .id(1L)
                .startTime(updatedSchedule.getStartTime())
                .endTime(updatedSchedule.getEndTime())
                .date(updatedSchedule.getDate())
                .build();

        ScheduleDto editedScheduleDto = ScheduleDto.builder()
                .id(editedSchedule.getId())
                .startTime(editedSchedule.getStartTime())
                .endTime(editedSchedule.getEndTime())
                .date(editedSchedule.getDate())
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
                    softly.assertThat(actualScheduleDto.getStartTime()).isEqualTo(editedScheduleDto.getStartTime());
                    softly.assertThat(actualScheduleDto.getEndTime()).isEqualTo(editedScheduleDto.getEndTime());
                    softly.assertThat(actualScheduleDto.getDate()).isEqualTo(editedScheduleDto.getDate());
                    softly.assertAll();
                });
    }

    @Test
    public void testEditNonExistingSchedule() throws Exception {
        // given
        Long scheduleId = 1L;

        ScheduleCreateDto dto = ScheduleCreateDto.builder()
                .startTime("11:00")
                .endTime("18:00")
                .date(Instant.now())
                .build();

        Schedule updatedSchedule = Schedule.builder()
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .date(dto.getDate())
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
