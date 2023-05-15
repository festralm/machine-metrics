package ru.kpfu.machinemetrics.service;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import ru.kpfu.machinemetrics.config.MessageSourceConfig;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.model.Country;
import ru.kpfu.machinemetrics.repository.CountryRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.kpfu.machinemetrics.constants.CountryConstants.COUNTRY_NOT_FOUND_EXCEPTION_MESSAGE;

@SpringBootTest
@ImportAutoConfiguration(MessageSourceConfig.class)
public class CountryServiceTest {

    @Autowired
    private MessageSource messageSource;

    @MockBean
    private CountryRepository countryRepository;

    @Autowired
    private CountryService countryService;

    @Test
    public void testGetAll() {
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

        when(countryRepository.findAll()).thenReturn(countryList);

        // when
        List<Country> result = countryService.getAll();

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.size()).isEqualTo(2);
        softly.assertThat(result.get(0).getId()).isEqualTo(country1.getId());
        softly.assertThat(result.get(0).getName()).isEqualTo(country1.getName());
        softly.assertThat(result.get(1).getId()).isEqualTo(country2.getId());
        softly.assertThat(result.get(1).getName()).isEqualTo(country2.getName());
        softly.assertAll();
    }

    @Test
    void testSave() {
        // given
        Country country = Country.builder()
                .name("Test Country")
                .build();

        Country savedCountry = Country.builder()
                .name(country.getName())
                .build();

        when(countryRepository.save(any(Country.class))).thenReturn(savedCountry);

        // when
        Country actualCountry = countryService.save(country);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualCountry.getName()).isEqualTo(savedCountry.getName());
        softly.assertAll();
    }

    @Test
    void testDeleteWithExistingCountry() {
        // given
        Long countryId = 1L;

        Country country = new Country();
        country.setId(countryId);
        country.setName("Test Country");

        when(countryRepository.findById(countryId)).thenReturn(Optional.of(country));

        // when
        countryService.delete(countryId);

        // then
        verify(countryRepository, Mockito.times(1)).findById(countryId);
        verify(countryRepository, Mockito.times(1)).delete(country);
    }

    @Test
    void testDeleteWithNonExistingCountry() {
        // given
        Long countryId = 1L;
        when(countryRepository.findById(countryId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> countryService.delete(countryId));

        // then
        verify(countryRepository, Mockito.times(1)).findById(countryId);
        verify(countryRepository, Mockito.never()).save(Mockito.any(Country.class));
        String expectedMessage = messageSource.getMessage(
                COUNTRY_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{countryId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);
    }
}
