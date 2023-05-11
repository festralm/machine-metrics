package ru.kpfu.machinemetrics.exception;

public class ResourceNotDeletedException extends RuntimeException {
    public ResourceNotDeletedException(String message) {
        super(message);
    }
}

