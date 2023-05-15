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
import ru.kpfu.machinemetrics.model.Unit;
import ru.kpfu.machinemetrics.repository.UnitRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.kpfu.machinemetrics.constants.UnitConstants.UNIT_NOT_FOUND_EXCEPTION_MESSAGE;

@SpringBootTest
@ImportAutoConfiguration(MessageSourceConfig.class)
public class UnitServiceTest {

    @Autowired
    private MessageSource messageSource;

    @MockBean
    private UnitRepository unitRepository;

    @Autowired
    private UnitService unitService;

    @Test
    public void testGetAll() {
        // given
        Unit unit1 = Unit.builder()
                .id(1L)
                .name("Unit 1")
                .build();
        Unit unit2 = Unit.builder()
                .id(2L)
                .name("Unit 2")
                .build();
        List<Unit> unitList = List.of(unit1, unit2);

        when(unitRepository.findAll()).thenReturn(unitList);

        // when
        List<Unit> result = unitService.getAll();

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.size()).isEqualTo(2);
        softly.assertThat(result.get(0).getId()).isEqualTo(unit1.getId());
        softly.assertThat(result.get(0).getName()).isEqualTo(unit1.getName());
        softly.assertThat(result.get(1).getId()).isEqualTo(unit2.getId());
        softly.assertThat(result.get(1).getName()).isEqualTo(unit2.getName());
        softly.assertAll();
    }

    @Test
    void testSave() {
        // given
        Unit parent = Unit.builder()
                .id(1L)
                .name("Test Parent")
                .build();

        Unit unit = Unit.builder()
                .name("Test Unit")
                .parent(parent)
                .build();

        Unit savedUnit = Unit.builder()
                .name(unit.getName())
                .build();

        when(unitRepository.save(any(Unit.class))).thenReturn(savedUnit);
        when(unitRepository.findById(parent.getId())).thenReturn(Optional.of(parent));

        // when
        Unit actualUnit = unitService.save(unit);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualUnit.getName()).isEqualTo(savedUnit.getName());
        softly.assertAll();
    }

    @Test
    void testDeleteWithExistingUnit() {
        // given
        Long unitId = 1L;

        Unit unit = new Unit();
        unit.setId(unitId);
        unit.setName("Test Unit");

        when(unitRepository.findById(unitId)).thenReturn(Optional.of(unit));

        // when
        unitService.delete(unitId);

        // then
        verify(unitRepository, Mockito.times(1)).findById(unitId);
        verify(unitRepository, Mockito.times(1)).delete(unit);
    }

    @Test
    void testDeleteWithNonExistingUnit() {
        // given
        Long unitId = 1L;
        when(unitRepository.findById(unitId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> unitService.delete(unitId));

        // then
        verify(unitRepository, Mockito.times(1)).findById(unitId);
        verify(unitRepository, Mockito.never()).save(Mockito.any(Unit.class));
        String expectedMessage = messageSource.getMessage(
                UNIT_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{unitId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);
    }
}
