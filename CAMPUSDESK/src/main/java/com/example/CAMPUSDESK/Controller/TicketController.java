package com.example.CAMPUSDESK.Controller;

import com.example.CAMPUSDESK.Dto.Request.*;
import com.example.CAMPUSDESK.Dto.Response.TicketResponseDTO;
import com.example.CAMPUSDESK.Enums.Category;
import com.example.CAMPUSDESK.Enums.Priority;
import com.example.CAMPUSDESK.Enums.TicketStatus;
import com.example.CAMPUSDESK.Security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** CRUD y ciclo de vida de tickets. Sin lógica de negocio aquí (RT-01). */
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tickets", description = "Gestión de incidencias y flujo estricto de estados")
public class TicketController {

    private final TicketService ticketService;

    @Operation(summary = "Crear ticket: estado inicial ABIERTA automático (CP-06)")
    @PostMapping
    public ResponseEntity<TicketResponseDTO> create(@Valid @RequestBody TicketCreateDTO dto,
                                                    @AuthenticationPrincipal UserPrincipal me) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.create(dto, me));
    }

    @Operation(summary = "Listar tickets visibles según rol, con filtros estado/prioridad/categoría (RF-08)")
    @GetMapping
    public ResponseEntity<List<TicketResponseDTO>> list(
            @RequestParam(required = false) TicketStatus estado,
            @RequestParam(required = false) Priority prioridad,
            @RequestParam(required = false) Category categoria,
            @AuthenticationPrincipal UserPrincipal me) {
        return ResponseEntity.ok(ticketService.findScoped(me, estado, prioridad, categoria));
    }

    @Operation(summary = "Detalle de un ticket (valida propiedad por rol)")
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> findById(@PathVariable Long id,
                                                      @AuthenticationPrincipal UserPrincipal me) {
        return ResponseEntity.ok(ticketService.findById(id, me));
    }

    @Operation(summary = "Editar datos del ticket (USER: solo ABIERTA sin técnico; CERRADA es inmutable)")
    @PutMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> update(@PathVariable Long id,
                                                    @Valid @RequestBody TicketUpdateDTO dto,
                                                    @AuthenticationPrincipal UserPrincipal me) {
        return ResponseEntity.ok(ticketService.update(id, dto, me));
    }

    @Operation(summary = "Asignar/reasignar técnico (solo ADMIN): ABIERTA -> ASIGNADA (CP-07)")
    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponseDTO> assign(@PathVariable Long id,
                                                    @Valid @RequestBody TicketAssignDTO dto,
                                                    @AuthenticationPrincipal UserPrincipal me) {
        return ResponseEntity.ok(ticketService.assignTechnician(id, dto, me));
    }

    @Operation(summary = "Cambiar estado según flujo estricto (CP-08..CP-12)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponseDTO> changeStatus(@PathVariable Long id,
                                                          @Valid @RequestBody TicketStatusUpdateDTO dto,
                                                          @AuthenticationPrincipal UserPrincipal me) {
        return ResponseEntity.ok(ticketService.changeStatus(id, dto, me));
    }
}
