package ru.kpfu.machinemetrics.exception;

public class ScheduleIsAlreadyCreatedException extends RuntimeException {
    public ScheduleIsAlreadyCreatedException(String message) {
        super(message);
    }
}
