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
import ru.kpfu.machinemetrics.properties.AppProperties;
import ru.kpfu.machinemetrics.repository.ScheduleRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static ru.kpfu.machinemetrics.constants.ScheduleConstants.SCHEDULE_CREATED_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.ScheduleConstants.SCHEDULE_DELETE_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.ScheduleConstants.SCHEDULE_NOT_FOUND_EXCEPTION_MESSAGE;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    private final MessageSource messageSource;

    private final AppProperties appProperties;

    public List<Schedule> listDefault() {
        return scheduleRepository.findAllByDateAndEquipmentIdOrderByDateAscWeekdayAsc(null, null);
    }

    public List<Schedule> listNotDefault() {
        return scheduleRepository.findAllNotDefault();
    }

    public List<Schedule> listByEquipmentId(Long id) {
        return scheduleRepository.findAllByEquipmentId(id);
    }

    public Schedule save(@NotNull Schedule schedule) {
        final OffsetDateTime date = schedule.getDate() != null ? schedule.getDate().truncatedTo(ChronoUnit.DAYS) : null;
        if (
                scheduleRepository.findByDateAndEquipmentIdAndWeekday(
                        date,
                        schedule.getEquipmentId(),
                        schedule.getWeekday()
                ).isPresent()
        ) {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage(
                    SCHEDULE_CREATED_EXCEPTION_MESSAGE,
                    new Object[]{},
                    locale
            );
            throw new ScheduleIsAlreadyCreatedException(message);
        }
        schedule.setDate(date);
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
        final OffsetDateTime date = updatedSchedule.getDate() != null
                ? updatedSchedule.getDate().withOffsetSameInstant(ZoneOffset.of(appProperties.getDefaultZone())).truncatedTo(ChronoUnit.DAYS)
                : null;

        Schedule schedule = getById(id);

        var optionalDefaultSchedule = scheduleRepository.findByDateAndEquipmentIdAndWeekday(
                null,
                null,
                updatedSchedule.getWeekday()
        );
        if (optionalDefaultSchedule.isPresent()) {
            final Schedule defaultSchedule = optionalDefaultSchedule.get();
            if (date == null && !defaultSchedule.getId().equals(id)) {
                Locale locale = LocaleContextHolder.getLocale();
                String message = messageSource.getMessage(
                        SCHEDULE_CREATED_EXCEPTION_MESSAGE,
                        new Object[]{},
                        locale
                );
                throw new ScheduleIsAlreadyCreatedException(message);
            }
        }
        if (schedule.getDate() == null && date != null) {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage(
                    SCHEDULE_DELETE_EXCEPTION_MESSAGE,
                    new Object[]{id},
                    locale
            );
            throw new CannotDeleteScheduleException(message);
        }
        final Optional<Schedule> existingSchedule = scheduleRepository.findByDateAndEquipmentIdAndWeekday(
                date,
                updatedSchedule.getEquipmentId(),
                updatedSchedule.getWeekday()
        );
        if (existingSchedule.isPresent() && !existingSchedule.get().getId().equals(id)) {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage(
                    SCHEDULE_CREATED_EXCEPTION_MESSAGE,
                    new Object[]{},
                    locale
            );
            throw new ScheduleIsAlreadyCreatedException(message);
        }
        schedule.setWeekday(updatedSchedule.getWeekday());
        schedule.setDate(date);
        schedule.setEquipmentId(updatedSchedule.getEquipmentId());
        schedule.setStartTime(updatedSchedule.getStartTime());
        schedule.setEndTime(updatedSchedule.getEndTime());

        return scheduleRepository.save(schedule);
    }
}
