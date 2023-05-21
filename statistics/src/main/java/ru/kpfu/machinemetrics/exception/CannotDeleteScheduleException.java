package ru.kpfu.machinemetrics.exception;

public class CannotDeleteScheduleException extends RuntimeException {
    public CannotDeleteScheduleException(String message) {
        super(message);
    }
}
