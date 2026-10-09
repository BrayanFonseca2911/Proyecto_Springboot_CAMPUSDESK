package com.example.CAMPUSDESK.Dto.Request;

import com.example.CAMPUSDESK.Enums.TicketStatus;
import jakarta.validation.constraints.NotNull;

/** Cambio de estado de un ticket según el flujo estricto (RF-04). */
public record TicketStatusUpdateDTO(
        @NotNull(message = "El nuevo estado es obligatorio")
        TicketStatus nuevoEstado
) {}
