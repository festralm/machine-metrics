package ru.kpfu.machinemetrics.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.kpfu.machinemetrics.dto.AddressCreateDto;
import ru.kpfu.machinemetrics.dto.AddressDto;
import ru.kpfu.machinemetrics.model.Address;
import ru.kpfu.machinemetrics.model.Unit;

import java.util.List;

@Mapper(uses = {UnitMapper.class})
public interface AddressMapper {

    @Mapping(source = "unit", target = "unit", qualifiedByName = "mapToUnit")
    Address toAddress(AddressCreateDto dto);

    @Named("mapToUnit")
    @Mapping(source = ".", target = "id")
    Unit mapToUnit(Long id);

    List<AddressDto> toAddressDtos(List<Address> addresses);

    AddressDto toAddressDto(Address address);
}
