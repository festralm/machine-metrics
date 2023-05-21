package ru.kpfu.machinemetrics.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import ru.kpfu.machinemetrics.exception.CannotDeleteScheduleException;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.exception.ScheduleIsAlreadyCreatedException;
import ru.kpfu.machinemetrics.model.Schedule;
import ru.kpfu.machinemetrics.repository.ScheduleRepository;

import java.util.List;
import java.util.Locale;

import static ru.kpfu.machinemetrics.constants.ScheduleConstants.SCHEDULE_CREATED_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.ScheduleConstants.SCHEDULE_DELETE_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.ScheduleConstants.SCHEDULE_NOT_FOUND_EXCEPTION_MESSAGE;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    private final MessageSource messageSource;

    public List<Schedule> getAll() {
        return scheduleRepository.findAll();
    }

    public Schedule save(@NotNull Schedule schedule) {
        if (schedule.getDate() == null && scheduleRepository.findByDate(null).isPresent()) {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage(
                    SCHEDULE_CREATED_EXCEPTION_MESSAGE,
                    new Object[]{},
                    locale
            );
            throw new ScheduleIsAlreadyCreatedException(message);
        }
        return scheduleRepository.save(schedule);
    }

    private Schedule getById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> {
                    Locale locale = LocaleContextHolder.getLocale();
                    String message = messageSource.getMessage(
                            SCHEDULE_NOT_FOUND_EXCEPTION_MESSAGE,
                            new Object[]{id},
                            locale
                    );
                    return new ResourceNotFoundException(message);
                });
    }

    public void delete(Long id) {
        Schedule schedule = getById(id);
        if (schedule.getDate() == null) {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage(
                    SCHEDULE_DELETE_EXCEPTION_MESSAGE,
                    new Object[]{id},
                    locale
            );
            throw new CannotDeleteScheduleException(message);
        }
        scheduleRepository.delete(schedule);
    }

    public Schedule edit(Long id, Schedule updatedSchedule) {
        Schedule schedule = getById(id);
        var optionalDefaultSchedule = scheduleRepository.findByDate(null);
        if (optionalDefaultSchedule.isPresent()) {
            final Schedule defaultSchedule = optionalDefaultSchedule.get();
            if (updatedSchedule.getDate() == null && !defaultSchedule.getId().equals(id)) {
                Locale locale = LocaleContextHolder.getLocale();
                String message = messageSource.getMessage(
                        SCHEDULE_CREATED_EXCEPTION_MESSAGE,
                        new Object[]{},
                        locale
                );
                throw new ScheduleIsAlreadyCreatedException(message);
            }
            if (schedule.getDate() == null && updatedSchedule.getDate() != null) {
                Locale locale = LocaleContextHolder.getLocale();
                String message = messageSource.getMessage(
                        SCHEDULE_DELETE_EXCEPTION_MESSAGE,
                        new Object[]{id},
                        locale
                );
                throw new CannotDeleteScheduleException(message);
            }
        }
        schedule.setStartTime(updatedSchedule.getStartTime());
        schedule.setEndTime(updatedSchedule.getEndTime());
        schedule.setDate(updatedSchedule.getDate());

        return scheduleRepository.save(schedule);
    }
}
