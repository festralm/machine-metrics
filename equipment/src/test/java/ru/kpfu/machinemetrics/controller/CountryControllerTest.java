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
import ru.kpfu.machinemetrics.model.Country;
import ru.kpfu.machinemetrics.service.CountryService;

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
import static ru.kpfu.machinemetrics.constants.CountryConstants.COUNTRY_NOT_FOUND_EXCEPTION_MESSAGE;

@WebMvcTest(CountryController.class)
@ImportAutoConfiguration(MessageSourceConfig.class)
public class CountryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CountryService countryService;

    @Test
    public void testListAll() throws Exception {
        // given
        Country country1 = Country.builder()
                .id(1L)
                .name("Country 1")
                .build();
        Country country2 = Country.builder()
                .id(2L)
                .name("Country 2")
                .build();
        List<Country> countryList = List.of(country1, country2);

        when(countryService.getAll()).thenReturn(countryList);

        // expect
        mockMvc.perform(get("/api/v1/country"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(country1.getId()))
                .andExpect(jsonPath("$[0].name").value(country1.getName()))
                .andExpect(jsonPath("$[1].id").value(country2.getId()))
                .andExpect(jsonPath("$[1].name").value(country2.getName()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    List<Country> actualList = Arrays.asList(objectMapper.readValue(response,
                            Country[].class));

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualList).isNotNull();
                    softly.assertThat(actualList).hasSize(2);
                    softly.assertThat(actualList.get(0).getName()).isEqualTo(country1.getName());
                    softly.assertThat(actualList.get(1).getName()).isEqualTo(country2.getName());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreate() throws Exception {
        // given
        Country countryCreate = Country.builder()
                .name("name")
                .build();

        Country savedCountry = Country.builder()
                .id(1L)
                .name(countryCreate.getName())
                .build();

        when(countryService.save(any())).thenReturn(savedCountry);

        // expect
        mockMvc.perform(post("/api/v1/country")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(countryCreate)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedCountry.getId()))
                .andExpect(jsonPath("$.name").value(savedCountry.getName()))
                .andExpect(header().string("Location", "/country"))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    Country actualCountryCreate = objectMapper.readValue(response, Country.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualCountryCreate).isNotNull();
                    softly.assertThat(actualCountryCreate.getId()).isEqualTo(savedCountry.getId());
                    softly.assertThat(actualCountryCreate.getName()).isEqualTo(savedCountry.getName());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreateWithEmptyName() throws Exception {
        // given
        Country countryCreate = Country.builder()
                .name("")
                .build();

        String message = messageSource.getMessage("validation.country.name.empty", null, new Locale("ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/country")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(countryCreate)))
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
    public void testDeleteExistingCountry() throws Exception {
        // given
        Long countryId = 1L;
        doNothing().when(countryService).delete(countryId);

        // expect
        mockMvc.perform(delete("/api/v1/country/{id}", countryId))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteNonExistingCountry() throws Exception {
        // given
        Long countryId = 1L;

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                COUNTRY_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{countryId},
                locale
        );

        doThrow(new ResourceNotFoundException(message)).when(countryService).delete(countryId);

        ErrorResponse expectedResponseBody = new ErrorResponse(404, message);

        // expect
        mockMvc.perform(delete("/api/v1/country/{id}", countryId))
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
