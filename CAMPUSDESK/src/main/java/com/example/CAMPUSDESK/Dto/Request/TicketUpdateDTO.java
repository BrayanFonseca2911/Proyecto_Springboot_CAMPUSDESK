package com.example.CAMPUSDESK.Dto.Request;

import com.example.CAMPUSDESK.Enums.Category;
import com.example.CAMPUSDESK.Enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Edición de datos de un ticket (solo ABIERTA y sin técnico asignado, RF-01/RF-04). */
public record TicketUpdateDTO(
        @NotBlank(message = "El título es obligatorio")
        @Size(min = 5, max = 150, message = "El título debe tener entre 5 y 150 caracteres")
        String titulo,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(min = 10, max = 4000, message = "La descripción debe tener entre 10 y 4000 caracteres")
        String descripcion,

        Category categoria,

        Priority prioridad
) {}
