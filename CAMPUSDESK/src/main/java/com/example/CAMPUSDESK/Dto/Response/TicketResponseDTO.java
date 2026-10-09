package com.example.CAMPUSDESK.Dto.Response;

import com.example.CAMPUSDESK.Entity.Ticket;
import com.example.CAMPUSDESK.Enums.Category;
import com.example.CAMPUSDESK.Enums.Priority;
import com.example.CAMPUSDESK.Enums.TicketStatus;

import java.time.LocalDateTime;

/** Representación pública de un ticket para la API. */
public record TicketResponseDTO(
        Long id,
        String titulo,
        String descripcion,
        Category categoria,
        Priority prioridad,
        TicketStatus estado,
        Long solicitanteId,
        String solicitanteNombre,
        Long tecnicoAsignadoId,
        String tecnicoAsignadoNombre,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
    public static TicketResponseDTO from(Ticket t) {
        return new TicketResponseDTO(
                t.getId(), t.getTitulo(), t.getDescripcion(), t.getCategoria(),
                t.getPrioridad(), t.getEstado(),
                t.getSolicitante().getId(), t.getSolicitante().getNombre(),
                t.getTecnicoAsignado() != null ? t.getTecnicoAsignado().getId() : null,
                t.getTecnicoAsignado() != null ? t.getTecnicoAsignado().getNombre() : null,
                t.getFechaCreacion(), t.getFechaActualizacion()
        );
    }
}
