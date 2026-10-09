package com.example.CAMPUSDESK.Exception;

import com.example.CAMPUSDESK.Dto.Response.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejo global de excepciones (RT-04). Devuelve siempre la estructura JSON
 * uniforme de ErrorResponseDTO sin exponer stack traces ni datos sensibles.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 400 - Datos inválidos por Bean Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex,
                                                             HttpServletRequest req) {
        Map<String, String> details = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            details.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "Datos inválidos",
                "Uno o más campos no cumplen las reglas de validación", req, details);
    }

    // 400 - Reglas de negocio violadas
    @ExceptionHandler({BadRequestException.class, InvalidStatusTransitionException.class})
    public ResponseEntity<ErrorResponseDTO> handleBadRequest(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req, null);
    }

    // 400 - JSON mal formado o enum inexistente (RT-04)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnreadable(HttpMessageNotReadableException ex,
                                                             HttpServletRequest req) {
        String msg = "Cuerpo de la solicitud inválido: verifique el formato JSON y los valores de los enums";
        Throwable specific = org.springframework.core.NestedExceptionUtils.getMostSpecificCause(ex);
        if (specific instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException ife
                && ife.getTargetType() != null && ife.getTargetType().isEnum()) {
            msg = "Valor de enum inexistente: '" + ife.getValue() + "' para "
                    + ife.getTargetType().getSimpleName();
        }
        return build(HttpStatus.BAD_REQUEST, "Bad Request", msg, req, null);
    }

    // 400 - Parámetros de query/path con tipo inválido (enum inexistente)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                               HttpServletRequest req) {
        Class<?> type = ex.getRequiredType();
        String msg = (type != null && type.isEnum())
                ? "Parámetro '" + ex.getName() + "' inválido. Valores permitidos: " + java.util.Arrays.toString(type.getEnumConstants())
                : "Parámetro '" + ex.getName() + "' inválido";
        return build(HttpStatus.BAD_REQUEST, "Bad Request", msg, req, null);
    }

    // 401 - Credenciales/token inválido lanzado desde servicios
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnauthorized(UnauthorizedAccessException ex,
                                                               HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), req, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthn(AuthenticationException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Unauthorized", "Credenciales inválidas", req, null);
    }

    // 403 - Rol insuficiente o falta de propiedad del recurso
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleForbidden(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Forbidden",
                "Acceso denegado por rol o falta de propiedad sobre el recurso", req, null);
    }

    // 404 - Recurso no existente
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(ResourceNotFoundException ex,
                                                           HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req, null);
    }

    // 500 - Error controlado: se loguea internamente, nunca se expone el stack trace
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Error interno no esperado en {}", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Ha ocurrido un error interno. Intente nuevamente o contacte al administrador.",
                req, null);
    }

    private ResponseEntity<ErrorResponseDTO> build(HttpStatus status, String error, String message,
                                                   HttpServletRequest req, Map<String, String> details) {
        ErrorResponseDTO body = details == null
                ? ErrorResponseDTO.of(status.value(), error, message, req.getRequestURI())
                : ErrorResponseDTO.of(status.value(), error, message, req.getRequestURI(), details);
        return ResponseEntity.status(status).body(body);
    }
}
