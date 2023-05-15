package ru.kpfu.machinemetrics.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.model.Country;
import ru.kpfu.machinemetrics.repository.CountryRepository;

import java.util.List;
import java.util.Locale;

import static ru.kpfu.machinemetrics.constants.CountryConstants.COUNTRY_NOT_FOUND_EXCEPTION_MESSAGE;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;

    private final MessageSource messageSource;

    public List<Country> getAll() {
        return countryRepository.findAll();
    }

    public Country save(@NotNull Country country) {
        return countryRepository.save(country);
    }

    private Country getById(@NotNull Long id) {
        return countryRepository.findById(id)
                .orElseThrow(() -> {
                    Locale locale = LocaleContextHolder.getLocale();
                    String message = messageSource.getMessage(
                            COUNTRY_NOT_FOUND_EXCEPTION_MESSAGE,
                            new Object[]{id},
                            locale
                    );
                    return new ResourceNotFoundException(message);
                });
    }

    public void delete(@NotNull Long id) {
        Country country = getById(id);
        // todo what if there are equipments
        countryRepository.delete(country);
    }
}
