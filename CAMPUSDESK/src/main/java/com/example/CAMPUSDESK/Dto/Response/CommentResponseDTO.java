package com.example.CAMPUSDESK.Dto.Response;

import com.example.CAMPUSDESK.Entity.Comment;

import java.time.LocalDateTime;

/** Comentario con autor y fecha, orden cronológico en el service (RF-05). */
public record CommentResponseDTO(
        Long id,
        String contenido,
        Long autorId,
        String autorNombre,
        LocalDateTime fechaCreacion
) {
    public static CommentResponseDTO from(Comment c) {
        return new CommentResponseDTO(c.getId(), c.getContenido(),
                c.getAutor().getId(), c.getAutor().getNombre(), c.getFechaCreacion());
    }
}
