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
import ru.kpfu.machinemetrics.model.Purpose;
import ru.kpfu.machinemetrics.service.PurposeService;

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
import static ru.kpfu.machinemetrics.constants.PurposeConstants.PURPOSE_NOT_FOUND_EXCEPTION_MESSAGE;

@WebMvcTest(PurposeController.class)
@ImportAutoConfiguration(MessageSourceConfig.class)
public class PurposeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PurposeService purposeService;

    @Test
    public void testListAll() throws Exception {
        // given
        Purpose purpose1 = Purpose.builder()
                .id(1L)
                .name("Purpose 1")
                .build();
        Purpose purpose2 = Purpose.builder()
                .id(2L)
                .name("Purpose 2")
                .build();
        List<Purpose> purposeList = List.of(purpose1, purpose2);

        when(purposeService.getAll()).thenReturn(purposeList);

        // expect
        mockMvc.perform(get("/api/v1/purpose"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(purpose1.getId()))
                .andExpect(jsonPath("$[0].name").value(purpose1.getName()))
                .andExpect(jsonPath("$[1].id").value(purpose2.getId()))
                .andExpect(jsonPath("$[1].name").value(purpose2.getName()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    List<Purpose> actualList = Arrays.asList(objectMapper.readValue(response,
                            Purpose[].class));

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualList).isNotNull();
                    softly.assertThat(actualList).hasSize(2);
                    softly.assertThat(actualList.get(0).getName()).isEqualTo(purpose1.getName());
                    softly.assertThat(actualList.get(1).getName()).isEqualTo(purpose2.getName());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreate() throws Exception {
        // given
        Purpose purposeCreate = Purpose.builder()
                .name("name")
                .build();

        Purpose savedPurpose = Purpose.builder()
                .id(1L)
                .name(purposeCreate.getName())
                .build();

        when(purposeService.save(any())).thenReturn(savedPurpose);

        // expect
        mockMvc.perform(post("/api/v1/purpose")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purposeCreate)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedPurpose.getId()))
                .andExpect(jsonPath("$.name").value(savedPurpose.getName()))
                .andExpect(header().string("Location", "/purpose"))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    Purpose actualPurposeCreate = objectMapper.readValue(response, Purpose.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualPurposeCreate).isNotNull();
                    softly.assertThat(actualPurposeCreate.getId()).isEqualTo(savedPurpose.getId());
                    softly.assertThat(actualPurposeCreate.getName()).isEqualTo(savedPurpose.getName());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreateWithEmptyName() throws Exception {
        // given
        Purpose purposeCreate = Purpose.builder()
                .name("")
                .build();

        String message = messageSource.getMessage("validation.purpose.name.empty", null, new Locale("ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/purpose")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purposeCreate)))
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
    public void testDeleteExistingPurpose() throws Exception {
        // given
        Long purposeId = 1L;
        doNothing().when(purposeService).delete(purposeId);

        // expect
        mockMvc.perform(delete("/api/v1/purpose/{id}", purposeId))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteNonExistingPurpose() throws Exception {
        // given
        Long purposeId = 1L;

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                PURPOSE_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{purposeId},
                locale
        );

        doThrow(new ResourceNotFoundException(message)).when(purposeService).delete(purposeId);

        ErrorResponse expectedResponseBody = new ErrorResponse(404, message);

        // expect
        mockMvc.perform(delete("/api/v1/purpose/{id}", purposeId))
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
