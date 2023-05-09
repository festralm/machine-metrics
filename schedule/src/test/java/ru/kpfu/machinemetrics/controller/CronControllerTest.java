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
import ru.kpfu.machinemetrics.dto.CronCreateDto;
import ru.kpfu.machinemetrics.dto.ErrorResponse;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.mapper.CronMapper;
import ru.kpfu.machinemetrics.model.Cron;
import ru.kpfu.machinemetrics.service.CronService;

import java.nio.charset.StandardCharsets;
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
import static ru.kpfu.machinemetrics.constants.CronConstants.CRON_NOT_FOUND_EXCEPTION_MESSAGE;

@WebMvcTest(CronController.class)
@ImportAutoConfiguration(MessageSourceConfig.class)
public class CronControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CronMapper cronMapperMock;

    @MockBean
    private CronService cronServiceMock;

    @Test
    public void testListAll() throws Exception {
        // given
        Cron cron1 = Cron.builder()
                .id(1L)
                .expression("1 * * * * ?")
                .order(1)
                .name("Cron 1")
                .build();
        Cron cron2 = Cron.builder()
                .id(2L)
                .expression("2 * * * * ?")
                .order(2)
                .name("Cron 2")
                .build();
        List<Cron> cronList = List.of(cron1, cron2);

        when(cronServiceMock.getAll()).thenReturn(cronList);

        // expect
        mockMvc.perform(get("/api/v1/cron"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(cron1.getId()))
                .andExpect(jsonPath("$[0].expression").value(cron1.getExpression()))
                .andExpect(jsonPath("$[0].order").value(cron1.getOrder()))
                .andExpect(jsonPath("$[0].name").value(cron1.getName()))
                .andExpect(jsonPath("$[1].id").value(cron2.getId()))
                .andExpect(jsonPath("$[1].expression").value(cron2.getExpression()))
                .andExpect(jsonPath("$[1].order").value(cron2.getOrder()))
                .andExpect(jsonPath("$[1].name").value(cron2.getName()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    List<Cron> actualList = Arrays.asList(objectMapper.readValue(response, Cron[].class));

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualList).isNotNull();
                    softly.assertThat(actualList).hasSize(2);
                    softly.assertThat(actualList.get(0).getId()).isEqualTo(cron1.getId());
                    softly.assertThat(actualList.get(0).getExpression()).isEqualTo(cron1.getExpression());
                    softly.assertThat(actualList.get(0).getOrder()).isEqualTo(cron1.getOrder());
                    softly.assertThat(actualList.get(0).getName()).isEqualTo(cron1.getName());
                    softly.assertThat(actualList.get(1).getId()).isEqualTo(cron2.getId());
                    softly.assertThat(actualList.get(1).getExpression()).isEqualTo(cron2.getExpression());
                    softly.assertThat(actualList.get(1).getOrder()).isEqualTo(cron2.getOrder());
                    softly.assertThat(actualList.get(1).getName()).isEqualTo(cron2.getName());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreate() throws Exception {
        // given
        CronCreateDto cronCreateDto = CronCreateDto.builder()
                .expression("1 * * * * ?")
                .order(1)
                .name("Cron")
                .build();

        Cron cron = Cron.builder()
                .expression(cronCreateDto.getExpression())
                .order(cronCreateDto.getOrder())
                .name(cronCreateDto.getName())
                .build();

        Cron savedCron = Cron.builder()
                .id(1L)
                .expression(cronCreateDto.getExpression())
                .order(cronCreateDto.getOrder())
                .name(cronCreateDto.getName())
                .build();

        when(cronMapperMock.toCron(any())).thenReturn(cron);
        when(cronServiceMock.save(any())).thenReturn(savedCron);

        // expect
        mockMvc.perform(post("/api/v1/cron")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cronCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedCron.getId()))
                .andExpect(jsonPath("$.expression").value(savedCron.getExpression()))
                .andExpect(jsonPath("$.order").value(savedCron.getOrder()))
                .andExpect(jsonPath("$.name").value(savedCron.getName()))
                .andExpect(header().string("Location", "/cron"))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    Cron actualCronCreate = objectMapper.readValue(response, Cron.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualCronCreate).isNotNull();
                    softly.assertThat(actualCronCreate.getId()).isEqualTo(savedCron.getId());
                    softly.assertThat(actualCronCreate.getExpression()).isEqualTo(savedCron.getExpression());
                    softly.assertThat(actualCronCreate.getOrder()).isEqualTo(savedCron.getOrder());
                    softly.assertThat(actualCronCreate.getName()).isEqualTo(savedCron.getName());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreateWithEmptyName() throws Exception {
        // given
        CronCreateDto cronCreateDto = CronCreateDto.builder()
                .expression("1 * * * * ?")
                .order(1)
                .name("")
                .build();

        String message = messageSource.getMessage("validation.cron.name.empty", null, new Locale("ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/cron")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cronCreateDto)))
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
    public void testCreateWithEmptyId() throws Exception {
        // given
        CronCreateDto cronCreateDto = CronCreateDto.builder()
                .expression("")
                .order(1)
                .name("Name 1")
                .build();

        Cron cron = Cron.builder()
                .expression(cronCreateDto.getExpression())
                .order(cronCreateDto.getOrder())
                .name(cronCreateDto.getName())
                .build();

        String message = messageSource.getMessage("validation.cron.empty", null, new Locale("ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/cron")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cronCreateDto)))
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
    public void testCreateWithEmptyOrder() throws Exception {
        // given
        CronCreateDto cronCreateDto = CronCreateDto.builder()
                .expression("1 * * * * ?")
                .name("Name 2")
                .build();

        String message = messageSource.getMessage("validation.cron.order.empty", null, new Locale("ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/cron")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cronCreateDto)))
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
    public void testDeleteExistingCron() throws Exception {
        // given
        Long cronId = 1L;
        doNothing().when(cronServiceMock).delete(cronId);

        // expect
        mockMvc.perform(delete("/api/v1/cron/{id}", cronId))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteNonExistingCron() throws Exception {
        // given
        Long cronId = 1L;

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                CRON_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{cronId},
                locale
        );

        doThrow(new ResourceNotFoundException(message)).when(cronServiceMock).delete(cronId);

        ErrorResponse expectedResponseBody = new ErrorResponse(404, message);

        // expect
        mockMvc.perform(delete("/api/v1/cron/{id}", cronId))
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
    public void testEdit() throws Exception {
        // given
        Long cronId = 1L;

        CronCreateDto cronCreateDto = CronCreateDto.builder()
                .expression("1 * * * * ?")
                .order(1)
                .name("Cron")
                .build();

        Cron cron = Cron.builder()
                .expression(cronCreateDto.getExpression())
                .order(cronCreateDto.getOrder())
                .name(cronCreateDto.getName())
                .build();

        Cron savedCron = Cron.builder()
                .id(cronId)
                .expression(cronCreateDto.getExpression())
                .order(cronCreateDto.getOrder())
                .name(cronCreateDto.getName())
                .build();

        when(cronMapperMock.toCron(any(CronCreateDto.class))).thenReturn(cron);
        when(cronServiceMock.edit(eq(cronId), any(Cron.class))).thenReturn(savedCron);

        // expect
        mockMvc.perform(put("/api/v1/cron/{id}", cronId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cronCreateDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedCron.getId()))
                .andExpect(jsonPath("$.expression").value(savedCron.getExpression()))
                .andExpect(jsonPath("$.order").value(savedCron.getOrder()))
                .andExpect(jsonPath("$.name").value(savedCron.getName()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    Cron actualCronCreate = objectMapper.readValue(response, Cron.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualCronCreate).isNotNull();
                    softly.assertThat(actualCronCreate.getId()).isEqualTo(savedCron.getId());
                    softly.assertThat(actualCronCreate.getExpression()).isEqualTo(savedCron.getExpression());
                    softly.assertThat(actualCronCreate.getOrder()).isEqualTo(savedCron.getOrder());
                    softly.assertThat(actualCronCreate.getName()).isEqualTo(savedCron.getName());
                    softly.assertAll();
                });
    }

    @Test
    public void testEditNonExistingCron() throws Exception {
        // given
        Long cronId = 1L;

        CronCreateDto cronCreateDto = CronCreateDto.builder()
                .expression("1 * * * * ?")
                .order(1)
                .name("Cron")
                .build();

        Cron cron = Cron.builder()
                .expression(cronCreateDto.getExpression())
                .order(cronCreateDto.getOrder())
                .name(cronCreateDto.getName())
                .build();

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                CRON_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{cronId},
                locale
        );

        when(cronMapperMock.toCron(any(CronCreateDto.class))).thenReturn(cron);
        doThrow(new ResourceNotFoundException(message)).when(cronServiceMock).edit(eq(cronId), any(Cron.class));

        ErrorResponse expectedResponseBody = new ErrorResponse(404, message);

        // expect
        mockMvc.perform(put("/api/v1/cron/{id}", cronId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cronCreateDto)))
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
