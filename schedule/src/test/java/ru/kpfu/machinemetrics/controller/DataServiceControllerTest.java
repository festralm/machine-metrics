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
import ru.kpfu.machinemetrics.configuration.MessageSourceConfig;
import ru.kpfu.machinemetrics.dto.ErrorResponse;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.model.DataService;
import ru.kpfu.machinemetrics.service.DataServiceService;

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
import static ru.kpfu.machinemetrics.constants.DataServiceConstants.DATA_SERVICE_NOT_FOUND_EXCEPTION_MESSAGE;

@WebMvcTest(DataServiceController.class)
@ImportAutoConfiguration(MessageSourceConfig.class)
public class DataServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DataServiceService dataServiceService;

    @Test
    public void testListAll() throws Exception {
        // given
        DataService dataService1 = DataService.builder()
                .id(0L)
                .name("DataService 1")
                .build();
        DataService dataService2 = DataService.builder()
                .id(1L)
                .name("DataService 2")
                .build();
        List<DataService> dataServiceList = List.of(dataService1, dataService2);

        when(dataServiceService.getAll()).thenReturn(dataServiceList);

        // expect
        mockMvc.perform(get("/api/v1/data-service"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(dataService1.getId()))
                .andExpect(jsonPath("$[0].name").value(dataService1.getName()))
                .andExpect(jsonPath("$[1].id").value(dataService2.getId()))
                .andExpect(jsonPath("$[1].name").value(dataService2.getName()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    List<DataService> actualList = Arrays.asList(objectMapper.readValue(response,
                            DataService[].class));

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualList).isNotNull();
                    softly.assertThat(actualList).hasSize(2);
                    softly.assertThat(actualList.get(0).getName()).isEqualTo(dataService1.getName());
                    softly.assertThat(actualList.get(1).getName()).isEqualTo(dataService2.getName());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreate() throws Exception {
        // given
        DataService dataServiceCreate = DataService.builder()
                .name("name")
                .build();

        DataService savedDataService = DataService.builder()
                .id(1L)
                .name(dataServiceCreate.getName())
                .build();

        when(dataServiceService.save(any())).thenReturn(savedDataService);

        // expect
        mockMvc.perform(post("/api/v1/data-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dataServiceCreate)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedDataService.getId()))
                .andExpect(jsonPath("$.name").value(savedDataService.getName()))
                .andExpect(header().string("Location", "/data-service"))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    DataService actualDataServiceCreate = objectMapper.readValue(response, DataService.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualDataServiceCreate).isNotNull();
                    softly.assertThat(actualDataServiceCreate.getId()).isEqualTo(savedDataService.getId());
                    softly.assertThat(actualDataServiceCreate.getName()).isEqualTo(savedDataService.getName());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreateWithEmptyName() throws Exception {
        // given
        DataService dataServiceCreate = DataService.builder()
                .name("")
                .build();

        String message = messageSource.getMessage("validation.data-service.name.empty", null, new Locale("ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/data-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dataServiceCreate)))
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
    public void testGetExistingDataService() throws Exception {
        // given
        DataService dataService = DataService.builder()
                .id(1L)
                .name("DataService 1")
                .build();

        when(dataServiceService.getById(dataService.getId())).thenReturn(dataService);

        // expect
        mockMvc.perform(get("/api/v1/data-service/{id}", dataService.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(dataService.getId()))
                .andExpect(jsonPath("$.name").value(dataService.getName()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    DataService actualDataService = objectMapper.readValue(response, DataService.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualDataService).isNotNull();
                    softly.assertThat(actualDataService.getId()).isEqualTo(dataService.getId());
                    softly.assertThat(actualDataService.getName()).isEqualTo(dataService.getName());
                    softly.assertAll();
                });
    }

    @Test
    public void testGetNonExistingDataService() throws Exception {
        // given
        Long dataServiceId = 1L;

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                DATA_SERVICE_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{dataServiceId},
                locale
        );

        when(dataServiceService.getById(dataServiceId)).thenThrow(new ResourceNotFoundException(message));

        ErrorResponse expectedResponseBody = new ErrorResponse(404, message);

        // expect
        mockMvc.perform(get("/api/v1/data-service/{id}", dataServiceId))
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
    public void testDeleteExistingDataService() throws Exception {
        // given
        Long dataServiceId = 1L;
        doNothing().when(dataServiceService).delete(dataServiceId);

        // expect
        mockMvc.perform(delete("/api/v1/data-service/{id}", dataServiceId))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteNonExistingDataService() throws Exception {
        // given
        Long dataServiceId = 1L;

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                DATA_SERVICE_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{dataServiceId},
                locale
        );

        doThrow(new ResourceNotFoundException(message)).when(dataServiceService).delete(dataServiceId);

        ErrorResponse expectedResponseBody = new ErrorResponse(404, message);

        // expect
        mockMvc.perform(delete("/api/v1/data-service/{id}", dataServiceId))
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
