package com.tvmaze.web;

import com.tvmaze.exception.InvalidSearchQueryException;
import com.tvmaze.exception.ShowNotFoundException;
import com.tvmaze.exception.TvMazeRateLimitException;
import com.tvmaze.exception.TvMazeUnavailableException;
import com.tvmaze.web.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Convierte las excepciones del middleware en respuestas HTTP con un cuerpo uniforme.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ShowNotFoundException.class)
    public ResponseEntity<ApiError> handleShowNotFound(ShowNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler({InvalidSearchQueryException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> handleInvalidRequest(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex,
                                                             HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(" "));
        return build(HttpStatus.BAD_REQUEST, message.isBlank() ? "Peticion invalida." : message, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                       HttpServletRequest request) {
        String message = "El valor recibido no es valido para el parametro %s.".formatted(ex.getName());
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(TvMazeRateLimitException.class)
    public ResponseEntity<ApiError> handleRateLimit(TvMazeRateLimitException ex, HttpServletRequest request) {
        return build(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), request);
    }

    @ExceptionHandler(TvMazeUnavailableException.class)
    public ResponseEntity<ApiError> handleUnavailable(TvMazeUnavailableException ex, HttpServletRequest request) {
        log.error("Falla al consumir TVmaze: {}", ex.getMessage(), ex);
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    /**
     * Excepciones de Spring que ya traen su propio estado (validacion de parametros
     * de metodo, rutas inexistentes): se respeta ese estado en lugar de degradarlas a 500.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleBodyValidation(MethodArgumentNotValidException ex,
                                                        HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
        return build(HttpStatus.BAD_REQUEST, message.isBlank() ? "Peticion invalida." : message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException ex,
                                                        HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "El cuerpo de la peticion no es un JSON valido o no se pudo interpretar.", request);
    }

    /** MongoDB no esta disponible o rechazo la operacion. */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDataAccess(DataAccessException ex, HttpServletRequest request) {
        log.error("Falla al acceder a MongoDB: {}", ex.getMessage(), ex);
        return build(HttpStatus.SERVICE_UNAVAILABLE,
                "No fue posible acceder a la base de datos en este momento.", request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        HttpStatus resolved = status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
        String message = ex.getReason() != null ? ex.getReason() : resolved.getReasonPhrase();
        return build(resolved, message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error inesperado en el servidor.", request);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest request) {
        ApiError body = new ApiError(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
