package com.example.CAMPUSDESK.Controller;

import com.example.CAMPUSDESK.Dto.Request.CommentCreateDTO;
import com.example.CAMPUSDESK.Dto.Response.CommentResponseDTO;
import com.example.CAMPUSDESK.Security.UserPrincipal;
import com.example.CAMPUSDESK.Service.CommentService;
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

/** Comentarios de tickets (RF-05). */
@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Comentarios", description = "Sistema de comentarios por ticket")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Agregar comentario a un ticket activo (CP-13)")
    @PostMapping
    public ResponseEntity<CommentResponseDTO> create(@PathVariable Long ticketId,
                                                     @Valid @RequestBody CommentCreateDTO dto,
                                                     @AuthenticationPrincipal UserPrincipal me) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.create(ticketId, dto, me));
    }

    @Operation(summary = "Listar comentarios de un ticket en orden cronológico")
    @GetMapping
    public ResponseEntity<List<CommentResponseDTO>> list(@PathVariable Long ticketId,
                                                         @AuthenticationPrincipal UserPrincipal me) {
        return ResponseEntity.ok(commentService.findByTicket(ticketId, me));
    }
}
