package ru.kpfu.machinemetrics.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.kpfu.machinemetrics.dto.UnitCreateDto;
import ru.kpfu.machinemetrics.dto.UnitDto;
import ru.kpfu.machinemetrics.model.Unit;

import java.util.List;

@Mapper
public interface UnitMapper {

    @Mapping(source = "parent", target = "parent", qualifiedByName = "mapToParent")
    Unit toUnit(UnitCreateDto dto);

    @Named("mapToParent")
    @Mapping(source = ".", target = "id")
    Unit mapToParent(Long id);

    List<UnitDto> toUnitDtos(List<Unit> units);

    @Mapping(source = ".", target = "name", qualifiedByName = "getName")
    UnitDto toUnitDto(Unit unit);

    @Named("getName")
    default String getName(Unit unit) {
        Unit currentUnit = unit;
        StringBuilder sb = new StringBuilder();

        sb.append(currentUnit.getName());
        currentUnit = unit.getParent();

        while (currentUnit != null) {
            sb.insert(0, "/").insert(0, currentUnit.getName());
            currentUnit = currentUnit.getParent();
        }

        return sb.toString();
    }
}
