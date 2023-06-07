package ru.kpfu.machinemetrics.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.machinemetrics.dto.AddressCreateDto;
import ru.kpfu.machinemetrics.dto.AddressDto;
import ru.kpfu.machinemetrics.mapper.AddressMapper;
import ru.kpfu.machinemetrics.model.Address;
import ru.kpfu.machinemetrics.service.AddressService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "${app.api.prefix.v1}/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final AddressMapper addressMapper;

    @GetMapping
    public List<AddressDto> listAll() {
        List<Address> addressList = addressService.getAll();
        return addressMapper.toAddressDtos(addressList);
    }

    @PostMapping
    public ResponseEntity<AddressDto> create(@Valid @RequestBody AddressCreateDto dto) {
        Address address = addressMapper.toAddress(dto);
        Address savedAddress = addressService.save(address);
        AddressDto savedDto = addressMapper.toAddressDto(savedAddress);
        return ResponseEntity.created(URI.create("/address")).body(savedDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
