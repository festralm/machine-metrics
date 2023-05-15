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
import ru.kpfu.machinemetrics.model.Purpose;
import ru.kpfu.machinemetrics.repository.PurposeRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.kpfu.machinemetrics.constants.PurposeConstants.PURPOSE_NOT_FOUND_EXCEPTION_MESSAGE;

@SpringBootTest
@ImportAutoConfiguration(MessageSourceConfig.class)
public class PurposeServiceTest {

    @Autowired
    private MessageSource messageSource;

    @MockBean
    private PurposeRepository purposeRepository;

    @Autowired
    private PurposeService purposeService;

    @Test
    public void testGetAll() {
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

        when(purposeRepository.findAll()).thenReturn(purposeList);

        // when
        List<Purpose> result = purposeService.getAll();

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.size()).isEqualTo(2);
        softly.assertThat(result.get(0).getId()).isEqualTo(purpose1.getId());
        softly.assertThat(result.get(0).getName()).isEqualTo(purpose1.getName());
        softly.assertThat(result.get(1).getId()).isEqualTo(purpose2.getId());
        softly.assertThat(result.get(1).getName()).isEqualTo(purpose2.getName());
        softly.assertAll();
    }

    @Test
    void testSave() {
        // given
        Purpose purpose = Purpose.builder()
                .name("Test Purpose")
                .build();

        Purpose savedPurpose = Purpose.builder()
                .name(purpose.getName())
                .build();

        when(purposeRepository.save(any(Purpose.class))).thenReturn(savedPurpose);

        // when
        Purpose actualPurpose = purposeService.save(purpose);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualPurpose.getName()).isEqualTo(savedPurpose.getName());
        softly.assertAll();
    }

    @Test
    void testDeleteWithExistingPurpose() {
        // given
        Long purposeId = 1L;

        Purpose purpose = new Purpose();
        purpose.setId(purposeId);
        purpose.setName("Test Purpose");

        when(purposeRepository.findById(purposeId)).thenReturn(Optional.of(purpose));

        // when
        purposeService.delete(purposeId);

        // then
        verify(purposeRepository, Mockito.times(1)).findById(purposeId);
        verify(purposeRepository, Mockito.times(1)).delete(purpose);
    }

    @Test
    void testDeleteWithNonExistingPurpose() {
        // given
        Long purposeId = 1L;
        when(purposeRepository.findById(purposeId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> purposeService.delete(purposeId));

        // then
        verify(purposeRepository, Mockito.times(1)).findById(purposeId);
        verify(purposeRepository, Mockito.never()).save(Mockito.any(Purpose.class));
        String expectedMessage = messageSource.getMessage(
                PURPOSE_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{purposeId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);
    }
}
