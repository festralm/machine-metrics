package ru.kpfu.machinemetrics.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.kpfu.machinemetrics.dto.ScheduleCreateDto;
import ru.kpfu.machinemetrics.validation.annotation.ScheduleConstraint;

public class ScheduleValidator implements
        ConstraintValidator<ScheduleConstraint, ScheduleCreateDto> {

    @Override
    public boolean isValid(ScheduleCreateDto scheduleCreateDto, ConstraintValidatorContext context) {
        if (scheduleCreateDto == null) {
            return false;
        }

        boolean valid = true;

        if (scheduleCreateDto.getIsWorkday() == null) {
            if (scheduleCreateDto.getStartTime() == null) {
                context.disableDefaultConstraintViolation();
                context
                        .buildConstraintViolationWithTemplate("{validation.schedule.start-time.empty}")
                        .addConstraintViolation();
                valid = false;
            }
            if (scheduleCreateDto.getEndTime() == null) {
                context.disableDefaultConstraintViolation();
                context
                        .buildConstraintViolationWithTemplate("{validation.schedule.end-time.empty}")
                        .addConstraintViolation();
                valid = false;
            }
        } else {
            if (scheduleCreateDto.getStartTime() != null) {
                context.disableDefaultConstraintViolation();
                context
                        .buildConstraintViolationWithTemplate("{validation.schedule.start-time.not-empty}")
                        .addConstraintViolation();
                valid = false;
            }
            if (scheduleCreateDto.getEndTime() != null) {
                context.disableDefaultConstraintViolation();
                context
                        .buildConstraintViolationWithTemplate("{validation.schedule.end-time.not-empty}")
                        .addConstraintViolation();
                valid = false;
            }
        }

        if (scheduleCreateDto.getStartTime() != null) {
            var isStartValid = isTimeValid(scheduleCreateDto.getStartTime());
            if (!isStartValid) {
                context.disableDefaultConstraintViolation();
                context
                        .buildConstraintViolationWithTemplate("{validation.schedule.start-time.not-valid}")
                        .addConstraintViolation();
                valid = false;
            }
        }

        if (scheduleCreateDto.getEndTime() != null) {
            var isEndValid = isTimeValid(scheduleCreateDto.getEndTime());
            if (!isEndValid) {
                context.disableDefaultConstraintViolation();
                context
                        .buildConstraintViolationWithTemplate("{validation.schedule.end-time.not-valid}")
                        .addConstraintViolation();
                valid = false;
            }
        }

        if (scheduleCreateDto.getWeekday() != null && scheduleCreateDto.getDate() != null) {
            context.disableDefaultConstraintViolation();
            context
                    .buildConstraintViolationWithTemplate("{validation.schedule.date.not-empty}")
                    .addConstraintViolation();
            valid = false;
        }

        return valid;
    }

    private boolean isTimeValid(String time) {
        var split = time.split(":");
        if (split.length != 2) {
            return false;
        }

        if (split[0].length() != 2 || split[1].length() != 2) {
            return false;
        }

        try {
            var hours = Long.parseLong(split[0]);
            var minutes = Long.parseLong(split[1]);

            if (hours > 24 || hours < 0 || minutes > 60 || minutes < 0) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
