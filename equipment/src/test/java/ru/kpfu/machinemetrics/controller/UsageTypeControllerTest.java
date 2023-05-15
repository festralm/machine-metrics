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
import ru.kpfu.machinemetrics.model.UsageType;
import ru.kpfu.machinemetrics.service.UsageTypeService;

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
import static ru.kpfu.machinemetrics.constants.UsageTypeConstants.USAGE_TYPE_NOT_FOUND_EXCEPTION_MESSAGE;

@WebMvcTest(UsageTypeController.class)
@ImportAutoConfiguration(MessageSourceConfig.class)
public class UsageTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsageTypeService usageTypeService;

    @Test
    public void testListAll() throws Exception {
        // given
        UsageType usageType1 = UsageType.builder()
                .id(1L)
                .name("UsageType 1")
                .build();
        UsageType usageType2 = UsageType.builder()
                .id(2L)
                .name("UsageType 2")
                .build();
        List<UsageType> usageTypeList = List.of(usageType1, usageType2);

        when(usageTypeService.getAll()).thenReturn(usageTypeList);

        // expect
        mockMvc.perform(get("/api/v1/usage-type"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(usageType1.getId()))
                .andExpect(jsonPath("$[0].name").value(usageType1.getName()))
                .andExpect(jsonPath("$[1].id").value(usageType2.getId()))
                .andExpect(jsonPath("$[1].name").value(usageType2.getName()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    List<UsageType> actualList = Arrays.asList(objectMapper.readValue(response,
                            UsageType[].class));

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualList).isNotNull();
                    softly.assertThat(actualList).hasSize(2);
                    softly.assertThat(actualList.get(0).getName()).isEqualTo(usageType1.getName());
                    softly.assertThat(actualList.get(1).getName()).isEqualTo(usageType2.getName());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreate() throws Exception {
        // given
        UsageType usageTypeCreate = UsageType.builder()
                .name("name")
                .build();

        UsageType savedUsageType = UsageType.builder()
                .id(1L)
                .name(usageTypeCreate.getName())
                .build();

        when(usageTypeService.save(any())).thenReturn(savedUsageType);

        // expect
        mockMvc.perform(post("/api/v1/usage-type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usageTypeCreate)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedUsageType.getId()))
                .andExpect(jsonPath("$.name").value(savedUsageType.getName()))
                .andExpect(header().string("Location", "/usage-type"))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    UsageType actualUsageTypeCreate = objectMapper.readValue(response, UsageType.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualUsageTypeCreate).isNotNull();
                    softly.assertThat(actualUsageTypeCreate.getId()).isEqualTo(savedUsageType.getId());
                    softly.assertThat(actualUsageTypeCreate.getName()).isEqualTo(savedUsageType.getName());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreateWithEmptyName() throws Exception {
        // given
        UsageType usageTypeCreate = UsageType.builder()
                .name("")
                .build();

        String message = messageSource.getMessage("validation.usage-type.name.empty", null, new Locale("ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/usage-type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usageTypeCreate)))
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
    public void testDeleteExistingUsageType() throws Exception {
        // given
        Long usageTypeId = 1L;
        doNothing().when(usageTypeService).delete(usageTypeId);

        // expect
        mockMvc.perform(delete("/api/v1/usage-type/{id}", usageTypeId))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteNonExistingUsageType() throws Exception {
        // given
        Long usageTypeId = 1L;

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                USAGE_TYPE_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{usageTypeId},
                locale
        );

        doThrow(new ResourceNotFoundException(message)).when(usageTypeService).delete(usageTypeId);

        ErrorResponse expectedResponseBody = new ErrorResponse(404, message);

        // expect
        mockMvc.perform(delete("/api/v1/usage-type/{id}", usageTypeId))
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
