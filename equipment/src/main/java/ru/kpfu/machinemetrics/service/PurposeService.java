package ru.kpfu.machinemetrics.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.model.Purpose;
import ru.kpfu.machinemetrics.repository.PurposeRepository;

import java.util.List;
import java.util.Locale;

import static ru.kpfu.machinemetrics.constants.PurposeConstants.PURPOSE_NOT_FOUND_EXCEPTION_MESSAGE;

@Service
@RequiredArgsConstructor
public class PurposeService {

    private final PurposeRepository purposeRepository;

    private final MessageSource messageSource;

    public List<Purpose> getAll() {
        return purposeRepository.findAll();
    }

    public Purpose save(@NotNull Purpose purpose) {
        return purposeRepository.save(purpose);
    }

    private Purpose getById(@NotNull Long id) {
        return purposeRepository.findById(id)
                .orElseThrow(() -> {
                    Locale locale = LocaleContextHolder.getLocale();
                    String message = messageSource.getMessage(
                            PURPOSE_NOT_FOUND_EXCEPTION_MESSAGE,
                            new Object[]{id},
                            locale
                    );
                    return new ResourceNotFoundException(message);
                });
    }

    public void delete(@NotNull Long id) {
        Purpose purpose = getById(id);
        // todo what if there are equipments
        purposeRepository.delete(purpose);
    }
}
