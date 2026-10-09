package com.example.CAMPUSDESK.Dto.Request;

import jakarta.validation.constraints.NotNull;

/** Asignación/reasignación de técnico (RF-04 reglas 2 y 7, solo ADMIN). */
public record TicketAssignDTO(
        @NotNull(message = "El id del técnico es obligatorio")
        Long tecnicoId
) {}
