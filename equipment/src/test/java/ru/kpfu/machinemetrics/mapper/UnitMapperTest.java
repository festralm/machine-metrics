package ru.kpfu.machinemetrics.mapper;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.kpfu.machinemetrics.dto.UnitCreateDto;
import ru.kpfu.machinemetrics.dto.UnitDto;
import ru.kpfu.machinemetrics.model.Unit;

import java.util.List;

@SpringBootTest
public class UnitMapperTest {

    @Autowired
    private UnitMapper unitMapper;

    @Test
    public void testToUnit() {
        // given
        UnitCreateDto dto = UnitCreateDto.builder()
                .name("name 1")
                .build();

        // when
        Unit unit = unitMapper.toUnit(dto);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(unit.getName()).isEqualTo(dto.getName());
        softly.assertThat(unit.getParent()).isNull();
        softly.assertAll();
    }

    @Test
    public void testToUnit_withParent() {
        // given
        UnitCreateDto dto = UnitCreateDto.builder()
                .name("name 1")
                .parent(1L)
                .build();

        // when
        Unit unit = unitMapper.toUnit(dto);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(unit.getName()).isEqualTo(dto.getName());
        softly.assertThat(unit.getParent().getId()).isEqualTo(dto.getParent());
        softly.assertAll();
    }

    @Test
    public void testToUnitDtos() {
        // given
        Unit unit1 = Unit.builder()
                .id(1L)
                .name("name 1")
                .build();
        Unit unit2 = Unit.builder()
                .id(2L)
                .name("name 2")
                .parent(unit1)
                .build();
        Unit unit3 = Unit.builder()
                .id(3L)
                .name("name 3")
                .parent(unit2)
                .build();
        List<Unit> units = List.of(unit1, unit2, unit3);

        // when
        List<UnitDto> dtos = unitMapper.toUnitDtos(units);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(dtos).hasSize(units.size());

        softly.assertThat(dtos.get(0).getId()).isEqualTo(units.get(0).getId());
        softly.assertThat(dtos.get(0).getName()).isEqualTo("name 1");

        softly.assertThat(dtos.get(1).getId()).isEqualTo(units.get(1).getId());
        softly.assertThat(dtos.get(1).getName()).isEqualTo("name 1/name 2");

        softly.assertThat(dtos.get(2).getId()).isEqualTo(units.get(2).getId());
        softly.assertThat(dtos.get(2).getName()).isEqualTo("name 1/name 2/name 3");

        softly.assertAll();
    }
}
