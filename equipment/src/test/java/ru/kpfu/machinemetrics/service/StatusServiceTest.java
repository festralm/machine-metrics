package ru.kpfu.machinemetrics.service;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.TestPropertySource;
import ru.kpfu.machinemetrics.config.MessageSourceConfig;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.model.Status;
import ru.kpfu.machinemetrics.repository.StatusRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.kpfu.machinemetrics.constants.StatusConstants.STATUS_NOT_FOUND_EXCEPTION_MESSAGE;

@SpringBootTest
@TestPropertySource("classpath:application.yml")
@ImportAutoConfiguration(MessageSourceConfig.class)
public class StatusServiceTest {

    @Autowired
    private MessageSource messageSource;

    @MockBean
    private StatusRepository statusRepository;

    @Autowired
    private StatusService statusService;

    @Test
    public void testGetAll() {
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

        when(statusRepository.findAll()).thenReturn(statusList);

        // when
        List<Status> result = statusService.getAll();

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.size()).isEqualTo(2);
        softly.assertThat(result.get(0).getId()).isEqualTo(status1.getId());
        softly.assertThat(result.get(0).getName()).isEqualTo(status1.getName());
        softly.assertThat(result.get(1).getId()).isEqualTo(status2.getId());
        softly.assertThat(result.get(1).getName()).isEqualTo(status2.getName());
        softly.assertAll();
    }

    @Test
    void testSave() {
        // given
        Status status = Status.builder()
                .name("Test Status")
                .build();

        Status savedStatus = Status.builder()
                .name(status.getName())
                .build();

        when(statusRepository.save(any(Status.class))).thenReturn(savedStatus);

        // when
        Status actualStatus = statusService.save(status);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualStatus.getName()).isEqualTo(savedStatus.getName());
        softly.assertAll();
    }

    @Test
    void testDeleteWithExistingStatus() {
        // given
        Long statusId = 1L;

        Status status = new Status();
        status.setId(statusId);
        status.setName("Test Status");

        when(statusRepository.findById(statusId)).thenReturn(Optional.of(status));

        // when
        statusService.delete(statusId);

        // then
        verify(statusRepository, Mockito.times(1)).findById(statusId);
        verify(statusRepository, Mockito.times(1)).delete(status);
    }

    @Test
    void testDeleteWithNonExistingStatus() {
        // given
        Long statusId = 1L;
        when(statusRepository.findById(statusId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> statusService.delete(statusId));

        // then
        verify(statusRepository, Mockito.times(1)).findById(statusId);
        verify(statusRepository, Mockito.never()).save(Mockito.any(Status.class));
        String expectedMessage = messageSource.getMessage(
                STATUS_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{statusId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);
    }
}
