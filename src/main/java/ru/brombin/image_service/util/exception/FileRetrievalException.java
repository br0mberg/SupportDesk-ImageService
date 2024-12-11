package ru.brombin.image_service.util.exception;

public class FileRetrievalException extends RuntimeException {
    public FileRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
