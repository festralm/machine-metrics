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
import ru.kpfu.machinemetrics.exception.ValidationException;
import ru.kpfu.machinemetrics.model.Cron;
import ru.kpfu.machinemetrics.repository.CronRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.kpfu.machinemetrics.constants.CronConstants.CRON_NOT_FOUND_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.CronConstants.CRON_VALIDATION_EXCEPTION_MESSAGE;

@SpringBootTest
@ImportAutoConfiguration(MessageSourceConfig.class)
public class CronServiceTest {

    @Autowired
    private MessageSource messageSource;

    @MockBean
    private CronRepository cronRepositoryMock;

    @Autowired
    private CronService cronService;

    @Test
    public void testGetAll() {
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

        when(cronRepositoryMock.findAll()).thenReturn(cronList);

        // when
        List<Cron> result = cronService.getAll();

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.size()).isEqualTo(2);
        softly.assertThat(result.get(0).getId()).isEqualTo(cron1.getId());
        softly.assertThat(result.get(0).getOrder()).isEqualTo(cron1.getOrder());
        softly.assertThat(result.get(0).getName()).isEqualTo(cron1.getName());
        softly.assertThat(result.get(1).getId()).isEqualTo(cron2.getId());
        softly.assertThat(result.get(1).getOrder()).isEqualTo(cron2.getOrder());
        softly.assertThat(result.get(1).getName()).isEqualTo(cron2.getName());
        softly.assertAll();
    }

    @Test
    void testSave() {
        // given
        Cron cron = Cron.builder()
                .expression("1 * * * * ?")
                .order(1)
                .name("Cron 1")
                .build();

        Cron savedCron = Cron.builder()
                .id(1L)
                .order(cron.getOrder())
                .name(cron.getName())
                .build();

        when(cronRepositoryMock.save(any(Cron.class))).thenReturn(savedCron);

        // when
        Cron actualCron = cronService.save(cron);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualCron.getId()).isEqualTo(savedCron.getId());
        softly.assertThat(actualCron.getOrder()).isEqualTo(savedCron.getOrder());
        softly.assertThat(actualCron.getName()).isEqualTo(savedCron.getName());
        softly.assertAll();
    }

    @Test
    void testSaveWithWrongCron() {
        // given
        Cron cron = Cron.builder()
                .expression("wrong")
                .order(1)
                .name("Cron 1")
                .build();

        // when
        Throwable thrown = catchThrowable(() -> cronService.save(cron));

        // then
        verify(cronRepositoryMock, Mockito.never()).save(Mockito.any(Cron.class));
        String expectedMessage = messageSource.getMessage(
                CRON_VALIDATION_EXCEPTION_MESSAGE,
                new Object[]{cron.getExpression()},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ValidationException.class).hasMessage(expectedMessage);
    }

    @Test
    void testDeleteWithExistingCron() {
        // given
        Long cronId = 1L;

        Cron cron = Cron.builder()
                .id(cronId)
                .expression("1 * * * * ?")
                .order(1)
                .name("Cron 1")
                .build();

        when(cronRepositoryMock.findById(cronId)).thenReturn(Optional.of(cron));

        // when
        cronService.delete(cronId);

        // then
        verify(cronRepositoryMock, Mockito.times(1)).findById(cronId);
        verify(cronRepositoryMock, Mockito.times(1)).delete(cron);
    }

    @Test
    void testDeleteWithNonExistingCron() {
        // given
        Long cronId = 1L;
        when(cronRepositoryMock.findById(cronId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> cronService.delete(cronId));

        // then
        verify(cronRepositoryMock, Mockito.times(1)).findById(cronId);
        verify(cronRepositoryMock, Mockito.never()).save(Mockito.any(Cron.class));
        String expectedMessage = messageSource.getMessage(
                CRON_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{cronId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    void testEdit() {
        // given
        Long cronId = 1L;

        Cron existingCron = Cron.builder()
                .id(cronId)
                .expression("1 * * * * ?")
                .order(1)
                .name("Existing Cron")
                .build();

        Cron updatedCron = Cron.builder()
                .expression("2 * * * * ?")
                .order(1)
                .name("Updated Cron")
                .build();

        when(cronRepositoryMock.findById(cronId)).thenReturn(Optional.of(existingCron));
        when(cronRepositoryMock.save(any(Cron.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Cron actualCron = cronService.edit(cronId, updatedCron);

        // then
        verify(cronRepositoryMock).findById(cronId);
        verify(cronRepositoryMock).save(existingCron);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualCron.getId()).isEqualTo(existingCron.getId());
        softly.assertThat(actualCron.getExpression()).isEqualTo(updatedCron.getExpression());
        softly.assertThat(actualCron.getOrder()).isEqualTo(updatedCron.getOrder());
        softly.assertThat(actualCron.getName()).isEqualTo(updatedCron.getName());
        softly.assertAll();
    }

    @Test
    void testEditCronNotFound() {
        // given
        Long cronId = 1L;

        when(cronRepositoryMock.findById(cronId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> cronService.delete(cronId));

        // then
        String expectedMessage = messageSource.getMessage(
                CRON_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{cronId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    void testEditWithWrongCron() {
        // given
        Long cronId = 1L;
        Cron existingCron = Cron.builder()
                .id(cronId)
                .expression("1 * * * * ?")
                .order(1)
                .name("Existing Cron")
                .build();

        Cron updatedCron = Cron.builder()
                .expression("* * * * * * *")
                .order(1)
                .name("Cron 1")
                .build();

        when(cronRepositoryMock.findById(cronId)).thenReturn(Optional.of(existingCron));

        // when
        Throwable thrown = catchThrowable(() -> cronService.edit(cronId, updatedCron));

        // then
        verify(cronRepositoryMock, Mockito.never()).save(Mockito.any(Cron.class));
        String expectedMessage = messageSource.getMessage(
                CRON_VALIDATION_EXCEPTION_MESSAGE,
                new Object[]{updatedCron.getExpression()},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ValidationException.class).hasMessage(expectedMessage);
    }
}
