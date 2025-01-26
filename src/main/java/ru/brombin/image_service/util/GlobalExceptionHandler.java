package ru.brombin.image_service.util;



import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import ru.brombin.image_service.util.exception.FileStorageException;
import ru.brombin.image_service.util.exception.NotFoundException;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(NotFoundException ex, WebRequest request) {
        log.error("Resource not found exception: {}, Request: {}, Stack trace: ",
                ex.getMessage(), request.getDescription(true), ex);
        return new ErrorResponse("Requested resource not found", System.currentTimeMillis());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation failed for request: {}, Errors: {}, Stack trace: ",
                request.getDescription(true), ex.getBindingResult().getAllErrors(), ex);
        return new ErrorResponse("Invalid request data", System.currentTimeMillis());
    }

    @ExceptionHandler(FileStorageException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleFileStorageException(
            FileStorageException ex, WebRequest request) {
        log.error("File storage exception occurred. Request: {}, Error: {}, Stack trace: ",
                request.getDescription(true), ex.getMessage(), ex);
        return new ErrorResponse("Error processing file", System.currentTimeMillis());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ErrorResponse handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, WebRequest request) {
        log.error("File size limit exceeded. Request: {}, Error: {}, Stack trace: ",
                request.getDescription(true), ex.getMessage(), ex);
        return new ErrorResponse("File size limit exceeded", System.currentTimeMillis());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGlobalException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred. Request: {}, Error: {}, Stack trace: ",
                request.getDescription(true), ex.getMessage(), ex);
        return new ErrorResponse("Internal server error", System.currentTimeMillis());
    }
}