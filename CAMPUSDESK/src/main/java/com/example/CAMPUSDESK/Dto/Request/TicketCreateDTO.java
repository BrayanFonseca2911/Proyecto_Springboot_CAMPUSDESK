package com.example.CAMPUSDESK.Dto.Request;

import com.example.CAMPUSDESK.Enums.Category;
import com.example.CAMPUSDESK.Enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Creación de ticket (RF-03, CP-06). El estado y el solicitante los fija el backend. */
public record TicketCreateDTO(
        @NotBlank(message = "El título es obligatorio")
        @Size(min = 5, max = 150, message = "El título debe tener entre 5 y 150 caracteres")
        String titulo,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(min = 10, max = 4000, message = "La descripción debe tener entre 10 y 4000 caracteres")
        String descripcion,

        @NotNull(message = "La categoría es obligatoria (HARDWARE, SOFTWARE, REDES, ACCESOS, OTROS)")
        Category categoria,

        @NotNull(message = "La prioridad es obligatoria (BAJA, MEDIA, ALTA, CRITICA)")
        Priority prioridad
) {}
