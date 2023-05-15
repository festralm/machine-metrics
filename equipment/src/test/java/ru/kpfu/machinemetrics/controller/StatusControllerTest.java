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
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.model.Status;
import ru.kpfu.machinemetrics.service.StatusService;

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
import static ru.kpfu.machinemetrics.constants.StatusConstants.STATUS_NOT_FOUND_EXCEPTION_MESSAGE;

@WebMvcTest(StatusController.class)
@ImportAutoConfiguration(MessageSourceConfig.class)
public class StatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StatusService statusService;

    @Test
    public void testListAll() throws Exception {
        // given
        Status status1 = Status.builder()
                .id(1L)
                .name("Status 1")
                .build();
        Status status2 = Status.builder()
                .id(2L)
                .name("Status 2")
                .build();
        List<Status> statusList = List.of(status1, status2);

        when(statusService.getAll()).thenReturn(statusList);

        // expect
        mockMvc.perform(get("/api/v1/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(status1.getId()))
                .andExpect(jsonPath("$[0].name").value(status1.getName()))
                .andExpect(jsonPath("$[1].id").value(status2.getId()))
                .andExpect(jsonPath("$[1].name").value(status2.getName()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    List<Status> actualList = Arrays.asList(objectMapper.readValue(response,
                            Status[].class));

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualList).isNotNull();
                    softly.assertThat(actualList).hasSize(2);
                    softly.assertThat(actualList.get(0).getName()).isEqualTo(status1.getName());
                    softly.assertThat(actualList.get(1).getName()).isEqualTo(status2.getName());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreate() throws Exception {
        // given
        Status statusCreate = Status.builder()
                .name("name")
                .build();

        Status savedStatus = Status.builder()
                .id(1L)
                .name(statusCreate.getName())
                .build();

        when(statusService.save(any())).thenReturn(savedStatus);

        // expect
        mockMvc.perform(post("/api/v1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusCreate)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedStatus.getId()))
                .andExpect(jsonPath("$.name").value(savedStatus.getName()))
                .andExpect(header().string("Location", "/status"))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    Status actualStatusCreate = objectMapper.readValue(response, Status.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualStatusCreate).isNotNull();
                    softly.assertThat(actualStatusCreate.getId()).isEqualTo(savedStatus.getId());
                    softly.assertThat(actualStatusCreate.getName()).isEqualTo(savedStatus.getName());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreateWithEmptyName() throws Exception {
        // given
        Status statusCreate = Status.builder()
                .name("")
                .build();

        String message = messageSource.getMessage("validation.status.name.empty", null, new Locale("ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusCreate)))
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
    public void testDeleteExistingStatus() throws Exception {
        // given
        Long statusId = 1L;
        doNothing().when(statusService).delete(statusId);

        // expect
        mockMvc.perform(delete("/api/v1/status/{id}", statusId))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteNonExistingStatus() throws Exception {
        // given
        Long statusId = 1L;

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                STATUS_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{statusId},
                locale
        );

        doThrow(new ResourceNotFoundException(message)).when(statusService).delete(statusId);

        ErrorResponse expectedResponseBody = new ErrorResponse(404, message);

        // expect
        mockMvc.perform(delete("/api/v1/status/{id}", statusId))
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
