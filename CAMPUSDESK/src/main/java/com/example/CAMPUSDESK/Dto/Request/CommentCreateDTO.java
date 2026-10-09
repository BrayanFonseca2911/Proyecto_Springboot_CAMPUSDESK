package com.example.CAMPUSDESK.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Nuevo comentario (RF-05). */
public record CommentCreateDTO(
        @NotBlank(message = "El contenido del comentario es obligatorio")
        @Size(min = 2, max = 2000, message = "El comentario debe tener entre 2 y 2000 caracteres")
        String contenido
) {}
