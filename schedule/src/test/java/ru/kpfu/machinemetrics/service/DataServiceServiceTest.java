package ru.kpfu.machinemetrics.service;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import ru.kpfu.machinemetrics.configuration.MessageSourceConfig;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.model.DataService;
import ru.kpfu.machinemetrics.repository.DataServiceRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.kpfu.machinemetrics.constants.DataServiceConstants.DATA_SERVICE_NOT_FOUND_EXCEPTION_MESSAGE;

@SpringBootTest
@ImportAutoConfiguration(MessageSourceConfig.class)
public class DataServiceServiceTest {

    @Autowired
    private MessageSource messageSource;

    @MockBean
    private DataServiceRepository dataServiceRepository;

    @Autowired
    private DataServiceService dataServiceService;

    @Test
    public void testGetAll() {
        // given
        DataService dataService1 = DataService.builder()
                .name("DataService 1")
                .url("url 1")
                .build();
        DataService dataService2 = DataService.builder()
                .name("DataService 2")
                .url("url 2")
                .build();
        List<DataService> dataServiceList = List.of(dataService1, dataService2);

        when(dataServiceRepository.findAll()).thenReturn(dataServiceList);

        // when
        List<DataService> result = dataServiceService.getAll();

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.size()).isEqualTo(2);
        softly.assertThat(result.get(0).getName()).isEqualTo(dataService1.getName());
        softly.assertThat(result.get(0).getUrl()).isEqualTo(dataService1.getUrl());
        softly.assertThat(result.get(1).getName()).isEqualTo(dataService2.getName());
        softly.assertThat(result.get(1).getUrl()).isEqualTo(dataService2.getUrl());
        softly.assertAll();
    }

    @Test
    void testSave() {
        // given
        DataService dataService = DataService.builder()
                .name("Test DataService")
                .url("Test url")
                .build();

        DataService savedDataService = DataService.builder()
                .id(1L)
                .name(dataService.getName())
                .url(dataService.getUrl())
                .build();

        when(dataServiceRepository.save(any(DataService.class))).thenReturn(savedDataService);

        // when
        DataService actualDataService = dataServiceService.save(dataService);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDataService.getId()).isEqualTo(savedDataService.getId());
        softly.assertThat(actualDataService.getName()).isEqualTo(savedDataService.getName());
        softly.assertThat(actualDataService.getUrl()).isEqualTo(savedDataService.getUrl());
        softly.assertAll();
    }

    @Test
    public void testGetByIdFound() {
        // given
        DataService dataService = new DataService();
        dataService.setId(1L);
        dataService.setName("DataService 1");
        dataService.setUrl("Url 1");

        when(dataServiceRepository.findById(dataService.getId())).thenReturn(Optional.of(dataService));

        // when
        DataService actualDataService = dataServiceService.getById(dataService.getId());

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDataService.getId()).isEqualTo(dataService.getId());
        softly.assertThat(actualDataService.getName()).isEqualTo(dataService.getName());
        softly.assertThat(actualDataService.getUrl()).isEqualTo(dataService.getUrl());
        softly.assertAll();
    }

    @Test
    public void testGetByIdNotFound() {
        // given
        Long givenId = 1L;

        when(dataServiceRepository.findById(givenId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> dataServiceService.getById(givenId));

        // then
        String expectedMessage = messageSource.getMessage(
                DATA_SERVICE_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{givenId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    void testDeleteWithExistingDataService() {
        // given
        Long dataServiceId = 1L;

        DataService dataService = new DataService();
        dataService.setId(dataServiceId);
        dataService.setName("Test DataService");
        dataService.setUrl("Test Url");

        when(dataServiceRepository.findById(dataServiceId)).thenReturn(Optional.of(dataService));

        // when
        dataServiceService.delete(dataServiceId);

        // then
        verify(dataServiceRepository, Mockito.times(1)).findById(dataServiceId);
        verify(dataServiceRepository, Mockito.times(1)).delete(dataService);
    }

    @Test
    void testDeleteWithNonExistingDataService() {
        // given
        Long dataServiceId = 1L;
        when(dataServiceRepository.findById(dataServiceId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> dataServiceService.delete(dataServiceId));

        // then
        verify(dataServiceRepository, Mockito.times(1)).findById(dataServiceId);
        verify(dataServiceRepository, Mockito.never()).save(Mockito.any(DataService.class));
        String expectedMessage = messageSource.getMessage(
                DATA_SERVICE_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{dataServiceId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    void testEdit() {
        // given
        Long dataServiceId = 1L;

        DataService updatedDataService = DataService.builder()
                .id(dataServiceId)
                .name("Updated DataService")
                .url("Updated URL")
                .build();

        when(dataServiceRepository.findById(dataServiceId)).thenReturn(Optional.of(updatedDataService));
        when(dataServiceRepository.save(any(DataService.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        DataService actualDataService = dataServiceService.edit(updatedDataService);

        // then
        verify(dataServiceRepository).findById(dataServiceId);
        verify(dataServiceRepository).save(updatedDataService);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualDataService.getId()).isEqualTo(updatedDataService.getId());
        softly.assertThat(actualDataService.getName()).isEqualTo(updatedDataService.getName());
        softly.assertThat(actualDataService.getUrl()).isEqualTo(updatedDataService.getUrl());
        softly.assertAll();
    }

    @Test
    void testEditDataServiceNotFound() {
        // given
        Long dataServiceId = 1L;

        when(dataServiceRepository.findById(dataServiceId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> dataServiceService.delete(dataServiceId));

        // then
        String expectedMessage = messageSource.getMessage(
                DATA_SERVICE_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{dataServiceId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);
    }
}
