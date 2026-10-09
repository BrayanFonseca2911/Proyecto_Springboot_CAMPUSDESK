package com.example.CAMPUSDESK.Controller;

import com.example.CAMPUSDESK.Dto.Response.HistoryResponseDTO;
import com.example.CAMPUSDESK.Security.UserPrincipal;
import com.example.CAMPUSDESK.Service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Historial de cambios: SOLO GET, no hay PUT/PATCH/DELETE (RF-06, CP-14). */
@RestController
@RequestMapping("/api/tickets/{ticketId}/history")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Historial", description = "Línea de tiempo inmutable de cambios de estado")
public class HistoryController {

    private final HistoryService historyService;

    @Operation(summary = "Consultar historial de un ticket (secuencia de estados, responsables y fechas)")
    @GetMapping
    public ResponseEntity<List<HistoryResponseDTO>> list(@PathVariable Long ticketId,
                                                         @AuthenticationPrincipal UserPrincipal me) {
        return ResponseEntity.ok(historyService.findByTicket(ticketId, me));
    }
}
