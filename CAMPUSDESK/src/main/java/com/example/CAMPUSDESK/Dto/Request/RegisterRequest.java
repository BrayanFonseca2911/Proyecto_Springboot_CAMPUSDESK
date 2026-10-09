package com.example.CAMPUSDESK.Dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Registro público (RF-01, CP-01/CP-02). NO incluye campo rol: el backend
 * siempre asigna Role.USER; cualquier rol enviado en el JSON se ignora.
 */
public record RegisterRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es válido")
        @Size(max = 150)
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,72}$",
                message = "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número")
        String password
) {}
