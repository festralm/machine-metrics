package ru.kpfu.machinemetrics.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.model.Address;
import ru.kpfu.machinemetrics.repository.AddressRepository;

import java.util.List;
import java.util.Locale;

import static ru.kpfu.machinemetrics.constants.AddressConstants.ADDRESS_NOT_FOUND_EXCEPTION_MESSAGE;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    private final MessageSource messageSource;

    public List<Address> getAll() {
        return addressRepository.findAll();
    }

    public Address save(@NotNull Address address) {
        return addressRepository.save(address);
    }

    private Address getById(@NotNull Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> {
                    Locale locale = LocaleContextHolder.getLocale();
                    String message = messageSource.getMessage(
                            ADDRESS_NOT_FOUND_EXCEPTION_MESSAGE,
                            new Object[]{id},
                            locale
                    );
                    return new ResourceNotFoundException(message);
                });
    }

    public void delete(@NotNull Long id) {
        Address address = getById(id);
        // todo what if there are equipments
        addressRepository.delete(address);
    }
}
