package ru.kpfu.machinemetrics.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.model.Status;
import ru.kpfu.machinemetrics.repository.StatusRepository;

import java.util.List;
import java.util.Locale;

import static ru.kpfu.machinemetrics.constants.StatusConstants.STATUS_NOT_FOUND_EXCEPTION_MESSAGE;

@Service
@RequiredArgsConstructor
public class StatusService {

    private final StatusRepository statusRepository;

    private final MessageSource messageSource;

    public List<Status> getAll() {
        return statusRepository.findAll();
    }

    public Status save(@NotNull Status status) {
        return statusRepository.save(status);
    }

    private Status getById(@NotNull Long id) {
        return statusRepository.findById(id)
                .orElseThrow(() -> {
                    Locale locale = LocaleContextHolder.getLocale();
                    String message = messageSource.getMessage(
                            STATUS_NOT_FOUND_EXCEPTION_MESSAGE,
                            new Object[]{id},
                            locale
                    );
                    return new ResourceNotFoundException(message);
                });
    }

    public void delete(@NotNull Long id) {
        Status status = getById(id);
        // todo what if there are equipments
        statusRepository.delete(status);
    }
}
