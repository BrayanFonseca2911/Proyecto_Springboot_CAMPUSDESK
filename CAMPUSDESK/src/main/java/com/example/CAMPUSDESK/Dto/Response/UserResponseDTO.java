package com.example.CAMPUSDESK.Dto.Response;

import com.example.CAMPUSDESK.Entity.User;
import com.example.CAMPUSDESK.Enums.Role;

import java.time.LocalDateTime;

/** Proyección segura de User: NUNCA expone la contraseña (RT-04). */
public record UserResponseDTO(
        Long id,
        String nombre,
        String email,
        Role rol,
        LocalDateTime fechaCreacion
) {
    public static UserResponseDTO from(User u) {
        return new UserResponseDTO(u.getId(), u.getNombre(), u.getEmail(), u.getRol(), u.getFechaCreacion());
    }
}
