package ru.brombin.image_service.util.exception;

public class MinioFileException extends RuntimeException {
    public MinioFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
