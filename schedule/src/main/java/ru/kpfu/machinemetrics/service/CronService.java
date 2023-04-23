package ru.kpfu.machinemetrics.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.exception.ValidationException;
import ru.kpfu.machinemetrics.model.Cron;
import ru.kpfu.machinemetrics.repository.CronRepository;

import java.util.List;
import java.util.Locale;

import static ru.kpfu.machinemetrics.constants.CronConstants.CRON_NOT_FOUND_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.CronConstants.CRON_VALIDATION_EXCEPTION_MESSAGE;

@Service
@RequiredArgsConstructor
public class CronService {

    private final CronRepository cronRepository;
    private final MessageSource messageSource;

    public List<Cron> getAll() {
        return cronRepository.findAll();
    }

    public Cron save(@NotNull Cron cron) {
        if (!CronExpression.isValidExpression(cron.getId())) {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage(
                    CRON_VALIDATION_EXCEPTION_MESSAGE,
                    new Object[]{cron.getId()},
                    locale
            );
            throw new ValidationException(message);
        }
        return cronRepository.save(cron);
    }

    public void delete(@NotNull String id) {
        Cron cron = cronRepository.findById(id)
                .orElseThrow(() -> {
                    Locale locale = LocaleContextHolder.getLocale();
                    String message = messageSource.getMessage(
                            CRON_NOT_FOUND_EXCEPTION_MESSAGE,
                            new Object[]{id},
                            locale
                    );
                    return new ResourceNotFoundException(message);
                });
        ;
        cronRepository.delete(cron);
    }
}
