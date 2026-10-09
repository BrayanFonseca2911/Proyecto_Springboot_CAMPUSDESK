package com.example.CAMPUSDESK.Dto.Response;

import com.example.CAMPUSDESK.Entity.History;
import com.example.CAMPUSDESK.Enums.TicketStatus;

import java.time.LocalDateTime;

/** Registro del historial inmutable (RF-06). */
public record HistoryResponseDTO(
        Long id,
        Long ticketId,
        TicketStatus estadoAnterior,
        TicketStatus estadoNuevo,
        Long usuarioResponsableId,
        String usuarioResponsableNombre,
        LocalDateTime fechaHora
) {
    public static HistoryResponseDTO from(History h) {
        return new HistoryResponseDTO(h.getId(), h.getTicket().getId(), h.getEstadoAnterior(),
                h.getEstadoNuevo(), h.getUsuarioResponsable().getId(),
                h.getUsuarioResponsable().getNombre(), h.getFechaHora());
    }
}
