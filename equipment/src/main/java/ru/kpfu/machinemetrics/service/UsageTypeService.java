package ru.kpfu.machinemetrics.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.model.UsageType;
import ru.kpfu.machinemetrics.repository.UsageTypeRepository;

import java.util.List;
import java.util.Locale;

import static ru.kpfu.machinemetrics.constants.UsageTypeConstants.USAGE_TYPE_NOT_FOUND_EXCEPTION_MESSAGE;

@Service
@RequiredArgsConstructor
public class UsageTypeService {

    private final UsageTypeRepository usageTypeRepository;

    private final MessageSource messageSource;

    public List<UsageType> getAll() {
        return usageTypeRepository.findAll();
    }

    public UsageType save(@NotNull UsageType usageType) {
        return usageTypeRepository.save(usageType);
    }

    private UsageType getById(@NotNull Long id) {
        return usageTypeRepository.findById(id)
                .orElseThrow(() -> {
                    Locale locale = LocaleContextHolder.getLocale();
                    String message = messageSource.getMessage(
                            USAGE_TYPE_NOT_FOUND_EXCEPTION_MESSAGE,
                            new Object[]{id},
                            locale
                    );
                    return new ResourceNotFoundException(message);
                });
    }

    public void delete(@NotNull Long id) {
        UsageType usageType = getById(id);
        // todo what if there are equipments
        usageTypeRepository.delete(usageType);
    }
}
