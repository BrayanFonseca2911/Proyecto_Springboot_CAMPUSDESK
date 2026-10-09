package com.example.CAMPUSDESK.Dto.Response;

import com.example.CAMPUSDESK.Enums.Role;

/** Respuesta de login/registro (RF-02). */
public record AuthResponse(
        String token,
        String tipoToken,
        long expiraEnSegundos,
        Long userId,
        String nombre,
        String email,
        Role rol
) {}
