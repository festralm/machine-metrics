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
import ru.kpfu.machinemetrics.model.UsageType;
import ru.kpfu.machinemetrics.repository.UsageTypeRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.kpfu.machinemetrics.constants.UsageTypeConstants.USAGE_TYPE_NOT_FOUND_EXCEPTION_MESSAGE;

@SpringBootTest
@TestPropertySource("classpath:application.yml")
@ImportAutoConfiguration(MessageSourceConfig.class)
public class UsageTypeServiceTest {

    @Autowired
    private MessageSource messageSource;

    @MockBean
    private UsageTypeRepository usageTypeRepository;

    @Autowired
    private UsageTypeService usageTypeService;

    @Test
    public void testGetAll() {
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

        when(usageTypeRepository.findAll()).thenReturn(usageTypeList);

        // when
        List<UsageType> result = usageTypeService.getAll();

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.size()).isEqualTo(2);
        softly.assertThat(result.get(0).getId()).isEqualTo(usageType1.getId());
        softly.assertThat(result.get(0).getName()).isEqualTo(usageType1.getName());
        softly.assertThat(result.get(1).getId()).isEqualTo(usageType2.getId());
        softly.assertThat(result.get(1).getName()).isEqualTo(usageType2.getName());
        softly.assertAll();
    }

    @Test
    void testSave() {
        // given
        UsageType usageType = UsageType.builder()
                .name("Test UsageType")
                .build();

        UsageType savedUsageType = UsageType.builder()
                .name(usageType.getName())
                .build();

        when(usageTypeRepository.save(any(UsageType.class))).thenReturn(savedUsageType);

        // when
        UsageType actualUsageType = usageTypeService.save(usageType);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualUsageType.getName()).isEqualTo(savedUsageType.getName());
        softly.assertAll();
    }

    @Test
    void testDeleteWithExistingUsageType() {
        // given
        Long usageTypeId = 1L;

        UsageType usageType = new UsageType();
        usageType.setId(usageTypeId);
        usageType.setName("Test UsageType");

        when(usageTypeRepository.findById(usageTypeId)).thenReturn(Optional.of(usageType));

        // when
        usageTypeService.delete(usageTypeId);

        // then
        verify(usageTypeRepository, Mockito.times(1)).findById(usageTypeId);
        verify(usageTypeRepository, Mockito.times(1)).delete(usageType);
    }

    @Test
    void testDeleteWithNonExistingUsageType() {
        // given
        Long usageTypeId = 1L;
        when(usageTypeRepository.findById(usageTypeId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> usageTypeService.delete(usageTypeId));

        // then
        verify(usageTypeRepository, Mockito.times(1)).findById(usageTypeId);
        verify(usageTypeRepository, Mockito.never()).save(Mockito.any(UsageType.class));
        String expectedMessage = messageSource.getMessage(
                USAGE_TYPE_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{usageTypeId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);
    }
}
