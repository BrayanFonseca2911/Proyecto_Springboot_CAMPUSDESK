package com.example.CAMPUSDESK.Dto.Response;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Estructura JSON uniforme de error (RT-04). Prohibido exponer stack traces,
 * tokens o contraseñas.
 */
public record ErrorResponseDTO(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp,
        Map<String, String> details
) {
    public static ErrorResponseDTO of(int status, String error, String message, String path) {
        return new ErrorResponseDTO(status, error, message, path, LocalDateTime.now(), null);
    }

    public static ErrorResponseDTO of(int status, String error, String message, String path,
                                      Map<String, String> details) {
        return new ErrorResponseDTO(status, error, message, path, LocalDateTime.now(), details);
    }
}
